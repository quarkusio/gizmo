package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;
import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.MethodBuilder;
import io.github.dmlloyd.classfile.MethodSignature;
import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.github.dmlloyd.classfile.TypeKind;
import io.github.dmlloyd.classfile.attribute.ExceptionsAttribute;
import io.github.dmlloyd.classfile.attribute.MethodParameterInfo;
import io.github.dmlloyd.classfile.attribute.MethodParametersAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.SignatureAttribute;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.TypeParameter;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.ExecutableCreator;
import io.quarkus.gizmo2.creator.ParamCreator;

public sealed abstract class ExecutableCreatorImpl extends ModifiableCreatorImpl implements ExecutableCreator
        permits ConstructorCreatorImpl, MethodCreatorImpl {

    private static final MethodParameterInfo EMPTY_PI = MethodParameterInfo.of(Optional.empty(), 0);

    static final int ST_INITIAL = 0;
    static final int ST_BODY = 1;
    static final int ST_POST_BODY = 2;
    static final int ST_DONE = 3;

    final BitSet locals = new BitSet();
    final TypeCreatorImpl typeCreator;

    private List<TypeParameter> typeParameters = List.of();

    GenericType genericReturnType;
    boolean typeEstablished;
    MethodTypeDesc type = null;
    List<ParamVarImpl> params = List.of();
    int state = ST_INITIAL;
    List<GenericType.OfThrows> throws_ = List.of();

    ExecutableCreatorImpl(final TypeCreatorImpl typeCreator) {
        super(typeCreator.gizmo);
        this.typeCreator = typeCreator;
    }

    public MethodTypeDesc type() {
        MethodTypeDesc type = this.type;
        if (type == null) {
            type = this.type = computeType();
        }
        return type;
    }

    public void setType(final MethodTypeDesc desc) {
        if (state >= ST_BODY) {
            throw new IllegalStateException("Type may no longer be changed");
        }
        if (typeEstablished) {
            MethodTypeDesc type = type();
            if (!desc.equals(type)) {
                throw new IllegalArgumentException("Type " + desc + " does not match established type " + type);
            }
        } else {
            MethodTypeDesc existing = type;
            if (desc.equals(existing)) {
                return;
            }
            // validate existing information
            GenericType returnType = this.genericReturnType;
            if (returnType != null && !desc.returnType().equals(returnType.desc())) {
                throw new IllegalArgumentException(
                        "Type " + desc + " has a return type that does not match established return type " + returnType);
            }
            int paramCnt = params.size();
            int descParamCnt = desc.parameterCount();
            if (paramCnt > descParamCnt) {
                throw new IllegalArgumentException(
                        "Existing parameter count (" + paramCnt + ") is greater than the number of parameters in " + desc);
            }
            List<ParamVarImpl> params = this.params;
            for (int i = 0; i < paramCnt; i++) {
                final ParamVarImpl param = params.get(i);
                if (param != null && !param.type().equals(desc.parameterType(i))) {
                    throw new IllegalArgumentException(
                            "Defined parameter " + i + " has a type of " + param.type() + " which conflicts with " + desc);
                }
            }
            clearType();
            type = desc;
            this.genericReturnType = GenericType.of(desc.returnType());
            if (params.size() != descParamCnt) {
                if (params instanceof ArrayList<ParamVarImpl> al) {
                    al.ensureCapacity(descParamCnt);
                } else {
                    // exactly size the array
                    this.params = new ArrayList<>(descParamCnt);
                    this.params.addAll(params);
                }
            }
            typeEstablished = true;
        }
    }

    MethodTypeDesc computeType() {
        assert type == null;
        return MethodTypeDesc.of(returnType(),
                params.stream().map(ParamVarImpl::type).toArray(ClassDesc[]::new));
    }

    public GenericType genericReturnType() {
        GenericType returnType = this.genericReturnType;
        if (returnType == null) {
            return GenericType.of(CD_void);
        }
        return returnType;
    }

    public ClassDesc returnType() {
        return genericReturnType().desc();
    }

    void returning(final GenericType type) {
        checkNotNullParam("type", type);
        if (state >= ST_BODY) {
            throw new IllegalStateException("Return type may no longer be changed");
        }
        GenericType returnType = this.genericReturnType;
        if (returnType == null) {
            assert !typeEstablished;
            this.genericReturnType = type;
        } else if (!returnType.equals(type)) {
            throw new IllegalArgumentException("Return type " + type + " does not match established return type " + returnType);
        }
    }

    void returning(final ClassDesc type) {
        returning(GenericType.of(type));
    }

    void doBody(final Consumer<BlockCreator> builder, MethodBuilder mb) {
        ArrayList<TypeAnnotation> visible = new ArrayList<>();
        ArrayList<TypeAnnotation> invisible = new ArrayList<>();
        mb.with(SignatureAttribute.of(computeSignature()));
        mb.withFlags(modifiers);
        addVisible(mb);
        addInvisible(mb);
        List<GenericType.OfThrows> throws_ = this.throws_;
        if (!throws_.isEmpty()) {
            mb.with(ExceptionsAttribute.of(
                    throws_.stream().map(GenericType::desc).map(cd -> typeCreator.zb.constantPool().classEntry(cd)).toList()));
            for (int i = 0; i < throws_.size(); i++) {
                final GenericType.OfThrows genericType = throws_.get(i);
                Util.computeAnnotations(genericType, RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofThrows(i),
                        visible, new ArrayDeque<>());
                Util.computeAnnotations(genericType, RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofThrows(i),
                        invisible, new ArrayDeque<>());
            }
        }
        // lock parameters
        List<MethodParameterInfo> mpi = params.stream()
                .map(pv -> pv == null ? EMPTY_PI : MethodParameterInfo.ofParameter(Optional.of(pv.name()), pv.flags()))
                .toList();
        if (!mpi.isEmpty()) {
            mb.with(MethodParametersAttribute.of(mpi));
        }
        // find parameter annotations, if any
        if (params.stream().anyMatch(pvi -> !pvi.visible.isEmpty())) {
            mb.with(RuntimeVisibleParameterAnnotationsAttribute.of(params.stream().map(
                    pvi -> pvi != null ? pvi.visible : List.<Annotation> of()).toList()));
        }
        if (params.stream().anyMatch(pvi -> !pvi.invisible.isEmpty())) {
            mb.with(RuntimeInvisibleParameterAnnotationsAttribute.of(params.stream().map(
                    pvi -> pvi != null ? pvi.invisible : List.<Annotation> of()).toList()));
        }
        for (int i = 0; i < params.size(); i++) {
            GenericType genericType = params.get(i).genericType();
            Util.computeAnnotations(genericType, RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofMethodFormalParameter(i),
                    visible, new ArrayDeque<>());
            Util.computeAnnotations(genericType, RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofMethodFormalParameter(i),
                    invisible, new ArrayDeque<>());
        }
        Util.computeAnnotations(genericReturnType(), RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofMethodReturn(),
                visible, new ArrayDeque<>());
        Util.computeAnnotations(genericReturnType(), RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofMethodReturn(),
                invisible, new ArrayDeque<>());
        // todo: `this` type annotations and generic type
        for (int i = 0; i < typeParameters.size(); i++) {
            final TypeParameter tv = typeParameters.get(i);
            Util.computeAnnotations(tv, RetentionPolicy.RUNTIME,
                    TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, i),
                    visible, new ArrayDeque<>());
            Util.computeAnnotations(tv, RetentionPolicy.CLASS,
                    TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, i),
                    invisible, new ArrayDeque<>());
        }
        if (builder != null) {
            mb.withCode(cb -> {
                doCode(builder, cb);
            });
        }
        if (!visible.isEmpty()) {
            mb.with(RuntimeVisibleTypeAnnotationsAttribute.of(visible));
        }
        if (!invisible.isEmpty()) {
            mb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(invisible));
        }
    }

    MethodSignature computeSignature() {
        return MethodSignature.of(
                typeParameters.stream().map(Util::typeParamOf).toList(),
                throws_.stream().map(Util::signatureOf).map(Signature.ThrowableSig.class::cast).toList(),
                Util.signatureOf(genericReturnType()),
                params.stream().map(ParamVarImpl::genericType).map(Util::signatureOf).toArray(Signature[]::new));
    }

    void doCode(final Consumer<BlockCreator> builder, final CodeBuilder cb) {
        ArrayList<TypeAnnotation> visible = new ArrayList<>();
        ArrayList<TypeAnnotation> invisible = new ArrayList<>();
        BlockCreatorImpl bc = new BlockCreatorImpl(typeCreator, cb, returnType(),
                this instanceof ConstructorCreator ? "new" : name());
        if ((modifiers & ACC_STATIC) == 0) {
            // reserve `this` for all instance methods
            cb.localVariable(0, "this", typeCreator.type(), bc.startLabel(), bc.endLabel());
            GenericType.OfClass genericType = typeCreator.genericType();
            if (!genericType.isRaw()) {
                cb.localVariableType(0, "this", Util.signatureOf(genericType), bc.startLabel(), bc.endLabel());
            }
            if (genericType.hasVisibleAnnotations()) {
                Util.computeAnnotations(genericType, RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofLocalVariable(
                        List.of(TypeAnnotation.LocalVarTargetInfo.of(bc.startLabel(), bc.endLabel(), 0))),
                        visible, new ArrayDeque<>());
                Util.computeAnnotations(genericType, RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofMethodReceiver(),
                        visible, new ArrayDeque<>());
            }
            if (genericType.hasInvisibleAnnotations()) {
                Util.computeAnnotations(genericType, RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofLocalVariable(
                        List.of(TypeAnnotation.LocalVarTargetInfo.of(bc.startLabel(), bc.endLabel(), 0))),
                        invisible, new ArrayDeque<>());
                Util.computeAnnotations(genericType, RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofMethodReceiver(),
                        invisible, new ArrayDeque<>());
            }
        }
        for (final ParamVarImpl param : params) {
            if (param != null) {
                cb.localVariable(param.slot(), param.name(), param.type(), bc.startLabel(), bc.endLabel());
                GenericType genericType = param.genericType();
                if (!genericType.isRaw()) {
                    cb.localVariableType(param.slot(), param.name(), Util.signatureOf(genericType), bc.startLabel(),
                            bc.endLabel());
                }
                if (genericType.hasVisibleAnnotations()) {
                    Util.computeAnnotations(genericType, RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofLocalVariable(
                            List.of(TypeAnnotation.LocalVarTargetInfo.of(bc.startLabel(), bc.endLabel(), param.slot()))),
                            visible, new ArrayDeque<>());
                }
                if (genericType.hasInvisibleAnnotations()) {
                    Util.computeAnnotations(genericType, RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofLocalVariable(
                            List.of(TypeAnnotation.LocalVarTargetInfo.of(bc.startLabel(), bc.endLabel(), param.slot()))),
                            invisible, new ArrayDeque<>());
                }
            }
        }
        bc.accept(builder);
        bc.writeCode(cb, bc);
        if (bc.mayFallThrough()) {
            if (creationSite == null) {
                throw new IllegalStateException("Outermost block of an executable member must not fall out"
                        + " (return or throw instead)" + Util.trackingMessage);
            } else {
                throw new IllegalStateException("Outermost block of an executable member created at " + creationSite
                        + " must not fall out (return or throw instead)");
            }
        }
        bc.writeAnnotations(RetentionPolicy.RUNTIME, visible);
        bc.writeAnnotations(RetentionPolicy.CLASS, invisible);
        if (!visible.isEmpty()) {
            cb.with(RuntimeVisibleTypeAnnotationsAttribute.of(visible));
        }
        if (!invisible.isEmpty()) {
            cb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(invisible));
        }
    }

    abstract String name();

    void body(final Consumer<BlockCreator> builder) {
        if (state >= ST_BODY) {
            throw new IllegalStateException("Body established twice");
        }
        state = ST_BODY;
        try {
            typeCreator.zb.withMethod(name(), type(), modifiers, mb -> {
                doBody(builder, mb);
            });
        } finally {
            state = ST_POST_BODY;
        }
    }

    public ParamVar parameter(final String name, final Consumer<ParamCreator> builder) {
        return parameter(name, params.size(), builder);
    }

    public ParamVar parameter(final String name, final int position, final Consumer<ParamCreator> builder) {
        if (state >= ST_BODY) {
            throw new IllegalStateException("Parameters may no longer be established");
        }
        MethodTypeDesc type = this.type;
        if (type != null && !typeEstablished) {
            clearType();
            type = null;
        }
        int slot;
        ParamCreatorImpl pc;
        if (type == null) {
            // all parameters not established
            int size = params.size();
            if (position != size) {
                throw new IllegalStateException("The method type was not established upfront and so parameters"
                        + " must be declared in order: expected parameter " + size + ", but got " + position);
            }
            if (size == 0) {
                slot = firstSlot();
            } else {
                ParamVarImpl last = params.get(size - 1);
                slot = last.slot() + last.slotSize();
            }
            pc = new ParamCreatorImpl(typeCreator.gizmo);
        } else {
            if (position < 0 || position >= type.parameterCount()) {
                throw new IndexOutOfBoundsException("Parameter position " + position + " is out of bounds for type " + type);
            }
            if (position < params.size()) {
                throw new IllegalStateException("Parameter already defined at position " + position);
            }
            pc = new ParamCreatorImpl(typeCreator.gizmo, GenericType.of(type.parameterType(position)));
            slot = firstSlot() + IntStream.range(0, position).mapToObj(type::parameterType).map(TypeKind::from)
                    .mapToInt(TypeKind::slotSize).sum();
        }
        ParamVarImpl pv = pc.apply(builder, name, position, slot);
        if (params instanceof ArrayList<ParamVarImpl> al) {
            al.add(pv);
        } else {
            params = Util.listWith(params, pv);
        }
        locals.set(slot);
        if (TypeKind.from(pv.type()).slotSize() == 2) {
            // reserve the next slot as well
            locals.set(slot + 1);
        }
        return pv;
    }

    public void throws_(final GenericType.OfThrows throwableType) {
        checkNotNullParam("throwableType", throwableType);
        if (state >= ST_BODY) {
            throw new IllegalStateException("Exception throws may no longer be established");
        }
        if (throws_ instanceof ArrayList<GenericType.OfThrows> al) {
            al.add(throwableType);
        } else {
            throws_ = Util.listWith(throws_, throwableType);
        }
    }

    void clearType() {
        type = null;
    }

    int firstSlot() {
        return 1;
    }

    public ClassDesc owner() {
        return typeCreator.type();
    }

    <T extends TypeParameter> T addTypeParameter(T var) {
        if (typeParameters instanceof ArrayList<TypeParameter> al) {
            al.add(var);
        } else {
            typeParameters = Util.listWith(typeParameters, var);
        }
        return var;
    }
}
