package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;

import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.desc.MethodDesc;
import sun.reflect.ReflectionFactory;

public final class Util {
    private Util() {}

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

    public static ClassDesc classDesc(Signature sig) {
        if (sig instanceof Signature.ArrayTypeSig ats) {
            return classDesc(ats.componentSignature()).arrayType();
        } else if (sig instanceof Signature.ClassTypeSig cts) {
            return cts.classDesc();
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
                default -> throw new IllegalStateException();
            };
        } else if (sig instanceof Signature.TypeVarSig) {
            return ConstantDescs.CD_Object;
        } else {
            throw new IllegalArgumentException("Unknown signature type: " + sig);
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

    private static final StackWalker SW = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE, StackWalker.Option.SHOW_HIDDEN_FRAMES, StackWalker.Option.SHOW_REFLECT_FRAMES));

    public static String detailedStackTrace() {
        StringBuilder b = new StringBuilder(1000);
        SW.walk(stream -> {
            stream.forEachOrdered(
                fr -> b.append("at ").append(fr.toStackTraceElement()).append(" bci=").append(fr.getByteCodeIndex()).append('\n')
            );
            return null;
        });
        return b.toString();
    }

    // TODO: move to using smallrye-common-search
    public static int binarySearch(int from, int to, IntPredicate test) {
        int low = from;
        int high = to - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (test.test(mid)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return low;
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
        assert ! (original instanceof ArrayList<?>);
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

    public static MethodDesc findSam(final Class<?> type) {
        // this is a slow and expensive operation, but there seems to be no way around it.
        Method sam = null;
        for (Method method : type.getMethods()) {
            int mods = method.getModifiers();
            if (Modifier.isAbstract(mods) && Modifier.isPublic(mods) && ! Modifier.isStatic(mods)) {
                if (sam == null) {
                    sam = method;
                } else {
                    throw new IllegalArgumentException("Found two abstract methods on " + type + ": " + sam.getName() + " and " + method.getName());
                }
            }
        }
        if (sam == null) {
            throw new IllegalArgumentException("No SAM found on " + type);
        }
        return MethodDesc.of(sam.getDeclaringClass(), sam.getName(), sam.getReturnType(), sam.getParameterTypes());
    }
}
