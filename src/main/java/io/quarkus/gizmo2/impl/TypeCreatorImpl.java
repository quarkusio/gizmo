package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;
import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import io.quarkus.gizmo2.ClassVersion;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;
import io.quarkus.gizmo2.creator.TypeCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.smallrye.common.constraint.Assert;

public abstract sealed class TypeCreatorImpl extends AnnotatableCreatorImpl implements TypeCreator
        permits ClassCreatorImpl, InterfaceCreatorImpl {
    private ClassFileFormatVersion version = ClassFileFormatVersion.RELEASE_17;
    private final ClassDesc type;
    private final ClassOutputImpl output;
    private final ThisExpr this_;
    private ClassDesc superType = ConstantDescs.CD_Object;
    private Signature.ClassTypeSig superSig = Signature.ClassTypeSig.of(CD_Object);
    private ClassSignature sig;
    private List<Signature.TypeParam> typeParams = List.of();
    final ClassBuilder zb;
    private List<Consumer<BlockCreator>> staticInits = List.of();
    List<Consumer<BlockCreator>> preInits = List.of();
    List<Consumer<BlockCreator>> postInits = List.of();
    private List<Signature.ClassTypeSig> interfaceSigs = List.of();
    private int flags;
    private boolean hasLambdaBootstrap;

    /**
     * All fields on the class.
     * The map value is {@code true} if the member is {@code static}, or {@code false} if it is not.
     */
    final Map<FieldDesc, Boolean> fields = new LinkedHashMap<>();
    /**
     * All methods on the class.
     * The map value is {@code true} if the member is {@code static}, or {@code false} if it is not.
     */
    final Map<MethodDesc, Boolean> methods = new LinkedHashMap<>();
    final Set<ConstructorDesc> constructors = new LinkedHashSet<>();

    TypeCreatorImpl(final ClassDesc type, final ClassOutputImpl output, final ClassBuilder zb, final int flags) {
        this.type = type;
        this_ = new ThisExpr(type);
        this.output = output;
        this.zb = zb;
        this.flags = flags;
    }

    public ClassOutputImpl output() {
        return output;
    }

    @Override
    public void withVersion(final Runtime.Version version) {
        checkNotNullParam("version", version);
        this.version = ClassFileFormatVersion.valueOf(version);
    }

    public void withVersion(final ClassVersion version) {
        checkNotNullParam("version", version);
        this.version = switch (version) {
            case V17 -> ClassFileFormatVersion.RELEASE_17;
            case V21 -> ClassFileFormatVersion.RELEASE_21;
        };
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

    public void staticInitializer(final Consumer<BlockCreator> builder) {
        if (staticInits.isEmpty()) {
            staticInits = new ArrayList<>(4);
        }
        staticInits.add(Assert.checkNotNullParam("builder", builder));
    }

    public void instanceInitializer(final Consumer<BlockCreator> builder) {
        if (!constructors.isEmpty()) {
            throw new IllegalStateException("Instance initializers may not be added once constructors exist");
        }
        if (postInits.isEmpty()) {
            postInits = new ArrayList<>(4);
        }
        postInits.add(Assert.checkNotNullParam("builder", builder));
    }

    void instancePreinitializer(final Consumer<BlockCreator> builder) {
        if (!constructors.isEmpty()) {
            throw new IllegalStateException("Instance initializers may not be added once constructors exist");
        }
        if (preInits.isEmpty()) {
            preInits = new ArrayList<>(4);
        }
        preInits.add(Assert.checkNotNullParam("builder", builder));
    }

    public MethodDesc staticMethod(final String name, final Consumer<StaticMethodCreator> builder) {
        Objects.requireNonNull(builder, "builder");
        MethodDesc desc;
        boolean isInterface = (flags & AccessFlag.INTERFACE.mask()) == AccessFlag.INTERFACE.mask();
        if (isInterface) {
            StaticInterfaceMethodCreatorImpl smc = new StaticInterfaceMethodCreatorImpl(this, name);
            smc.accept(builder);
            desc = smc.desc();
        } else {
            StaticMethodCreatorImpl smc = new StaticMethodCreatorImpl(this, name);
            smc.accept(builder);
            desc = smc.desc();
        }
        if (methods.putIfAbsent(desc, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public StaticFieldVar staticField(final String name, final Consumer<StaticFieldCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        boolean isInterface = (flags & AccessFlag.INTERFACE.mask()) == AccessFlag.INTERFACE.mask();
        var fc = new StaticFieldCreatorImpl(this, type(), name, isInterface);
        fc.accept(builder);
        FieldDesc desc = fc.desc();
        if (fields.putIfAbsent(desc, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Duplicate field added: %s".formatted(desc));
        }
        return Expr.staticField(desc);
    }

    public This this_() {
        return this_;
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
        if (!staticInits.isEmpty()) {
            zb.withMethod("<clinit>", MethodTypeDesc.of(CD_void), AccessFlag.STATIC.mask(), mb -> {
                mb.withCode(cb -> {
                    BlockCreatorImpl bc = new BlockCreatorImpl(this, cb, CD_void);
                    bc.accept(b0 -> {
                        for (Consumer<BlockCreator> init : staticInits) {
                            b0.block(init);
                        }
                    });
                    bc.writeCode(cb, bc);
                    cb.return_();
                });
            });
        }
    }

    abstract MethodDesc methodDesc(final String name, final MethodTypeDesc type);

    void buildLambdaBootstrap() {
        if (!hasLambdaBootstrap) {
            staticMethod(
                    "defineLambdaCallSite",
                    MethodTypeDesc.of(
                            CD_CallSite,
                            CD_MethodHandles_Lookup,
                            CD_String,
                            CD_MethodType),
                    smc -> {
                        smc.withFlag(AccessFlag.PRIVATE);
                        ParamVar lookup = smc.parameter("lookup", 0);
                        ParamVar base64 = smc.parameter("base64", 1);
                        ParamVar methodType = smc.parameter("methodType", 2);
                        smc.body(b0 -> {
                            var decoder = b0.define("decoder", b0.invokeStatic(MethodDesc.of(
                                    Base64.class,
                                    "getDecoder",
                                    Base64.Decoder.class)));
                            var bytes = b0.define("bytes", b0.invokeVirtual(MethodDesc.of(
                                    Base64.Decoder.class,
                                    "decode",
                                    byte[].class,
                                    String.class), decoder, base64));
                            var definedLookup = b0.define("definedLookup", b0.invokeVirtual(MethodDesc.of(
                                    MethodHandles.Lookup.class,
                                    "defineHiddenClass",
                                    MethodHandles.Lookup.class,
                                    byte[].class,
                                    boolean.class,
                                    MethodHandles.Lookup.ClassOption[].class),
                                    lookup,
                                    bytes,
                                    Constant.of(false),
                                    b0.newArray(MethodHandles.Lookup.ClassOption.class,
                                            Constant.of(MethodHandles.Lookup.ClassOption.NESTMATE))));
                            var definedClass = b0.define("definedClass", b0.invokeVirtual(
                                    MethodDesc.of(
                                            MethodHandles.Lookup.class,
                                            "lookupClass",
                                            Class.class),
                                    definedLookup));
                            var ctorType = b0.define("ctorType", b0.invokeVirtual(
                                    MethodDesc.of(
                                            MethodType.class,
                                            "changeReturnType",
                                            MethodType.class,
                                            Class.class),
                                    methodType,
                                    Constant.of(void.class)));
                            var ctorHandle = b0.define("ctorHandle", b0.invokeVirtual(
                                    MethodDesc.of(
                                            MethodHandles.Lookup.class,
                                            "findConstructor",
                                            MethodHandle.class,
                                            Class.class,
                                            MethodType.class),
                                    definedLookup,
                                    definedClass,
                                    ctorType));
                            b0.ifElse(b0.eq(
                                    b0.invokeVirtual(
                                            MethodDesc.of(
                                                    MethodType.class,
                                                    "parameterCount",
                                                    int.class),
                                            methodType),
                                    0), t1 -> {
                                        // no parameters, so it should be a singleton
                                        LocalVar instance = t1.define("instance", t1.invokeVirtual(
                                                MethodDesc.of(MethodHandle.class, "invoke", Object.class),
                                                ctorHandle));
                                        LocalVar constHandle = t1.define("constHandle", t1.invokeStatic(
                                                MethodDesc.of(
                                                        MethodHandles.class,
                                                        "constant",
                                                        MethodHandle.class,
                                                        Class.class,
                                                        Object.class),
                                                definedClass,
                                                instance));
                                        t1.return_(t1.new_(ConstantCallSite.class, t1.invokeVirtual(
                                                MethodDesc.of(
                                                        MethodHandle.class,
                                                        "asType",
                                                        MethodHandle.class,
                                                        MethodType.class),
                                                constHandle,
                                                methodType)));
                                    }, f1 -> {
                                        f1.return_(f1.new_(ConstantCallSite.class, f1.invokeVirtual(
                                                MethodDesc.of(
                                                        MethodHandle.class,
                                                        "asType",
                                                        MethodHandle.class,
                                                        MethodType.class),
                                                ctorHandle,
                                                methodType)));
                                    });
                        });
                    });
            hasLambdaBootstrap = true;
        }
    }

    @Override
    public List<FieldDesc> staticFields() {
        return fields.entrySet().stream().filter(e -> e.getValue().booleanValue()).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<FieldDesc> instanceFields() {
        return fields.entrySet().stream().filter(e -> !e.getValue().booleanValue()).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<MethodDesc> staticMethods() {
        return methods.entrySet().stream().filter(e -> e.getValue().booleanValue()).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<MethodDesc> instanceMethods() {
        return methods.entrySet().stream().filter(e -> !e.getValue().booleanValue()).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<ConstructorDesc> constructors() {
        return List.copyOf(constructors);
    }

    ElementType annotationTargetType() {
        return ElementType.TYPE;
    }
}
