package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.MethodBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.github.dmlloyd.classfile.attribute.MethodParameterInfo;
import io.github.dmlloyd.classfile.attribute.MethodParametersAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ExecutableCreator;
import io.quarkus.gizmo2.creator.ParamCreator;

public sealed abstract class ExecutableCreatorImpl extends AnnotatableCreatorImpl implements ExecutableCreator
        permits ConstructorCreatorImpl, MethodCreatorImpl {

    private static final ParamVarImpl[] NO_PARAMS = new ParamVarImpl[0];
    private static final MethodParameterInfo EMPTY_PI = MethodParameterInfo.of(Optional.empty(), 0);

    static final int ST_INITIAL = 0;
    static final int ST_BODY = 1;
    static final int ST_POST_BODY = 2;
    static final int ST_DONE = 3;

    final BitSet locals = new BitSet();
    final TypeCreatorImpl typeCreator;

    ClassDesc returnType;
    boolean typeEstablished;
    MethodTypeDesc type = null;
    int flags;
    ParamVarImpl[] params = NO_PARAMS;
    int nextParam;
    int state = ST_INITIAL;

    ExecutableCreatorImpl(final TypeCreatorImpl typeCreator, final int flags) {
        this.typeCreator = typeCreator;
        this.flags = flags;
    }

    public MethodTypeDesc type() {
        MethodTypeDesc type = this.type;
        if (type == null) {
            type = this.type = computeType();
        }
        return type;
    }

    public void withType(final MethodTypeDesc desc) {
        if (state >= ST_BODY) {
            throw new IllegalStateException("Type may no longer be changed");
        }
        if (typeEstablished) {
            MethodTypeDesc type = type();
            if (! desc.equals(type)) {
                throw new IllegalArgumentException("Type " + desc + " does not match established type " + type);
            }
        } else {
            MethodTypeDesc existing = type;
            if (desc.equals(existing)) {
                return;
            }
            // validate existing information
            ClassDesc returnType = this.returnType;
            if (returnType != null && ! desc.returnType().equals(returnType)) {
                throw new IllegalArgumentException("Type " + desc + " has a return type that does not match established return type " + returnType);
            }
            int paramCnt = nextParam;
            int descParamCnt = desc.parameterCount();
            if (paramCnt > descParamCnt) {
                throw new IllegalArgumentException("Existing parameter count (" + paramCnt + ") is greater than the number of parameters in " + desc);
            }
            ParamVarImpl[] params = this.params;
            for (int i = 0; i < paramCnt; i++) {
                final ParamVarImpl param = params[i];
                if (param != null && ! param.type().equals(desc.parameterType(i))) {
                    throw new IllegalArgumentException("Defined parameter " + i + " has a type of " + param.type() + " which conflicts with " + desc);
                }
            }
            clearType();
            type = desc;
            this.returnType = desc.returnType();
            if (params.length != descParamCnt) {
                // exactly size the array
                this.params = Arrays.copyOf(params, descParamCnt);
            }
            typeEstablished = true;
        }
    }

    MethodTypeDesc computeType() {
        assert type == null;
        return MethodTypeDesc.of(returnType(), IntStream.range(0, nextParam).mapToObj(this::param).map(ParamVarImpl::type).toArray(ClassDesc[]::new));
    }

    private ParamVarImpl param(int idx) {
        return params[idx];
    }

    public ClassDesc returnType() {
        ClassDesc returnType = this.returnType;
        if (returnType == null) {
            return CD_void;
        }
        return returnType;
    }

    void returning(final ClassDesc type) {
        Objects.requireNonNull(type, "type");
        if (state >= ST_BODY) {
            throw new IllegalStateException("Return type may no longer be changed");
        }
        ClassDesc returnType = this.returnType;
        if (returnType == null) {
            assert ! typeEstablished;
            this.returnType = type;
        } else if (! returnType.equals(type)) {
            throw new IllegalArgumentException("Return type " + type + " does not match established return type " + returnType);
        }
    }

    void doBody(final Consumer<BlockCreator> builder, MethodBuilder mb) {
        mb.withFlags(flags);
        addVisible(mb);
        addInvisible(mb);
        // lock parameters
        int arraySize = params.length;
        int parameterCount = type().parameterCount();
        if (parameterCount != arraySize) {
            params = Arrays.copyOf(params, parameterCount);
        }
        List<MethodParameterInfo> mpi = Stream.of(params).map(pv -> pv == null ? EMPTY_PI : MethodParameterInfo.ofParameter(Optional.of(pv.name()), pv.flags())).toList();
        if (! mpi.isEmpty()) {
            mb.with(MethodParametersAttribute.of(mpi));
        }
        // find parameter annotations, if any
        if (Stream.of(params).anyMatch(pvi -> !pvi.visible.isEmpty())) {
            mb.with(RuntimeVisibleParameterAnnotationsAttribute.of(Stream.of(params).map(
                pvi -> pvi != null ? pvi.visible : List.<Annotation>of()).toList()));
        }
        if (Stream.of(params).anyMatch(pvi -> !pvi.invisible.isEmpty())) {
            mb.with(RuntimeInvisibleParameterAnnotationsAttribute.of(Stream.of(params).map(
                pvi -> pvi != null ? pvi.invisible : List.<Annotation>of()).toList()));
        }
        if (builder != null) {
            mb.withCode(cb -> {
                doCode(builder, cb);
            });
        }
    }

    void doCode(final Consumer<BlockCreator> builder, final CodeBuilder cb) {
        BlockCreatorImpl bc = new BlockCreatorImpl(typeCreator, cb, returnType());
        if ((flags & AccessFlag.STATIC.mask()) == 0) {
            // reserve `this` for all instance methods
            cb.localVariable(0, "this", typeCreator.type(), bc.startLabel(), bc.endLabel());
        }
        for (ParamVarImpl param : params) {
            if (param != null) {
                cb.localVariable(param.slot(), param.name(), param.type(), bc.startLabel(), bc.endLabel());
            }
        }
        bc.accept(builder);
        bc.writeCode(cb, bc);
        if (bc.mayFallThrough()) {
            throw new IllegalStateException(
                    "Outermost block of an executable member must not fall out (return or throw instead)");
        }
    }

    abstract String name();

    void body(final Consumer<BlockCreator> builder) {
        if (state >= ST_BODY) {
            throw new IllegalStateException("Body established twice");
        }
        state = ST_BODY;
        try {
            typeCreator.zb.withMethod(name(), type(), flags, mb -> {
                doBody(builder, mb);
            });
        } finally {
            state = ST_POST_BODY;
        }
    }

    public ParamVar parameter(final String name, final Consumer<ParamCreator> builder) {
        return parameter(name, nextParam, builder);
    }

    public ParamVar parameter(final String name, final int position, final Consumer<ParamCreator> builder) {
        if (state >= ST_BODY) {
            throw new IllegalStateException("Parameters may no longer be established");
        }
        MethodTypeDesc type = this.type;
        if (type != null && ! typeEstablished) {
            clearType();
            type = null;
        }
        int slot;
        ParamCreatorImpl pc;
        if (type == null) {
            // all parameters not established
            int size = nextParam;
            if (position != size) {
                throw new IllegalStateException("Cannot define positional parameter with index " + position + " before the type has been established");
            }
            if (size == 0) {
                slot = firstSlot();
            } else {
                ParamVarImpl last = params[size - 1];
                slot = last.slot() + last.slotSize();
            }
            pc = new ParamCreatorImpl();
            if (params.length == nextParam) {
                // grow it
                params = Arrays.copyOf(params, params.length + 5);
            }
        } else {
            if (position < 0 || position > type.parameterCount()) {
                throw new IndexOutOfBoundsException("Parameter position " + position + " is out of bounds for type " + type);
            }
            ParamVarImpl existing = params[position];
            if (existing != null) {
                throw new IllegalStateException("Parameter already defined at position " + position);
            }
            pc = new ParamCreatorImpl(type.parameterType(position));
            slot = firstSlot() + IntStream.range(0, position).mapToObj(type::parameterType).map(TypeKind::from).mapToInt(TypeKind::slotSize).sum();
        }
        ParamVarImpl pv = pc.apply(builder, name, position, slot);
        params[position] = pv;
        locals.set(slot);
        if (TypeKind.from(pv.type()).slotSize() == 2) {
            // reserve the next slot as well
            locals.set(slot + 1);
        }
        nextParam = position + 1;
        return pv;
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

    public void public_() {
        withFlag(AccessFlag.PUBLIC);
        withoutFlags(AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    public void packagePrivate() {
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    public void private_() {
        withFlag(AccessFlag.PRIVATE);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PROTECTED);
    }

    public void protected_() {
        withFlag(AccessFlag.PROTECTED);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE);
    }

    public void final_() {
        withFlag(AccessFlag.FINAL);
    }

    abstract void withFlag(AccessFlag flag);

    void withoutFlag(AccessFlag flag) {
        flags &= ~flag.mask();
    }

    void withoutFlags(AccessFlag... flags) {
        for (AccessFlag flag : flags) {
            withoutFlag(flag);
        }
    }
}
