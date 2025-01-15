package io.quarkus.gizmo2.impl;

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
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Set;
import java.util.function.IntPredicate;

import io.github.dmlloyd.classfile.Signature;
import sun.reflect.ReflectionFactory;

public final class Util {
    private Util() {}

    private static final ClassValue<ClassDesc> constantCache = new ClassValue<ClassDesc>() {
        protected ClassDesc computeValue(final Class<?> type) {
            return type.describeConstable().orElseThrow(IllegalArgumentException::new);
        }
    };

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
        private final ReflectionFactory rf = ReflectionFactory.getReflectionFactory();

        protected MethodHandle computeValue(final Class<?> type) {
            return rf.writeReplaceForSerialization(type);
        }
    };

    public static SerializedLambda serializedLambda(final Serializable lambda) {
        MethodHandle handle = writeReplaces.get(lambda.getClass());
        if (handle == null) {
            throw new IllegalArgumentException("Cannot interpret method handle");
        }
        try {
            return (SerializedLambda) (Object) handle.invoke(lambda);
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
}
