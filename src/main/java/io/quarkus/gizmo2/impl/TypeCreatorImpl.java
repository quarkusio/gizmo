package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Object;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.ClassSignature;
import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.attribute.SignatureAttribute;
import io.github.dmlloyd.classfile.attribute.SourceFileAttribute;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.github.dmlloyd.classfile.extras.reflect.ClassFileFormatVersion;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;
import io.quarkus.gizmo2.creator.TypeCreator;

public abstract sealed class TypeCreatorImpl extends AnnotatableCreatorImpl implements TypeCreator permits ClassCreatorImpl, InterfaceCreatorImpl {
    private ClassFileFormatVersion version = ClassFileFormatVersion.RELEASE_17;
    private final ClassDesc type;
    private Signature.ClassTypeSig superSig;
    private ClassSignature sig;
    private List<Signature.TypeParam> typeParams = List.of();
    final ClassBuilder zb;
    private List<Consumer<BlockCreator>> inits = List.of();
    private List<Signature.ClassTypeSig> interfaceSigs = List.of();

    TypeCreatorImpl(final ClassDesc type, final ClassBuilder zb) {
        this.type = type;
        this.zb = zb;
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
        zb.withFlags(flag);
    }

    public void withFlags(final Set<AccessFlag> flags) {
        zb.withFlags(flags.toArray(AccessFlag[]::new));
    }

    public void sourceFile(final String name) {
        zb.with(SourceFileAttribute.of(name));
    }

    void extends_(final Signature.ClassTypeSig genericType) {
        ClassDesc desc = genericType.classDesc();
        zb.withSuperclass(desc);
        superSig = genericType;
        sig = null;
    }

    void extends_(final ClassDesc desc) {
        zb.withSuperclass(desc);
        superSig = Signature.ClassTypeSig.of(desc);
        sig = null;
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
        Signature.ClassTypeSig superSig = this.superSig;
        return ClassSignature.of(typeParams, superSig == null ? Signature.ClassTypeSig.of(CD_Object) : superSig, interfaceSigs.toArray(Signature.ClassTypeSig[]::new));
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

    void postAccept() {
        zb.withVersion(version.major(), 0);
        zb.with(SignatureAttribute.of(signature()));
        addVisible(zb);
        addInvisible(zb);
        if (! inits.isEmpty()) {
            zb.withMethod("<clinit>", MethodTypeDesc.of(ConstantDescs.CD_void), AccessFlag.STATIC.mask(), mb -> {
                mb.withCode(cb -> {
                    BlockCreatorImpl bc = new BlockCreatorImpl(this, cb);
                    bc.accept(b0 -> {
                        for (Consumer<BlockCreator> init : inits) {
                            b0.block(init);
                        }
                    });
                });
            });
        }
    }
}
