package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.desc.MethodDesc;
import sun.reflect.ReflectionFactory;

public final class Util {
    /**
     * No descriptors array. Prevents ambiguity in cases where a non-varargs overload is e.g. {@code @since 21} or later.
     */
    public static final ClassDesc[] NO_DESCS = new ClassDesc[0];

    // set system property means enabled, even with an empty value, except if the value is `false`
    private static final boolean trackingEnabled = !"false".equals(System.getProperty("gizmo.enableTracking", "false"));

    public static final String trackingMessage = "\nTo track callers and get an improved exception message, add the system property `gizmo.enableTracking`";

    private Util() {
    }

    private static final ClassValue<ClassDesc> constantCache = new ClassValue<ClassDesc>() {
        protected ClassDesc computeValue(final Class<?> type) {
            return type.describeConstable().orElseThrow(IllegalArgumentException::new);
        }
    };

    public static StringBuilder descName(StringBuilder b, ClassDesc desc) {
        if (desc.packageName().isEmpty()) {
            return b.append(desc.displayName());
        } else {
            return b.append(desc.packageName()).append('.').append(desc.displayName());
        }
    }

    public static ClassDesc classDesc(Class<?> clazz) {
        return constantCache.get(clazz);
    }

    private static final MethodHandle actualKind;

    static {
        try {
            actualKind = MethodHandles.privateLookupIn(TypeKind.class, MethodHandles.lookup()).findGetter(TypeKind.class,
                    "actualKind", io.github.dmlloyd.classfile.TypeKind.class);
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
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
        return trackingEnabled ? callerOutsideGizmo() : null;
    }

    private static String callerOutsideGizmo() {
        return SW.walk(stream -> stream
                .filter(it -> !it.getClassName().startsWith("io.quarkus.gizmo2") || it.getClassName().endsWith("Test"))
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

    private static final ClassValue<MethodDesc> samCache = new ClassValue<MethodDesc>() {
        protected MethodDesc computeValue(final Class<?> type) {
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
                throw new IllegalArgumentException("No SAM found on " + type);
            }
            return MethodDesc.of(sam.getDeclaringClass(), sam.getName(), sam.getReturnType(), sam.getParameterTypes());
        }
    };

    public static MethodDesc findSam(final Class<?> type) {
        return samCache.get(type);
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
}
