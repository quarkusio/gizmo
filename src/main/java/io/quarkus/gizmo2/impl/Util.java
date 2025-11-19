package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.io.Serializable;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.AnnotationElement;
import io.github.dmlloyd.classfile.AnnotationValue;
import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.TypeArgument;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.TypeParameter;
import io.quarkus.gizmo2.desc.Descs;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.smallrye.common.constraint.Assert;
import sun.reflect.ReflectionFactory;

public final class Util {
    /**
     * No descriptors array. Prevents ambiguity in cases where a non-varargs overload is e.g. {@code @since 21} or later.
     */
    public static final ClassDesc[] NO_DESCS = new ClassDesc[0];

    // set system property means enabled, even with an empty value, except if the value is `false`
    public static final boolean debug = !"false".equals(System.getProperty("gizmo.debug", "false"));

    public static final String trackingMessage = "\nTo track callers and get an improved exception message, add the system property `gizmo.debug`";

    private Util() {
    }

    private static final ClassValue<ClassDesc> constantCache = new ClassValue<ClassDesc>() {
        protected ClassDesc computeValue(final Class<?> type) {
            return Uncached.classDesc(type);
        }
    };

    private static final Map<Class<?>, ClassDesc> fastCache;

    public static StringBuilder descName(StringBuilder b, ClassDesc desc) {
        if (desc.packageName().isEmpty()) {
            return b.append(desc.displayName());
        } else {
            return b.append(desc.packageName()).append('.').append(desc.displayName());
        }
    }

    public static ClassDesc classDesc(Class<?> clazz) {
        ClassDesc desc = fastCache.get(clazz);
        if (desc == null) {
            desc = constantCache.get(clazz);
        }
        return desc;
    }

    /**
     * Separate class for uncached class desc creation to avoid class init loops.
     */
    public static final class Uncached {
        private Uncached() {
        }

        public static ClassDesc classDesc(Class<?> clazz) {
            return clazz.describeConstable().orElseThrow(IllegalArgumentException::new);
        }
    }

    static {
        HashMap<Class<?>, ClassDesc> map = new HashMap<>();
        for (Class<?> owner : List.of(ConstantDescs.class, Descs.class)) {
            Field[] fields = owner.getFields();
            for (Field field : fields) {
                if (field.getType() == ClassDesc.class) {
                    int mods = field.getModifiers();
                    if (Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
                        ClassDesc desc = readDescField(field);
                        map.putIfAbsent(doLoad(desc), desc);
                    }
                }
            }
        }
        fastCache = Map.copyOf(map);
    }

    private static ClassDesc readDescField(Field f) {
        try {
            return (ClassDesc) f.get(null);
        } catch (IllegalAccessException e) {
            IllegalAccessError error = new IllegalAccessError(e.getMessage());
            error.setStackTrace(e.getStackTrace());
            throw error;
        }
    }

