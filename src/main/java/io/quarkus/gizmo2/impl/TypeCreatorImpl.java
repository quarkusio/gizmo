package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
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
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;
import io.quarkus.gizmo2.creator.TypeCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public abstract sealed class TypeCreatorImpl extends AnnotatableCreatorImpl implements TypeCreator
        permits ClassCreatorImpl, InterfaceCreatorImpl {
    private ClassFileFormatVersion version = ClassFileFormatVersion.RELEASE_17;
    private final ClassDesc type;
    private final ClassOutputImpl output;
    private ClassDesc superType = ConstantDescs.CD_Object;
    private Signature.ClassTypeSig superSig = Signature.ClassTypeSig.of(CD_Object);
    private ClassSignature sig;
    private List<Signature.TypeParam> typeParams = List.of();
    final ClassBuilder zb;
    private List<Consumer<BlockCreator>> inits = List.of();
    private List<Signature.ClassTypeSig> interfaceSigs = List.of();
    private int flags;
    private boolean hasLambdaBootstrap;

    protected final List<FieldDesc> staticFields = new ArrayList<>();
    protected final List<FieldDesc> instanceFields = new ArrayList<>();
    protected final List<MethodDesc> staticMethods = new ArrayList<>();
    protected final List<MethodDesc> instanceMethods = new ArrayList<>();
    protected final List<ConstructorDesc> constructors = new ArrayList<>();

    TypeCreatorImpl(final ClassDesc type, final ClassOutputImpl output, final ClassBuilder zb, final int flags) {
        this.type = type;
        this.output = output;
        this.zb = zb;
        this.flags = flags;
    }

    public ClassOutputImpl output() {
        return output;
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
        MethodDesc desc = smc.desc();
        staticMethods.add(desc);
        return desc;
    }

    public StaticFieldVar staticField(final String name, final Consumer<StaticFieldCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var fc = new StaticFieldCreatorImpl(this, type(), name);
        fc.accept(builder);
        FieldDesc desc = fc.desc();
        staticFields.add(desc);
        return Expr.staticField(desc);
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

    void buildLambdaBootstrap() {
        if (! hasLambdaBootstrap) {
            staticMethod(
                "defineLambdaCallSite",
                MethodTypeDesc.of(
                    CD_CallSite,
                    CD_MethodHandles_Lookup,
                    CD_String,
                    CD_MethodType
                ),
                smc -> {
                    smc.withFlag(AccessFlag.PRIVATE);
                    ParamVar lookup = smc.parameter("lookup", 0);
                    ParamVar base64 = smc.parameter("base64", 1);
                    ParamVar methodType = smc.parameter("methodType", 2);
                    smc.body(b0 -> {
                        var decoder = b0.define("decoder", b0.invokeStatic(MethodDesc.of(
                            Base64.class,
                            "getDecoder",
                            Base64.Decoder.class
                        )));
                        var bytes = b0.define("bytes", b0.invokeVirtual(MethodDesc.of(
                            Base64.Decoder.class,
                            "decode",
                            byte[].class,
                            String.class
                        ), decoder, base64));
                        var definedLookup = b0.define("definedLookup", b0.invokeVirtual(MethodDesc.of(
                                MethodHandles.Lookup.class,
                                "defineHiddenClass",
                                MethodHandles.Lookup.class,
                                byte[].class,
                                boolean.class,
                                MethodHandles.Lookup.ClassOption[].class
                            ),
                            lookup,
                            bytes,
                            Constant.of(false),
                            b0.newArray(MethodHandles.Lookup.ClassOption.class, Constant.of(MethodHandles.Lookup.ClassOption.NESTMATE))
                        ));
                        var definedClass = b0.define("definedClass", b0.invokeVirtual(
                            MethodDesc.of(
                                MethodHandles.Lookup.class,
                                "lookupClass",
                                Class.class
                            ),
                            definedLookup
                        ));
                        var ctorType = b0.define("ctorType", b0.invokeVirtual(
                            MethodDesc.of(
                                MethodType.class,
                                "changeReturnType",
                                MethodType.class,
                                Class.class
                            ),
                            methodType,
                            Constant.of(void.class)
                        ));
                        var ctorHandle = b0.define("ctorHandle", b0.invokeVirtual(
                            MethodDesc.of(
                                MethodHandles.Lookup.class,
                                "findConstructor",
                                MethodHandle.class,
                                Class.class,
                                MethodType.class
                            ),
                            definedLookup,
                            definedClass,
                            ctorType
                        ));
                        b0.ifElse(b0.eq(
                            b0.invokeVirtual(
                                MethodDesc.of(
                                    MethodType.class,
                                    "parameterCount",
                                    int.class),
                                methodType
                            ), 0
                        ), t1 -> {
                            // no parameters, so it should be a singleton
                            LocalVar instance = t1.define("instance", t1.invokeVirtual(
                                MethodDesc.of(MethodHandle.class, "invoke", Object.class),
                                ctorHandle
                            ));
                            LocalVar constHandle = t1.define("constHandle", t1.invokeStatic(
                                MethodDesc.of(
                                    MethodHandles.class,
                                    "constant",
                                    MethodHandle.class,
                                    Class.class,
                                    Object.class
                                ),
                                definedClass,
                                instance
                            ));
                            t1.return_(t1.new_(ConstantCallSite.class, t1.invokeVirtual(
                                MethodDesc.of(
                                    MethodHandle.class,
                                    "asType",
                                    MethodHandle.class,
                                    MethodType.class
                                ),
                                constHandle,
                                methodType
                            )));
                        }, f1 -> {
                            f1.return_(f1.new_(ConstantCallSite.class, f1.invokeVirtual(
                                MethodDesc.of(
                                    MethodHandle.class,
                                    "asType",
                                    MethodHandle.class,
                                    MethodType.class
                                ),
                                ctorHandle,
                                methodType
                            )));
                        });
                    });
                }
            );
            hasLambdaBootstrap = true;
        }
    }

    @Override
    public List<FieldDesc> staticFields() {
        return Collections.unmodifiableList(staticFields);
    }

    @Override
    public List<FieldDesc> instanceFields() {
        return Collections.unmodifiableList(instanceFields);
    }

    @Override
    public List<MethodDesc> staticMethods() {
        return Collections.unmodifiableList(staticMethods);
    }

    @Override
    public List<MethodDesc> instanceMethods() {
        return Collections.unmodifiableList(instanceMethods);
    }

    @Override
    public List<ConstructorDesc> constructors() {
        return Collections.unmodifiableList(constructors);
    }
}
