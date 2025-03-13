package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.ClassSignature;
import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.attribute.SignatureAttribute;
import io.github.dmlloyd.classfile.attribute.SourceFileAttribute;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.github.dmlloyd.classfile.extras.reflect.ClassFileFormatVersion;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;
import io.quarkus.gizmo2.creator.TypeCreator;
import io.quarkus.gizmo2.desc.MethodDesc;

public abstract sealed class TypeCreatorImpl extends AnnotatableCreatorImpl implements TypeCreator
        permits ClassCreatorImpl, InterfaceCreatorImpl {
    private ClassFileFormatVersion version = ClassFileFormatVersion.RELEASE_17;
    private final ClassDesc type;
    private ClassDesc superType = ConstantDescs.CD_Object;
    private Signature.ClassTypeSig superSig = Signature.ClassTypeSig.of(CD_Object);
    private ClassSignature sig;
    private List<Signature.TypeParam> typeParams = List.of();
    final ClassBuilder zb;
    private List<Consumer<BlockCreator>> inits = List.of();
    private List<Signature.ClassTypeSig> interfaceSigs = List.of();
    private int flags;

    TypeCreatorImpl(final ClassDesc type, final ClassBuilder zb, final int flags) {
        this.type = type;
        this.zb = zb;
        this.flags = flags;
    }

    public void withVersion(final ClassFileFormatVersion version) {
        this.version = Objects.requireNonNull(version, "version");
    }

    public void withTypeParam(final Signature.TypeParam param) {
        Objects.requireNonNull(param, "param");
        if (typeParams.isEmpty()) {
            typeParams = new ArrayList<>(4);
        }
        typeParams.add(param);
    }

    public void withFlag(final AccessFlag flag) {
        flags |= flag.mask();
    }

    protected void withoutFlag(final AccessFlag flag) {
        flags &= ~flag.mask();
    }

    protected void withoutFlags(AccessFlag... flags) {
        for (AccessFlag flag : flags) {
            withoutFlag(flag);
        }
    }

    public void sourceFile(final String name) {
        zb.with(SourceFileAttribute.of(name));
    }

    void extends_(final Signature.ClassTypeSig genericType) {
        ClassDesc desc = genericType.classDesc();
        zb.withSuperclass(superType = desc);
        superSig = genericType;
        sig = null;
    }

    void extends_(final ClassDesc desc) {
        zb.withSuperclass(superType = desc);
        superSig = Signature.ClassTypeSig.of(desc);
        sig = null;
    }

    ClassDesc superClass() {
        return superType;
    }

    public ClassDesc type() {
        return type;
    }

    public ClassSignature signature() {
        ClassSignature sig = this.sig;
        if (sig == null) {
            // compute one
            sig = this.sig = computeSignature();
        }
        return sig;
    }

    ClassSignature computeSignature() {
        return ClassSignature.of(typeParams, superSig, interfaceSigs.toArray(Signature.ClassTypeSig[]::new));
    }

    public void implements_(final Signature.ClassTypeSig genericType) {
        zb.withInterfaceSymbols(genericType.classDesc());
        if (interfaceSigs.isEmpty()) {
            interfaceSigs = new ArrayList<>(4);
        }
        interfaceSigs.add(genericType);
        sig = null;
    }

    public void implements_(final ClassDesc interface_) {
        implements_(Signature.ClassTypeSig.of(interface_));
    }

    public void initializer(final Consumer<BlockCreator> builder) {
        if (inits.isEmpty()) {
            inits = new ArrayList<>(4);
        }
        inits.add(Objects.requireNonNull(builder, "builder"));
    }

    public MethodDesc staticMethod(final String name, final Consumer<StaticMethodCreator> builder) {
        Objects.requireNonNull(builder, "builder");
        StaticMethodCreatorImpl smc = new StaticMethodCreatorImpl(this, name);
        smc.accept(builder);
        return smc.desc();
    }

    public StaticFieldVar staticField(final String name, final Consumer<StaticFieldCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var fc = new StaticFieldCreatorImpl(this, type(), name);
        fc.accept(builder);
        return Expr.staticField(fc.desc());
    }

    @Override
    public void public_() {
        withFlag(AccessFlag.PUBLIC);
    }

    @Override
    public void packagePrivate() {
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    void preAccept() {
        zb.withVersion(version.major(), 0);
    }

    void postAccept() {
        zb.withSuperclass(superSig.classDesc());
        zb.withInterfaces(interfaceSigs.stream().map(d -> zb.constantPool().classEntry(d.classDesc())).toList());
        zb.withFlags(flags);
        zb.with(SignatureAttribute.of(signature()));
        addVisible(zb);
        addInvisible(zb);
        if (!inits.isEmpty()) {
            zb.withMethod("<clinit>", MethodTypeDesc.of(CD_void), AccessFlag.STATIC.mask(), mb -> {
                mb.withCode(cb -> {
                    BlockCreatorImpl bc = new BlockCreatorImpl(this, cb, CD_void);
                    bc.accept(b0 -> {
                        for (Consumer<BlockCreator> init : inits) {
                            b0.block(init);
                        }
                    });
                });
            });
        }
    }

    abstract MethodDesc methodDesc(final String name, final MethodTypeDesc type);
}