    private static Class<?> doLoad(ClassDesc desc) {
        if (desc.isClassOrInterface()) {
            String ds = desc.descriptorString();
            try {
                return Class.forName(ds.substring(1, ds.length() - 1).replace('/', '.'), false, Util.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                NoClassDefFoundError error = new NoClassDefFoundError(e.getMessage());
                error.setStackTrace(e.getStackTrace());
                throw error;
            }
        } else if (desc.isArray()) {
            return doLoad(desc.componentType()).arrayType();
        } else {
            assert desc.isPrimitive();
            return switch (desc.descriptorString().charAt(0)) {
                case 'B' -> byte.class;
                case 'C' -> char.class;
                case 'D' -> double.class;
                case 'F' -> float.class;
                case 'I' -> int.class;
                case 'J' -> long.class;
                case 'S' -> short.class;
                case 'Z' -> boolean.class;
                case 'V' -> void.class;
                default -> throw Assert.impossibleSwitchCase(desc.descriptorString());
            };
        }
    }

    public static boolean equals(ClassDesc a, ClassDesc b) {
        return a == b || a != null && b != null && a.descriptorString().equals(b.descriptorString());
    }

    private static final MethodHandle actualKind;
    private static final MethodHandle GenericType_computeAnnotations;
    private static final MethodHandle TypeParameter_computeAnnotations;

    static {
        try {
            actualKind = MethodHandles.privateLookupIn(TypeKind.class, MethodHandles.lookup()).findGetter(TypeKind.class,
                    "actualKind", io.github.dmlloyd.classfile.TypeKind.class);
            MethodHandles.Lookup genericTypeLookup = MethodHandles.privateLookupIn(GenericType.class, MethodHandles.lookup());
            GenericType_computeAnnotations = genericTypeLookup.findVirtual(
                    GenericType.class, "computeAnnotations", MethodType.methodType(
                            List.class,
                            RetentionPolicy.class,
                            TypeAnnotation.TargetInfo.class,
                            ArrayList.class,
                            ArrayDeque.class));
            TypeParameter_computeAnnotations = genericTypeLookup.findVirtual(
                    TypeParameter.class, "computeAnnotations", MethodType.methodType(
                            List.class,
                            RetentionPolicy.class,
                            TypeAnnotation.TargetInfo.class,
                            ArrayList.class,
                            ArrayDeque.class));
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public static io.github.dmlloyd.classfile.TypeKind actualKindOf(TypeKind kind) {
        try {
            return (io.github.dmlloyd.classfile.TypeKind) actualKind.invokeExact(kind);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }

    private static final ClassValue<MethodHandle> writeReplaces = new ClassValue<MethodHandle>() {
        private static final ReflectionFactory rf = ReflectionFactory.getReflectionFactory();

        protected MethodHandle computeValue(final Class<?> type) {
            MethodHandle base = rf.writeReplaceForSerialization(type);
            return base == null ? null : base.asType(MethodType.methodType(SerializedLambda.class, Serializable.class));
        }
    };

    public static SerializedLambda serializedLambda(final Serializable lambda) {
        MethodHandle handle = writeReplaces.get(lambda.getClass());
        if (handle == null) {
            throw new IllegalArgumentException("Cannot interpret method handle");
        }
        try {
            return (SerializedLambda) handle.invokeExact(lambda);
        } catch (Error | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private static final StackWalker SW = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE,
            StackWalker.Option.SHOW_HIDDEN_FRAMES, StackWalker.Option.SHOW_REFLECT_FRAMES));

    public static String detailedStackTrace() {
        StringBuilder b = new StringBuilder(1000);
        SW.walk(stream -> {
            stream.forEachOrdered(
                    fr -> b.append("at ").append(fr.toStackTraceElement()).append(" bci=").append(fr.getByteCodeIndex())
                            .append('\n'));
            return null;
        });
        return b.toString();
    }

    public static String trackCaller() {
        return debug ? callerOutsideGizmo() : null;
    }

    private static String callerOutsideGizmo() {
        return SW.walk(stream -> stream
                .filter(it -> !it.getClassName().startsWith("io.quarkus.gizmo2")
                        && !it.getClassName().startsWith("io.github.dmlloyd.classfile")
                        || it.getClassName().endsWith("Test"))
                .findFirst()
                .map(it -> it.getClassName() + "." + it.getMethodName()
                        + "(" + it.getFileName() + ":" + it.getLineNumber() + ")")
                .orElseThrow(IllegalStateException::new));
    }

    /**
     * Append a value to an immutable list without unneeded allocations.
     * If the list length is greater than some threshold, return an {@code ArrayList}
     * containing the original list, the addend, and some extra space for new values.
     *
     * @param original the original list (must not be {@code null})
     * @param addend the value to add (must not be {@code null})
     * @return the new list (not {@code null})
     * @param <T> the value type
     */
    public static <T> List<T> listWith(List<T> original, T addend) {
        assert !(original instanceof ArrayList<?>);
        // this is a no-op if nobody did anything wrong
        original = List.copyOf(original);
        return switch (original.size()) {
            case 0 -> List.of(addend);
            case 1 -> List.of(original.get(0), addend);
            case 2 -> List.of(original.get(0), original.get(1), addend);
            default -> {
                var a = new ArrayList<T>();
                a.addAll(original);
                a.add(addend);
                yield a;
            }
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, R> List<R> reinterpretCast(List<T> list) {
        return (List) list;
    }

    public static <K, V> Map<V, K> reverseMap(Map<K, V> original) {
        return original.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                Map.Entry::getValue,
                Map.Entry::getKey));
    }

    public static ClassDesc erased(Signature sig) {
        if (sig instanceof Signature.ClassTypeSig cts) {
            return cts.classDesc();
        } else if (sig instanceof Signature.ArrayTypeSig ats) {
            return erased(ats.componentSignature()).arrayType();
        } else if (sig instanceof Signature.BaseTypeSig bts) {
            return switch (bts.baseType()) {
                case 'B' -> CD_byte;
                case 'C' -> CD_char;
                case 'D' -> CD_double;
                case 'F' -> CD_float;
                case 'I' -> CD_int;
                case 'J' -> CD_long;
                case 'S' -> CD_short;
                case 'V' -> CD_void;
                case 'Z' -> CD_boolean;
                default -> throw new IllegalArgumentException(bts.toString());
            };
        } else if (sig instanceof Signature.TypeVarSig) {
            return CD_Object;
        } else {
            throw new IllegalArgumentException(sig.toString());
        }
    }

    private static final ClassValue<Method> samCache = new ClassValue<Method>() {
        protected Method computeValue(final Class<?> type) {
            // this is a slow and expensive operation, but there seems to be no way around it.
            Method sam = null;
            for (Method method : type.getMethods()) {
                int mods = method.getModifiers();
                if (Modifier.isAbstract(mods) && Modifier.isPublic(mods) && !Modifier.isStatic(mods)) {
                    if (sam == null) {
                        sam = method;
                    } else {
                        throw new IllegalArgumentException(
                                "Found two abstract methods on " + type + ": " + sam.getName() + " and " + method.getName());
                    }
                }
            }
            if (sam == null) {
                throw new IllegalArgumentException("No abstract methods were found on " + type);
            }
            return sam;
        }
    };

    public static MethodDesc findSam(final Class<?> type) {
        return MethodDesc.of(samCache.get(type));
    }

    public static String internalName(final ClassDesc desc) {
        if (desc.isClassOrInterface()) {
            String ds = desc.descriptorString();
            return ds.substring(1, ds.length() - 1);
        } else {
            return desc.descriptorString();
        }
    }

    public static String binaryName(final ClassDesc desc) {
        return internalName(desc).replace('/', '.');
    }

    public static StringBuilder appendAnnotation(final StringBuilder b, final Annotation annotation) {
        b.append('@');
        b.append(binaryName(annotation.classSymbol()));
        List<AnnotationElement> elements = annotation.elements();
        if (!elements.isEmpty()) {
            b.append('(');
            if (elements.size() == 1 && elements.get(0).name().equalsString("value")) {
                appendAnnotationValue(b, elements.get(0).value());
            } else {
                Iterator<AnnotationElement> iter = elements.iterator();
                assert iter.hasNext();
                AnnotationElement entry = iter.next();
                appendAnnotationValue(b.append(entry.name().stringValue()).append('='), entry.value());
                while (iter.hasNext()) {
                    entry = iter.next();
                    appendAnnotationValue(b.append(',').append(entry.name().stringValue()).append('='), entry.value());
                }
            }
            b.append(')');
        }
        return b;
    }

    public static void appendAnnotationValue(final StringBuilder b, final AnnotationValue value) {
        switch (value.tag()) {
            case AnnotationValue.TAG_BYTE -> b.append(((AnnotationValue.OfByte) value).byteValue());
            case AnnotationValue.TAG_CHAR -> b.append('\'').append(((AnnotationValue.OfChar) value).charValue()).append('\'');
            case AnnotationValue.TAG_SHORT -> b.append(((AnnotationValue.OfShort) value).shortValue());
            case AnnotationValue.TAG_INT -> b.append(((AnnotationValue.OfInt) value).intValue());
            case AnnotationValue.TAG_LONG -> b.append(((AnnotationValue.OfLong) value).longValue()).append('L');
            case AnnotationValue.TAG_FLOAT -> b.append(((AnnotationValue.OfFloat) value).floatValue()).append('F');
            case AnnotationValue.TAG_DOUBLE -> b.append(((AnnotationValue.OfDouble) value).doubleValue());
            case AnnotationValue.TAG_BOOLEAN -> b.append(((AnnotationValue.OfBoolean) value).booleanValue());
            case AnnotationValue.TAG_STRING ->
                b.append('"').append(((AnnotationValue.OfString) value).stringValue()).append('"');
            case AnnotationValue.TAG_ENUM -> {
                AnnotationValue.OfEnum ofEnum = (AnnotationValue.OfEnum) value;
                b.append(binaryName(ofEnum.classSymbol())).append('.').append(ofEnum.constantName().stringValue());
            }
            case AnnotationValue.TAG_ARRAY -> {
                b.append('{');
                final Iterator<AnnotationValue> iterator = ((AnnotationValue.OfArray) value).values().iterator();
                if (iterator.hasNext()) {
                    AnnotationValue nested = iterator.next();
                    appendAnnotationValue(b, nested);
                    while (iterator.hasNext()) {
                        b.append(',');
                        nested = iterator.next();
                        appendAnnotationValue(b, nested);
                    }
                }
                b.append('}');
            }
            case AnnotationValue.TAG_CLASS -> b.append(binaryName(((AnnotationValue.OfClass) value).classSymbol()));
            case AnnotationValue.TAG_ANNOTATION -> appendAnnotation(b, ((AnnotationValue.OfAnnotation) value).annotation());
            default -> throw Assert.impossibleSwitchCase(value.tag());
        }
    }

    // Generic type signature mapping

    private static final Map<String, Signature.BaseTypeSig> baseTypeSigs = Stream.of(
            CD_boolean,
            CD_byte,
            CD_short,
            CD_char,
            CD_int,
            CD_long,
            CD_float,
            CD_double,
            CD_void).collect(Collectors.toUnmodifiableMap(ClassDesc::descriptorString, Signature.BaseTypeSig::of));

    public static Signature signatureOf(GenericType type) {
        if (type instanceof GenericType.OfPrimitive prim) {
            return signatureOf(prim);
        } else if (type instanceof GenericType.OfReference ref) {
            return signatureOf(ref);
        } else {
            throw invalidType(type);
        }
    }

    public static Signature.BaseTypeSig signatureOf(GenericType.OfPrimitive type) {
        return baseTypeSigs.get(type.desc().descriptorString());
    }

    public static Signature.RefTypeSig signatureOf(GenericType.OfReference type) {
        if (type instanceof GenericType.OfArray array) {
            return signatureOf(array);
        } else if (type instanceof GenericType.OfThrows ot) {
            return signatureOf(ot);
        } else {
            throw invalidType(type);
        }
    }

    public static Signature.ArrayTypeSig signatureOf(GenericType.OfArray type) {
        return Signature.ArrayTypeSig.of(1, signatureOf(type.componentType()));
    }

    public static Signature.RefTypeSig signatureOf(GenericType.OfThrows type) {
        if (type instanceof GenericType.OfClass oc) {
            return signatureOf(oc);
        } else if (type instanceof GenericType.OfTypeVariable tv) {
            return signatureOf(tv);
        } else {
            throw invalidType(type);
        }
    }

    public static Signature.ClassTypeSig signatureOf(GenericType.OfClass type) {
        if (type instanceof GenericType.OfRootClass oc) {
            return signatureOf(oc);
        } else if (type instanceof GenericType.OfInnerClass ic) {
            return signatureOf(ic);
        } else {
            throw invalidType(type);
        }
    }

    public static Signature.ClassTypeSig signatureOf(GenericType.OfRootClass type) {
        return Signature.ClassTypeSig.of(type.desc(), typeArgsOf(type.typeArguments()));
    }

    public static Signature.ClassTypeSig signatureOf(GenericType.OfInnerClass type) {
        return Signature.ClassTypeSig.of(signatureOf(type.outerType()), type.name(), typeArgsOf(type.typeArguments()));
    }

    public static Signature.TypeVarSig signatureOf(GenericType.OfTypeVariable type) {
        return Signature.TypeVarSig.of(type.name());
    }

    // Generic type argument mapping

    public static Signature.TypeArg[] typeArgsOf(final List<TypeArgument> typeArguments) {
        return typeArguments.stream().map(Util::typeArgOf).toArray(Signature.TypeArg[]::new);
    }

    public static Signature.TypeArg typeArgOf(TypeArgument arg) {
        if (arg instanceof TypeArgument.OfExact ex) {
            return typeArgOf(ex);
        } else if (arg instanceof TypeArgument.OfWildcard wld) {
            return typeArgOf(wld);
        } else {
            throw invalidType(arg);
        }
    }

    public static Signature.TypeArg.Bounded typeArgOf(TypeArgument.OfExact arg) {
        return Signature.TypeArg.of(signatureOf(arg.type()));
    }

    public static Signature.TypeArg typeArgOf(TypeArgument.OfWildcard arg) {
        if (arg instanceof TypeArgument.OfExtends oe) {
            return typeArgOf(oe);
        } else if (arg instanceof TypeArgument.OfSuper os) {
            return typeArgOf(os);
        } else if (arg instanceof TypeArgument.OfUnbounded wc) {
            return typeArgOf(wc);
        } else {
            throw invalidType(arg);
        }
    }

    public static Signature.TypeArg.Bounded typeArgOf(TypeArgument.OfExtends arg) {
        return Signature.TypeArg.extendsOf(signatureOf(arg.bound()));
    }

    public static Signature.TypeArg.Bounded typeArgOf(TypeArgument.OfSuper arg) {
        return Signature.TypeArg.superOf(signatureOf(arg.bound()));
    }

    public static Signature.TypeArg.Unbounded typeArgOf(TypeArgument.OfUnbounded arg) {
        return Signature.TypeArg.unbounded();
    }

    public static Signature.TypeParam typeParamOf(TypeParameter tp) {
        return Signature.TypeParam.of(
                tp.name(),
                tp.firstBound().map(Util::signatureOf),
                tp.otherBounds().stream().map(Util::signatureOf)
                        .toArray(Signature.RefTypeSig[]::new));
    }

    public static List<TypeAnnotation> computeAnnotations(GenericType type, RetentionPolicy retention,
            TypeAnnotation.TargetInfo targetInfo,
            ArrayList<TypeAnnotation> list, ArrayDeque<TypeAnnotation.TypePathComponent> path) {
        try {
            var ignored = (List<?>) GenericType_computeAnnotations.invokeExact(
                    type, retention, targetInfo, list, path);
            return list;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public static List<TypeAnnotation> computeAnnotations(TypeParameter tp, RetentionPolicy retention,
            TypeAnnotation.TargetInfo targetInfo,
            ArrayList<TypeAnnotation> list, ArrayDeque<TypeAnnotation.TypePathComponent> path) {
        try {
            var ignored = (List<?>) TypeParameter_computeAnnotations.invokeExact(
                    tp, retention, targetInfo, list, path);
            return list;
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private static IllegalArgumentException invalidType(final Object type) {
        return new IllegalArgumentException(type.getClass().toString());
    }

    public static boolean isVoid(ClassDesc type) {
        return "V".equals(type.descriptorString());
    }

    /**
     * Assert that the next item is the given item,
     * <em>and</em> set up the iterator so that its {@code remove()/set()} methods
     * will target the given item.
     *
     * @param itr the list iterator (must not be {@code null})
     * @param item the item to check (must not be {@code null})
     */
    public static void ensureBefore(ListIterator<Item> itr, Item item) {
        assert itr.hasNext();
        Item check = itr.next();
        assert item.equals(check);
        itr.previous();
    }

    /**
     * Assert that the previous item is the given item,
     * <em>and</em> set up the iterator so that its {@code remove()/set()} methods
     * will target the given item.
     *
     * @param itr the list iterator (must not be {@code null})
     * @param item the item to check (must not be {@code null})
     */
    public static void ensureAfter(ListIterator<Item> itr, final Item item) {
        assert itr.hasPrevious();
        Item check = itr.previous();
        assert item.equals(check);
        itr.next();
    }

    /**
     * Peek at the next item for this iterator,
     * <em>and</em> set up the iterator so that its {@code remove()/set()} methods
     * will target the returned item.
     *
     * @param itr the list iterator (must not be {@code null})
     * @return the next item (not {@code null})
     */
    public static Item peekNext(ListIterator<Item> itr) {
        assert itr.hasNext();
        itr.next();
        return itr.previous();
    }

    /**
     * Peek at the previous item for this iterator,
     * <em>and</em> set up the iterator so that its {@code remove()/set()} methods
     * will target the returned item.
     *
     * @param itr the list iterator (must not be {@code null})
     * @return the previous item (not {@code null})
     */
    public static Item peekPrevious(ListIterator<Item> itr) {
        assert itr.hasPrevious();
        itr.previous();
        return itr.next();
    }
}
