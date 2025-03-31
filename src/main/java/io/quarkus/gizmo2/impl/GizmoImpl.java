package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.impl.constant.ArrayVarHandleConstant;
import io.quarkus.gizmo2.impl.constant.ByteConstant;
import io.quarkus.gizmo2.impl.constant.CharConstant;
import io.quarkus.gizmo2.impl.constant.ClassConstant;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;
import io.quarkus.gizmo2.impl.constant.DoubleConstant;
import io.quarkus.gizmo2.impl.constant.DynamicConstant;
import io.quarkus.gizmo2.impl.constant.EnumConstant;
import io.quarkus.gizmo2.impl.constant.FloatConstant;
import io.quarkus.gizmo2.impl.constant.IntConstant;
import io.quarkus.gizmo2.impl.constant.LongConstant;
import io.quarkus.gizmo2.impl.constant.NullConstant;
import io.quarkus.gizmo2.impl.constant.ShortConstant;
import io.quarkus.gizmo2.impl.constant.StaticFieldVarHandleConstant;
import io.quarkus.gizmo2.impl.constant.StaticFinalFieldConstant;
import io.quarkus.gizmo2.impl.constant.StringConstant;
import io.quarkus.gizmo2.impl.constant.FieldVarHandleConstant;

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

    private final ConcurrentHashMap<ClassDesc, NullConstant> nullConstants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ConstantDesc, ConstantImpl> constants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ConstantImpl, ConstantImpl> funnyHashCodeConstants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<FieldDesc, StaticFieldVarImpl> staticFields = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<FieldDesc, StaticFinalFieldConstant> staticFinalFieldConstants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<FieldDesc, StaticFieldVarHandleConstant> staticFieldVarHandleConstants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<FieldDescImpl, FieldDescImpl> fieldDescs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<FieldDesc, FieldVarHandleConstant> fieldVarHandleConstants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<InterfaceMethodDescImpl, InterfaceMethodDescImpl> interfaceMethodDescs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ClassMethodDescImpl, ClassMethodDescImpl> classMethodDescs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ClassDesc, ArrayVarHandleConstant> arrayVarHandleConstants = new ConcurrentHashMap<>();

    public NullConstant nullConstant(ClassDesc type) {
        return nullConstants.computeIfAbsent(type, NullConstant::new);
    }

    public ClassConstant classConstant(ClassDesc value) {
        return (ClassConstant) constants.computeIfAbsent(value, ClassConstant::new);
    }

    public StringConstant stringConstant(String value) {
        return (StringConstant) constants.computeIfAbsent(value, StringConstant::new);
    }

    public ByteConstant byteConstant(Byte value) {
        return (ByteConstant) funnyHashCodeConstants.computeIfAbsent(new ByteConstant(value), Function.identity());
    }

    public ShortConstant shortConstant(Short value) {
        return (ShortConstant) funnyHashCodeConstants.computeIfAbsent(new ShortConstant(value), Function.identity());
    }

    public CharConstant charConstant(Character value) {
        return (CharConstant) funnyHashCodeConstants.computeIfAbsent(new CharConstant(value), Function.identity());
    }

    public IntConstant intConstant(Integer value) {
        return (IntConstant) constants.computeIfAbsent(value, IntConstant::new);
    }

    public LongConstant longConstant(Long value) {
        return (LongConstant) constants.computeIfAbsent(value, LongConstant::new);
    }

    public FloatConstant floatConstant(float value) {
        return (FloatConstant) funnyHashCodeConstants.computeIfAbsent(new FloatConstant(value), Function.identity());
    }

    public DoubleConstant doubleConstant(double value) {
        return (DoubleConstant) funnyHashCodeConstants.computeIfAbsent(new DoubleConstant(value), Function.identity());
    }

    public StaticFieldVar staticField(final FieldDesc desc) {
        return staticFields.computeIfAbsent(desc, StaticFieldVarImpl::new);
    }

    public StaticFinalFieldConstant staticFinalFieldConstant(final FieldDesc desc) {
        return staticFinalFieldConstants.computeIfAbsent(desc, StaticFinalFieldConstant::new);
    }

    public FieldDesc fieldDesc(final ClassDesc owner, final String name, final ClassDesc type) {
        return fieldDescs.computeIfAbsent(new FieldDescImpl(owner, name, type), Function.identity());
    }

    public InterfaceMethodDesc interfaceMethodDesc(final ClassDesc owner, final String name, final MethodTypeDesc type) {
        return interfaceMethodDescs.computeIfAbsent(new InterfaceMethodDescImpl(owner, name, type), Function.identity());
    }

    public ClassMethodDesc classMethodDesc(final ClassDesc owner, final String name, final MethodTypeDesc type) {
        return classMethodDescs.computeIfAbsent(new ClassMethodDescImpl(owner, name, type), Function.identity());
    }

    public ClassOutput classOutput(final BiConsumer<ClassDesc, byte[]> outputHandler) {
        return new ClassOutputImpl(this, outputHandler);
    }

    public EnumConstant enumConstant(final Enum.EnumDesc<?> value) {
        return (EnumConstant) funnyHashCodeConstants.computeIfAbsent(new EnumConstant(value), Function.identity());
    }

    public DynamicConstant dynamicConstant(final DynamicConstantDesc<?> value) {
        return (DynamicConstant) constants.computeIfAbsent(value, DynamicConstant::new);
    }

    public FieldVarHandleConstant fieldVarHandleConstant(final FieldDesc desc) {
        return fieldVarHandleConstants.computeIfAbsent(desc, FieldVarHandleConstant::new);
    }

    public StaticFieldVarHandleConstant staticFieldVarHandleConstant(final FieldDesc desc) {
        return staticFieldVarHandleConstants.computeIfAbsent(desc, StaticFieldVarHandleConstant::new);
    }

    public ArrayVarHandleConstant arrayVarHandleConstant(final ClassDesc arrayType) {
        return arrayVarHandleConstants.computeIfAbsent(arrayType, ArrayVarHandleConstant::new);
    }
}
