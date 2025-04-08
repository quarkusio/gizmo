package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicConstantDesc;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.constant.ArrayVarHandleConstant;
import io.quarkus.gizmo2.impl.constant.ByteConstant;
import io.quarkus.gizmo2.impl.constant.CharConstant;
import io.quarkus.gizmo2.impl.constant.ClassConstant;
import io.quarkus.gizmo2.impl.constant.DoubleConstant;
import io.quarkus.gizmo2.impl.constant.DynamicConstant;
import io.quarkus.gizmo2.impl.constant.EnumConstant;
import io.quarkus.gizmo2.impl.constant.FieldVarHandleConstant;
import io.quarkus.gizmo2.impl.constant.FloatConstant;
import io.quarkus.gizmo2.impl.constant.IntConstant;
import io.quarkus.gizmo2.impl.constant.LongConstant;
import io.quarkus.gizmo2.impl.constant.NullConstant;
import io.quarkus.gizmo2.impl.constant.ShortConstant;
import io.quarkus.gizmo2.impl.constant.StaticFieldVarHandleConstant;
import io.quarkus.gizmo2.impl.constant.StaticFinalFieldConstant;
import io.quarkus.gizmo2.impl.constant.StringConstant;

public final class GizmoImpl implements Gizmo {
    private static final ThreadLocal<GizmoImpl> current = new ThreadLocal<>();
    private final ClassOutputImpl defaultOutput;

    public GizmoImpl(final BiConsumer<ClassDesc, byte[]> outputHandler) {
        defaultOutput = new ClassOutputImpl(this, outputHandler);
    }

    public static GizmoImpl current() {
        GizmoImpl current = GizmoImpl.current.get();
        if (current == null) {
            throw new IllegalStateException("No active Gizmo");
        }
        return current;
    }

    public ClassDesc class_(final ClassDesc desc, final Consumer<ClassCreator> builder) {
        return defaultOutput.class_(desc, builder);
    }

    public ClassDesc interface_(final ClassDesc desc, final Consumer<InterfaceCreator> builder) {
        return defaultOutput.interface_(desc, builder);
    }

    <T> void do_(T att, Consumer<T> task) {
        GizmoImpl old = current.get();
        if (old == this) {
            task.accept(att);
        } else {
            current.set(this);
            try {
                task.accept(att);
            } finally {
                if (old == null) {
                    current.remove();
                } else {
                    current.set(old);
                }
            }
        }
    }

    private final ConcurrentHashMap<FieldDesc, StaticFieldVarImpl> staticFields = new ConcurrentHashMap<>();

    public NullConstant nullConstant(ClassDesc type) {
        return new NullConstant(type);
    }

    public ClassConstant classConstant(ClassDesc value) {
        return new ClassConstant(value);
    }

    public StringConstant stringConstant(String value) {
        return new StringConstant(value);
    }

    public ByteConstant byteConstant(Byte value) {
        return new ByteConstant(value);
    }

    public ShortConstant shortConstant(Short value) {
        return new ShortConstant(value);
    }

    public CharConstant charConstant(Character value) {
        return new CharConstant(value);
    }

    public IntConstant intConstant(Integer value) {
        return new IntConstant(value);
    }

    public LongConstant longConstant(Long value) {
        return new LongConstant(value);
    }

    public FloatConstant floatConstant(float value) {
        return new FloatConstant(value);
    }

    public DoubleConstant doubleConstant(double value) {
        return new DoubleConstant(value);
    }

    public StaticFieldVar staticField(final FieldDesc desc) {
        return staticFields.computeIfAbsent(desc, StaticFieldVarImpl::new);
    }

    public StaticFinalFieldConstant staticFinalFieldConstant(final FieldDesc desc) {
        return new StaticFinalFieldConstant(desc);
    }

    public ClassOutput classOutput(final BiConsumer<ClassDesc, byte[]> outputHandler) {
        return new ClassOutputImpl(this, outputHandler);
    }

    public EnumConstant enumConstant(final Enum.EnumDesc<?> value) {
        return new EnumConstant(value);
    }

    public DynamicConstant dynamicConstant(final DynamicConstantDesc<?> value) {
        return new DynamicConstant(value);
    }

    public FieldVarHandleConstant fieldVarHandleConstant(final FieldDesc desc) {
        return new FieldVarHandleConstant(desc);
    }

    public StaticFieldVarHandleConstant staticFieldVarHandleConstant(final FieldDesc desc) {
        return new StaticFieldVarHandleConstant(desc);
    }

    public ArrayVarHandleConstant arrayVarHandleConstant(final ClassDesc arrayType) {
        return new ArrayVarHandleConstant(arrayType);
    }
}
