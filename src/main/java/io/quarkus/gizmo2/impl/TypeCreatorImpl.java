package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.desc.Descs.*;
import static io.smallrye.classfile.ClassFile.*;
import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;
import static java.lang.invoke.MethodHandles.Lookup.ClassOption.*;

import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.EOFException;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.ClassVersion;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.GenericTypes;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.TypeArgument;
import io.quarkus.gizmo2.TypeParameter;
import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;
import io.quarkus.gizmo2.creator.TypeCreator;
import io.quarkus.gizmo2.creator.TypeParameterCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.smallrye.classfile.ClassBuilder;
import io.smallrye.classfile.ClassSignature;
import io.smallrye.classfile.Signature;
import io.smallrye.classfile.TypeAnnotation;
import io.smallrye.classfile.attribute.NestMembersAttribute;
import io.smallrye.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import io.smallrye.classfile.attribute.SignatureAttribute;
import io.smallrye.classfile.attribute.SourceFileAttribute;
import io.smallrye.classfile.extras.reflect.ClassFileFormatVersion;
import io.smallrye.common.constraint.Assert;

public abstract sealed class TypeCreatorImpl extends ModifiableCreatorImpl implements TypeCreator
        permits ClassCreatorImpl, InterfaceCreatorImpl {

    // invoke is sigpoly, so don't make a shared constant for now
    private static final ClassMethodDesc MD_invoke_returning_Object = ClassMethodDesc.of(ConstantDescs.CD_MethodHandle,
            "invoke", ConstantDescs.CD_Object);

    final GizmoImpl gizmo;
    private ClassFileFormatVersion version = ClassFileFormatVersion.RELEASE_17;
    private final ClassDesc type;
    private GenericType.OfClass genericType;
    private final ClassOutput output;
    private ThisExpr this_;
    private ClassDesc superType = ConstantDescs.CD_Object;
    private GenericType.OfClass superSig = GenericTypes.GT_Object;
    private List<GenericType.OfClass> interfaceSigs = List.of();
    private List<TypeParameter> typeParameters = List.of();
    final ClassBuilder zb;
    private List<Consumer<BlockCreator>> staticInits = List.of();
    List<Consumer<BlockCreator>> preInits = List.of();
    List<Consumer<BlockCreator>> postInits = List.of();
    private List<ClassDesc> nestMembers = new ArrayList<>();
    private int bootstraps;

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

    int lambdaAndAnonClassCounter;

    TypeCreatorImpl(final GizmoImpl gizmo, final ClassDesc type, final ClassOutput output, final ClassBuilder zb) {
        super(gizmo);
        this.gizmo = gizmo;
        this.type = type;
        this.output = output;
        this.zb = zb;
    }

    public ClassOutput output() {
        return output;
    }

    @Override
    public void setVersion(final Runtime.Version version) {
        checkNotNullParam("version", version);
        this.version = ClassFileFormatVersion.valueOf(version);
    }

    public void setVersion(final ClassVersion version) {
        checkNotNullParam("version", version);
        this.version = switch (version) {
            case V17 -> ClassFileFormatVersion.RELEASE_17;
            case V21 -> ClassFileFormatVersion.RELEASE_21;
        };
    }

    ClassFileFormatVersion version() {
        return version;
    }

    void addNestMember(ClassDesc nestMember) {
        nestMembers.add(nestMember);
    }

    public void sourceFile(final String name) {
        zb.with(SourceFileAttribute.of(name));
    }

    void extends_(final GenericType.OfClass genericType) {
        ClassDesc desc = genericType.desc();
        zb.withSuperclass(superType = desc);
        superSig = genericType;
    }

    void extends_(final ClassDesc desc) {
        zb.withSuperclass(superType = desc);
        superSig = (GenericType.OfClass) GenericType.of(desc);
    }

    ClassDesc superClass() {
        return superType;
    }

    public ClassDesc type() {
        return type;
    }

    public GenericType.OfClass genericType() {
        GenericType.OfClass genericType = this.genericType;
        if (genericType == null) {
            genericType = GenericType.ofClass(type());
            if (!typeParameters.isEmpty()) {
                genericType = genericType.withArguments(typeParameters.stream()
                        .map(TypeParameter::genericType)
                        .map(TypeArgument::ofExact)
                        .map(TypeArgument.class::cast)
                        .toList());
            }
            this.genericType = genericType;
        }
        return genericType;
    }

    public boolean hasGenericType() {
        return genericType != null;
    }

    boolean signatureNeeded() {
        return !typeParameters.isEmpty() || superSig.signatureNeeded()
                || interfaceSigs.stream().anyMatch(GenericType::signatureNeeded);
    }

    ClassSignature computeSignature() {
        return ClassSignature.of(
                typeParameters.stream().map(Util::typeParamOf).toList(),
                Util.signatureOf(superSig),
                interfaceSigs.stream().map(Util::signatureOf).toArray(Signature.ClassTypeSig[]::new));
    }

    void implements_(final GenericType.OfClass genericType) {
        zb.withInterfaceSymbols(genericType.desc());
        if (interfaceSigs.isEmpty()) {
            interfaceSigs = new ArrayList<>(4);
        }
        interfaceSigs.add(genericType);
    }

    void implements_(final ClassDesc interface_) {
        implements_((GenericType.OfClass) GenericType.of(interface_));
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
        checkNotNullParam("builder", builder);
        MethodDesc desc;
        boolean isInterface = (modifiers & ACC_INTERFACE) != 0;
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
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        boolean isInterface = (modifiers & ACC_INTERFACE) != 0;
        var fc = isInterface ? new InterfaceStaticFieldCreatorImpl(this, type(), name)
                : new ClassStaticFieldCreatorImpl(this, type(), name);
        fc.accept(builder);
        FieldDesc desc = fc.desc();
        if (fields.putIfAbsent(desc, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Duplicate field added: %s".formatted(desc));
        }
        return Expr.staticField(desc);
    }

    public This this_() {
        ThisExpr this_ = this.this_;
        if (this_ == null) {
            this_ = this.this_ = new ThisExpr(type(), hasGenericType() ? genericType() : null);
        }
        return this_;
    }

    void preAccept() {
        zb.withVersion(version.major(), 0);
    }

    void postAccept() {
        zb.withSuperclass(superSig.desc());
        zb.withInterfaces(interfaceSigs.stream().map(d -> zb.constantPool().classEntry(d.desc())).toList());
        zb.withFlags(modifiers);
        if (signatureNeeded()) {
            zb.with(SignatureAttribute.of(computeSignature()));
        }
        addVisible(zb);
        addInvisible(zb);
        if (!nestMembers.isEmpty()) {
            zb.with(NestMembersAttribute.ofSymbols(nestMembers));
        }
        ArrayList<TypeAnnotation> visible = new ArrayList<>();
        ArrayList<TypeAnnotation> invisible = new ArrayList<>();
        ArrayDeque<TypeAnnotation.TypePathComponent> pathStack = new ArrayDeque<>();
        for (int i = 0; i < typeParameters.size(); i++) {
            final TypeParameter tv = typeParameters.get(i);
            Util.computeAnnotations(tv, RetentionPolicy.RUNTIME,
                    TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, i),
                    visible, pathStack);
            assert pathStack.isEmpty();
            Util.computeAnnotations(tv, RetentionPolicy.CLASS,
                    TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, i),
                    invisible, pathStack);
            assert pathStack.isEmpty();
        }
        Util.computeAnnotations(superSig, RetentionPolicy.RUNTIME,
                TypeAnnotation.TargetInfo.ofClassExtends(65535 /* superclass */),
                visible, pathStack);
        assert pathStack.isEmpty();
        Util.computeAnnotations(superSig, RetentionPolicy.CLASS,
                TypeAnnotation.TargetInfo.ofClassExtends(65535 /* superclass */),
                invisible, pathStack);
        assert pathStack.isEmpty();
        for (int i = 0; i < interfaceSigs.size(); i++) {
            Util.computeAnnotations(interfaceSigs.get(i), RetentionPolicy.RUNTIME,
                    TypeAnnotation.TargetInfo.ofClassExtends(i),
                    visible, pathStack);
            assert pathStack.isEmpty();
            Util.computeAnnotations(interfaceSigs.get(i), RetentionPolicy.CLASS,
                    TypeAnnotation.TargetInfo.ofClassExtends(i),
                    invisible, pathStack);
            assert pathStack.isEmpty();
        }
        if (!visible.isEmpty()) {
            zb.with(RuntimeVisibleTypeAnnotationsAttribute.of(visible));
        }
        if (!invisible.isEmpty()) {
            zb.with(RuntimeVisibleTypeAnnotationsAttribute.of(invisible));
        }
        if (!staticInits.isEmpty()) {
            zb.withMethod("<clinit>", MethodTypeDesc.of(CD_void), ACC_STATIC, mb -> {
                mb.withCode(cb -> {
                    BlockCreatorImpl bc = new BlockCreatorImpl(this, cb, CD_void, "static");
                    bc.accept(b0 -> {
                        for (Consumer<BlockCreator> init : staticInits) {
                            b0.block(init);
                        }
                        if (!b0.done()) {
                            b0.return_();
                        }
                    });
                    bc.writeCode(cb, bc, new StackMapBuilder());
                });
            });
        }
    }

    abstract MethodDesc methodDesc(final String name, final MethodTypeDesc type);

    /**
     * {@return true if the bootstrap flag was not yet set}
     */
    private boolean getAndSetBootstrap(Bootstrap bootstrap) {
        int bootstraps = this.bootstraps;
        int bit = 1 << bootstrap.ordinal();
        if ((bootstraps & bit) == 0) {
            this.bootstraps = bootstraps | bit;
            return true;
        }
        return false;
    }

    void buildLambdaBootstrap() {
        if (getAndSetBootstrap(Bootstrap.LAMBDA)) {
            staticMethod(
                    "defineLambdaCallSite",
                    MethodTypeDesc.of(
                            CD_CallSite,
                            CD_MethodHandles_Lookup,
                            CD_String,
                            CD_MethodType),
                    smc -> {
                        smc.setAccess(AccessLevel.PRIVATE);
                        ParamVar lookup = smc.parameter("lookup", 0);
                        ParamVar base64 = smc.parameter("base64", 1);
                        ParamVar methodType = smc.parameter("methodType", 2);
                        smc.body(b0 -> {
                            var decoder = b0.localVar("decoder", b0.invokeStatic(MD_Base64.getUrlDecoder));
                            var bytes = b0.localVar("bytes", b0.invokeVirtual(MD_Base64.Decoder.decode_1, decoder, base64));
                            var definedLookup = b0.localVar("definedLookup", b0.invokeVirtual(
                                    MD_MethodHandles.Lookup.defineHiddenClass,
                                    lookup,
                                    bytes,
                                    Const.of(false),
                                    b0.newArray(CD_MethodHandles_Lookup_ClassOption, Const.of(NESTMATE))));
                            var definedClass = b0.localVar("definedClass", b0.invokeVirtual(
                                    MD_MethodHandles.Lookup.lookupClass,
                                    definedLookup));
                            var ctorType = b0.localVar("ctorType", b0.invokeVirtual(
                                    MD_MethodType.changeReturnType,
                                    methodType,
                                    Const.of(void.class)));
                            var ctorHandle = b0.localVar("ctorHandle", b0.invokeVirtual(
                                    MD_MethodHandles.Lookup.findConstructor,
                                    definedLookup,
                                    definedClass,
                                    ctorType));
                            b0.ifElse(b0.eq(
                                    b0.invokeVirtual(MD_MethodType.parameterCount, methodType),
                                    0), t1 -> {
                                        LocalVar instance = t1.localVar("instance", t1.invokeVirtual(
                                                MD_invoke_returning_Object,
                                                ctorHandle));
                                        LocalVar constHandle = t1.localVar("constHandle", t1.invokeStatic(
                                                MD_MethodHandles.constant,
                                                definedClass,
                                                instance));
                                        t1.return_(t1.new_(CD_ConstantCallSite, t1.invokeVirtual(
                                                MD_MethodHandle.asType,
                                                constHandle,
                                                methodType)));
                                    }, f1 -> {
                                        f1.return_(f1.new_(CD_ConstantCallSite, f1.invokeVirtual(
                                                MD_MethodHandle.asType,
                                                ctorHandle,
                                                methodType)));
                                    });
                        });
                    });
        }
    }

    @Override
    public List<FieldDesc> staticFields() {
        return fields.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<FieldDesc> instanceFields() {
        return fields.entrySet().stream().filter(e -> !e.getValue()).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<MethodDesc> staticMethods() {
        return methods.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<MethodDesc> instanceMethods() {
        return methods.entrySet().stream().filter(e -> !e.getValue()).map(Map.Entry::getKey).toList();
    }

    @Override
    public List<ConstructorDesc> constructors() {
        return List.copyOf(constructors);
    }

    public ElementType annotationTargetType() {
        return ElementType.TYPE;
    }

    public GenericType.OfTypeVariable typeParameter(final String name, final Consumer<TypeParameterCreator> builder) {
        if (genericType != null) {
            throw new IllegalStateException("Type has already been established");
        }
        TypeParameterCreatorImpl creator = new TypeParameterCreatorImpl(name);
        builder.accept(creator);
        TypeParameter.OfType var = creator.forType(type());
        if (typeParameters instanceof ArrayList<TypeParameter> al) {
            al.add(var);
        } else {
            typeParameters = Util.listWith(typeParameters, var);
        }
        return var.genericType();
    }

    void buildReadLineBoostrapHelper() {
        if (getAndSetBootstrap(Bootstrap.READ_LINE)) {
            staticMethod("$readUtfLine", mc -> {
                mc.returning(CD_String);
                ParamVar sb = mc.parameter("sb", CD_StringBuilder);
                ParamVar is = mc.parameter("is", CD_InputStream);
                mc.body(b0 -> {
                    // first byte of the line
                    LocalVar a = b0.localVar("a", b0.invokeVirtual(MD_InputStream.read, is));
                    // EOF (expected)
                    b0.if_(b0.eq(a, -1), BlockCreator::returnNull);
                    b0.loop(b1 -> {
                        // process characters
                        b1.block(b2 -> {
                            // check for newline
                            b2.if_(b2.eq(a, '\n'), b3 -> {
                                // end of string
                                Expr toString = b3.withObject(sb).toString_();
                                // this is safe because this statement does not use or leave anything on the stack
                                b3.invokeVirtual(MD_StringBuilder.setLength, sb, Const.of(0));
                                b3.return_(toString);
                            });
                            // parse code point
                            b2.if_(b2.lt(a, 0x80), b3 -> {
                                // one-byte sequence
                                b3.invokeVirtual(MD_StringBuilder.append_char, sb, b3.cast(a, CD_char));
                                b3.break_(b2);
                            });
                            // validate prefix
                            b2.if_(b2.lt(a, 0xC0), b3 -> {
                                // invalid first byte (unexpected)
                                b3.throw_(CharConversionException.class);
                            });
                            // at least two bytes
                            LocalVar b = b2.localVar("b", b2.invokeVirtual(MD_InputStream.read, is));
                            // check for eof (unexpected)
                            b2.if_(b2.eq(b, -1), b3 -> {
                                // eof (unexpected)
                                b3.throw_(EOFException.class);
                            });
                            // validate second byte
                            b2.if_(b2.logicalOr(b2.lt(b, 0x80), b3 -> b3.yield(b3.ge(b, 0xC0))), b3 -> {
                                // invalid (unexpected)
                                b3.throw_(CharConversionException.class);
                            });
                            // test for two-byte sequence
                            b2.if_(b2.lt(a, 0xE0), b3 -> {
                                // two-byte sequence
                                b3.invokeVirtual(MD_StringBuilder.appendCodePoint, sb,
                                        b3.or(
                                                b3.shl(b3.and(a, 0x1F), 6),
                                                b3.and(b, 0x3F)));
                                b3.break_(b2);
                            });
                            // at least three bytes
                            LocalVar c = b2.localVar("c", b2.invokeVirtual(MD_InputStream.read, is));
                            // check for eof (unexpected)
                            b2.if_(b2.eq(c, -1), b3 -> {
                                // eof (unexpected)
                                b3.throw_(EOFException.class);
                            });
                            // validate third byte
                            b2.if_(b2.logicalOr(b2.lt(c, 0x80), b3 -> b3.yield(b3.ge(c, 0xC0))), b3 -> {
                                // invalid (unexpected)
                                b3.throw_(CharConversionException.class);
                            });
                            // test for three-byte sequence
                            b2.if_(b2.lt(a, 0xF0), b3 -> {
                                // three-byte sequence
                                b3.invokeVirtual(MD_StringBuilder.appendCodePoint, sb,
                                        b3.or(
                                                b3.or(
                                                        b3.shl(b3.and(a, 0x0F), 12),
                                                        b3.shl(b3.and(b, 0x3F), 6)),
                                                b3.and(c, 0x3F)));
                                b3.break_(b2);
                            });
                            // four bytes (or invalid)
                            LocalVar d = b2.localVar("d", b2.invokeVirtual(MD_InputStream.read, is));
                            // check for eof (unexpected)
                            b2.if_(b2.eq(d, -1), b3 -> {
                                // eof (unexpected)
                                b3.throw_(EOFException.class);
                            });
                            // validate fourth byte
                            b2.if_(b2.logicalOr(b2.lt(d, 0x80), b3 -> b3.yield(b3.ge(d, 0xC0))), b3 -> {
                                // invalid (unexpected)
                                b3.throw_(CharConversionException.class);
                            });
                            // test for four-byte sequence
                            b2.if_(b2.lt(a, 0xF8), b3 -> {
                                // four-byte sequence
                                b3.invokeVirtual(MD_StringBuilder.appendCodePoint, sb,
                                        b3.or(
                                                b3.or(
                                                        b3.shl(b3.and(a, 0x07), 18),
                                                        b3.shl(b3.and(b, 0x3F), 12)),
                                                b3.or(
                                                        b3.shl(b3.and(c, 0x0F), 6),
                                                        b3.and(d, 0x3F))));
                                b3.break_(b2);
                            });
                            // invalid sequence (unexpected)
                            b2.throw_(CharConversionException.class);
                        });
                        b1.set(a, b1.invokeVirtual(MD_InputStream.read, is));
                        b1.if_(b1.eq(a, -1), b2 -> {
                            b2.throw_(EOFException.class);
                        });
                    });
                });
            });
        }
    }

    void buildStringListConstantBootstrap() {
        buildReadLineBoostrapHelper();
        if (getAndSetBootstrap(Bootstrap.LIST_CONSTANT)) {
            staticMethod("loadStringListConstant", mc -> {
                mc.returning(CD_List);
                mc.parameter("lookup", CD_MethodHandles_Lookup);
                ParamVar name = mc.parameter("name", CD_String);
                ParamVar clazz = mc.parameter("type", CD_Class);
                mc.body(b0 -> {
                    // verify type
                    b0.if_(b0.ne(clazz, Const.of(CD_List)), b1 -> {
                        b1.throw_(ClassCastException.class);
                    });
                    // load resource
                    LocalVar is = b0.localVar("is", b0.invokeVirtual(
                            MD_Class.getResourceAsStream,
                            Const.of(type),
                            b0.withString(b0.withString(Const.of(type.displayName() + "$")).concat(name))
                                    .concat(Const.of(".txt"))));
                    b0.if_(b0.eq(is, Const.ofNull(CD_InputStream)), b1 -> {
                        b1.throw_(NoSuchElementException.class);
                    });
                    b0.autoClose(is, b1 -> {
                        // decode the bytes
                        LocalVar sb = b1.localVar("sb", b1.new_(CD_StringBuilder, Const.of(100)));
                        LocalVar list = b1.localVar("list", b1.new_(CD_ArrayList, Const.of(60)));
                        LocalVar line = b1.localVar("line", b1.invokeStatic(ClassMethodDesc.of(
                                type,
                                "$readUtfLine",
                                MethodTypeDesc.of(
                                        CD_String,
                                        CD_StringBuilder,
                                        CD_InputStream)),
                                sb, is));
                        b1.while_(b2 -> b2.yield(b2.isNotNull(line)), b2 -> {
                            b2.withCollection(list).add(line);
                            b2.set(line, b2.invokeStatic(ClassMethodDesc.of(
                                    type,
                                    "$readUtfLine",
                                    MethodTypeDesc.of(
                                            CD_String,
                                            CD_StringBuilder,
                                            CD_InputStream)),
                                    sb, is));
                        });
                        b1.return_(b1.invokeStatic(MD_List.copyOf, list));
                    });
                });
            });
        }
    }

    public Const stringListResourceConstant(String name, List<String> items) {
        buildStringListConstantBootstrap();
        byte[] array = encodeStrings(items.stream());
        output.write("%s$%s.txt".formatted(Util.internalName(type), name), array);
        DynamicConstantDesc<List<String>> stringListConstant = DynamicConstantDesc.ofNamed(
                ofConstantBootstrap(
                        type,
                        "loadStringListConstant",
                        CD_List),
                name,
                CD_List);
        return Const.of(stringListConstant);
    }

    void buildStringSetConstantBootstrap() {
        buildReadLineBoostrapHelper();
        if (getAndSetBootstrap(Bootstrap.SET_CONSTANT)) {
            staticMethod("loadStringSetConstant", mc -> {
                mc.returning(CD_Set);
                mc.parameter("lookup", CD_MethodHandles_Lookup);
                ParamVar name = mc.parameter("name", CD_String);
                ParamVar clazz = mc.parameter("type", CD_Class);
                mc.body(b0 -> {
                    // verify type
                    b0.if_(b0.ne(clazz, Const.of(CD_Set)), b1 -> {
                        b1.throw_(ClassCastException.class);
                    });
                    // load resource
                    LocalVar is = b0.localVar("is", b0.invokeVirtual(
                            MD_Class.getResourceAsStream,
                            Const.of(type),
                            b0.withString(b0.withString(Const.of(type.displayName() + "$")).concat(name))
                                    .concat(Const.of(".txt"))));
                    b0.if_(b0.eq(is, Const.ofNull(CD_InputStream)), b1 -> {
                        b1.throw_(NoSuchElementException.class);
                    });
                    b0.autoClose(is, b1 -> {
                        // decode the bytes
                        LocalVar sb = b1.localVar("sb", b1.new_(CD_StringBuilder, Const.of(100)));
                        LocalVar list = b1.localVar("list", b1.new_(CD_ArrayList, Const.of(60)));
                        LocalVar line = b1.localVar("line", b1.invokeStatic(ClassMethodDesc.of(
                                type,
                                "$readUtfLine",
                                MethodTypeDesc.of(
                                        CD_String,
                                        CD_StringBuilder,
                                        CD_InputStream)),
                                sb, is));
                        b1.while_(b2 -> b2.yield(b2.isNotNull(line)), b2 -> {
                            b2.withCollection(list).add(line);
                            b2.set(line, b2.invokeStatic(ClassMethodDesc.of(
                                    type,
                                    "$readUtfLine",
                                    MethodTypeDesc.of(
                                            CD_String,
                                            CD_StringBuilder,
                                            CD_InputStream)),
                                    sb, is));
                        });
                        b1.return_(b1.invokeStatic(MD_Set.copyOf, list));
                    });
                });
            });
        }
    }

    public Const stringSetResourceConstant(String name, Set<String> items) {
        buildStringSetConstantBootstrap();
        byte[] array = encodeStrings(items.stream());
        output.write("%s$%s.txt".formatted(Util.internalName(type), name), array);
        DynamicConstantDesc<Set<String>> stringSetConstant = DynamicConstantDesc.ofNamed(
                ofConstantBootstrap(
                        type,
                        "loadStringSetConstant",
                        CD_Set),
                name,
                CD_Set);
        return Const.of(stringSetConstant);
    }

    void buildStringMapConstantBootstrap() {
        buildReadLineBoostrapHelper();
        if (getAndSetBootstrap(Bootstrap.MAP_CONSTANT)) {
            staticMethod("loadStringMapConstant", mc -> {
                mc.returning(CD_Map);
                mc.parameter("lookup", CD_MethodHandles_Lookup);
                ParamVar name = mc.parameter("name", CD_String);
                ParamVar clazz = mc.parameter("type", CD_Class);
                mc.body(b0 -> {
                    // verify type
                    b0.if_(b0.ne(clazz, Const.of(CD_Map)), b1 -> {
                        b1.throw_(ClassCastException.class);
                    });
                    // load resource
                    LocalVar is = b0.localVar("is", b0.invokeVirtual(
                            MD_Class.getResourceAsStream,
                            Const.of(type),
                            b0.withString(b0.withString(Const.of(type.displayName() + "$")).concat(name))
                                    .concat(Const.of(".txt"))));
                    b0.if_(b0.eq(is, Const.ofNull(CD_InputStream)), b1 -> {
                        b1.throw_(NoSuchElementException.class);
                    });
                    b0.autoClose(is, b1 -> {
                        // decode the bytes
                        LocalVar sb = b1.localVar("sb", b1.new_(CD_StringBuilder, Const.of(100)));
                        LocalVar list = b1.localVar("list", b1.new_(CD_ArrayList, Const.of(60)));
                        LocalVar key = b1.localVar("key", b1.invokeStatic(ClassMethodDesc.of(
                                type,
                                "$readUtfLine",
                                MethodTypeDesc.of(CD_String, CD_StringBuilder, CD_InputStream)),
                                sb, is));
                        LocalVar value = b1.localVar("value", b1.invokeStatic(ClassMethodDesc.of(
                                type,
                                "$readUtfLine",
                                MethodTypeDesc.of(CD_String, CD_StringBuilder, CD_InputStream)),
                                sb, is));
                        b1.while_(b2 -> b2.yield(b2.isNotNull(key)), b2 -> {
                            b2.withCollection(list).add(b2.mapEntry(key, value));
                            b2.set(key, b2.invokeStatic(ClassMethodDesc.of(
                                    type,
                                    "$readUtfLine",
                                    MethodTypeDesc.of(CD_String, CD_StringBuilder, CD_InputStream)),
                                    sb, is));
                            b2.set(value, b2.invokeStatic(ClassMethodDesc.of(
                                    type,
                                    "$readUtfLine",
                                    MethodTypeDesc.of(CD_String, CD_StringBuilder, CD_InputStream)),
                                    sb, is));
                        });
                        Expr array = b1.newEmptyArray(CD_Map_Entry, b1.withList(list).size());
                        array = b1.invokeVirtual(ClassMethodDesc.of(
                                CD_ArrayList,
                                "toArray",
                                MethodTypeDesc.of(
                                        CD_Object.arrayType(),
                                        CD_Object.arrayType())),
                                list, array);
                        b1.return_(b1.invokeStatic(MD_Map.ofEntries, array));
                    });
                });
            });
        }
    }

    public Const stringMapResourceConstant(String name, Map<String, String> items) {
        buildStringMapConstantBootstrap();
        byte[] array = encodeStrings(items.entrySet().stream().flatMap(e -> Stream.of(e.getKey(), e.getValue())));
        output.write("%s$%s.txt".formatted(Util.internalName(type), name), array);
        DynamicConstantDesc<Map<String, String>> stringMapConstant = DynamicConstantDesc.ofNamed(
                ofConstantBootstrap(
                        type,
                        "loadStringMapConstant",
                        CD_Map),
                name,
                CD_Map);
        return Const.of(stringMapConstant);
    }

    private byte[] encodeStrings(final Stream<String> stream) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(8192)) {
            stream.forEachOrdered(s -> {
                int cp;
                for (int i = 0; i < s.length(); i += Character.charCount(cp)) {
                    cp = s.codePointAt(i);
                    // always use 2-byte encoding for \n so we can embed strings with newlines
                    // (but it still looks OK in editors)
                    if (cp <= 0x7f && cp != '\n') {
                        os.write(cp);
                    } else if (cp <= 0x7ff) {
                        os.write(0b110_00000 | cp >> 6);
                        os.write(0b10_000000 | cp & 0x3f);
                    } else if (cp <= 0xffff) {
                        os.write(0b1110_0000 | cp >> 12);
                        os.write(0b10_000000 | cp >> 6 & 0x3f);
                        os.write(0b10_000000 | cp & 0x3f);
                    } else if (cp <= 0x10ffff) {
                        os.write(0b11110_000 | cp >> 18);
                        os.write(0b10_000000 | cp >> 12 & 0x3f);
                        os.write(0b10_000000 | cp >> 6 & 0x3f);
                        os.write(0b10_000000 | cp & 0x3f);
                    } else {
                        throw new IllegalStateException("Unexpected invalid code point");
                    }
                }
                os.write('\n');
            });
            return os.toByteArray();
        } catch (IOException e) {
            // impossible?
            throw new IllegalStateException(e);
        }
    }

    private enum Bootstrap {
        LAMBDA,
        READ_LINE,
        LIST_CONSTANT,
        SET_CONSTANT,
        MAP_CONSTANT,
        ;
    }
}
