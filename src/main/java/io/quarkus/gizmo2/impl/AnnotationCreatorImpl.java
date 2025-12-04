package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.smallrye.classfile.AnnotationElement;
import io.smallrye.classfile.AnnotationValue;

public final class AnnotationCreatorImpl<A extends Annotation> implements AnnotationCreator<A> {
    /**
     * Factory for creating annotation instances.
     *
     * @param type the annotation class (must not be {@code null})
     * @param builder the builder for the annotation contents (must not be {@code null})
     * @return the constructed annotation (not {@code null})
     * @param <A> the annotation type
     */
    static <A extends Annotation> io.smallrye.classfile.Annotation makeAnnotation(Class<A> type,
            Consumer<AnnotationCreator<A>> builder) {
        checkNotNullParam("type", type);
        checkNotNullParam("builder", builder);

        AnnotationCreatorImpl<A> creator = new AnnotationCreatorImpl<>(type);
        builder.accept(creator);
        return io.smallrye.classfile.Annotation.of(Util.classDesc(type), creator.elements);
    }

    /**
     * Factory for creating annotation instances.
     *
     * @param type the annotation class (must not be {@code null})
     * @param builder the builder for the annotation contents (must not be {@code null})
     * @return the constructed annotation (not {@code null})
     */
    static io.smallrye.classfile.Annotation makeAnnotation(ClassDesc type,
            Consumer<AnnotationCreator<Annotation>> builder) {
        checkNotNullParam("type", type);
        checkNotNullParam("builder", builder);

        AnnotationCreatorImpl<Annotation> creator = new AnnotationCreatorImpl<>(null);
        builder.accept(creator);
        return io.smallrye.classfile.Annotation.of(type, creator.elements);
    }

    // The `annotationClass` field is `null` when the `ClassDesc` variant of `makeAnnotation()`
    // was used. That is OK, because the `annotationClass` is only consulted when using
    // method references to identify annotation elements, and that's only doable when
    // the `Class` variant of `makeAnnotation()` was used.
    private final Class<A> annotationClass;
    private final List<AnnotationElement> elements = new ArrayList<>();

    AnnotationCreatorImpl(Class<A> annotationClass) {
        this.annotationClass = annotationClass;
    }

    /**
     * Add an annotation element to the annotation.
     *
     * @param element the element (must not be {@code null})
     */
    public void add(AnnotationElement element) {
        checkNotNullParam("element", element);
        elements.add(element);
    }

    /**
     * Add an annotation value to the annotation under given name.
     *
     * @param name the name of the annotation element (must not be {@code null})
     * @param value the element value (must not be {@code null})
     */
    public void add(String name, AnnotationValue value) {
        checkNotNullParam("name", name);
        checkNotNullParam("value", value);
        add(AnnotationElement.of(name, value));
    }

    private void add(SerializedLambda sl, AnnotationValue value) {
        assert annotationClass != null;
        if (sl.getImplClass().equals(annotationClass.getName().replace('.', '/'))
                && sl.getImplMethodKind() == MethodHandleInfo.REF_invokeInterface) {
            add(sl.getImplMethodName(), value);
        } else {
            throw new IllegalArgumentException("Invalid property name");
        }
    }

    // ---

    @Override
    public void add(String name, boolean value) {
        add(name, AnnotationValue.ofBoolean(value));
    }

    @Override
    public void add(BooleanProperty<A> prop, boolean value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofBoolean(value));
    }

    @Override
    public void add(String name, byte value) {
        add(name, AnnotationValue.ofByte(value));
    }

    @Override
    public void add(ByteProperty<A> prop, byte value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofByte(value));
    }

    @Override
    public void add(String name, short value) {
        add(name, AnnotationValue.ofShort(value));
    }

    @Override
    public void add(ShortProperty<A> prop, short value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofShort(value));
    }

    @Override
    public void add(String name, int value) {
        add(name, AnnotationValue.ofInt(value));
    }

    @Override
    public void add(IntProperty<A> prop, int value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofInt(value));
    }

    @Override
    public void add(String name, long value) {
        add(name, AnnotationValue.ofLong(value));
    }

    @Override
    public void add(LongProperty<A> prop, long value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofLong(value));
    }

    @Override
    public void add(String name, float value) {
        add(name, AnnotationValue.ofFloat(value));
    }

    @Override
    public void add(FloatProperty<A> prop, float value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofFloat(value));
    }

    @Override
    public void add(String name, double value) {
        add(name, AnnotationValue.ofDouble(value));
    }

    @Override
    public void add(DoubleProperty<A> prop, double value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofDouble(value));
    }

    @Override
    public void add(String name, char value) {
        add(name, AnnotationValue.ofChar(value));
    }

    @Override
    public void add(CharProperty<A> prop, char value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofChar(value));
    }

    @Override
    public void add(String name, String value) {
        add(name, AnnotationValue.ofString(value));
    }

    @Override
    public void add(StringProperty<A> prop, String value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofString(value));
    }

    @Override
    public void add(String name, Class<?> value) {
        add(name, AnnotationValue.ofClass(Util.classDesc(value)));
    }

    @Override
    public void add(ClassProperty<A> prop, Class<?> value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofClass(Util.classDesc(value)));
    }

    @Override
    public void add(String name, ClassDesc value) {
        add(name, AnnotationValue.ofClass(value));
    }

    @Override
    public void add(ClassProperty<A> prop, ClassDesc value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofClass(value));
    }

    @Override
    public <E extends Enum<E>> void add(String name, E value) {
        add(name, AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
    }

    @Override
    public <E extends Enum<E>> void add(EnumProperty<A, E> prop, E value) {
        add(Util.serializedLambda(prop), AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
    }

    @Override
    public void add(String name, ClassDesc enumClass, String enumConstant) {
        add(name, AnnotationValue.ofEnum(enumClass, enumConstant));
    }

    @Override
    public <S extends Annotation> void add(String name, Class<S> annotationClass, Consumer<AnnotationCreator<S>> builder) {
        add(name, AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Annotation> void add(AnnotationProperty<A, S> prop, Consumer<AnnotationCreator<S>> builder) {
        assert annotationClass != null;
        try {
            SerializedLambda serializedLambda = Util.serializedLambda(prop);
            String annotationElement = serializedLambda.getImplMethodName();
            Class<S> clazz = (Class<S>) annotationClass.getDeclaredMethod(annotationElement).getReturnType();
            add(serializedLambda, AnnotationValue.ofAnnotation(makeAnnotation(clazz, builder)));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public void add(String name, ClassDesc annotationClass, Consumer<AnnotationCreator<Annotation>> builder) {
        add(name, AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
    }

    @Override
    public void addArray(String name, boolean... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (boolean value : values) {
            array.add(AnnotationValue.ofBoolean(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(BooleanArrayProperty<A> prop, boolean... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (boolean value : values) {
            array.add(AnnotationValue.ofBoolean(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, byte... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (byte value : values) {
            array.add(AnnotationValue.ofByte(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(ByteArrayProperty<A> prop, byte... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (byte value : values) {
            array.add(AnnotationValue.ofByte(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, short... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (short value : values) {
            array.add(AnnotationValue.ofShort(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(ShortArrayProperty<A> prop, short... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (short value : values) {
            array.add(AnnotationValue.ofShort(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, int... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (int value : values) {
            array.add(AnnotationValue.ofInt(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(IntArrayProperty<A> prop, int... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (int value : values) {
            array.add(AnnotationValue.ofInt(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, long... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (long value : values) {
            array.add(AnnotationValue.ofLong(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(LongArrayProperty<A> prop, long... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (long value : values) {
            array.add(AnnotationValue.ofLong(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, float... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (float value : values) {
            array.add(AnnotationValue.ofFloat(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(FloatArrayProperty<A> prop, float... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (float value : values) {
            array.add(AnnotationValue.ofFloat(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, double... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (double value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(DoubleArrayProperty<A> prop, double... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (double value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, char... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (char value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(CharArrayProperty<A> prop, char... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (char value : values) {
            array.add(AnnotationValue.ofChar(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, String... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (String value : values) {
            array.add(AnnotationValue.ofString(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(StringArrayProperty<A> prop, String... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (String value : values) {
            array.add(AnnotationValue.ofString(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, Class<?>... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (Class<?> value : values) {
            array.add(AnnotationValue.ofClass(Util.classDesc(value)));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(ClassArrayProperty<A> prop, Class<?>... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (Class<?> value : values) {
            array.add(AnnotationValue.ofClass(Util.classDesc(value)));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, ClassDesc... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (ClassDesc value : values) {
            array.add(AnnotationValue.ofClass(value));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(ClassArrayProperty<A> prop, ClassDesc... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (ClassDesc value : values) {
            array.add(AnnotationValue.ofClass(value));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public <E extends Enum<E>> void addArray(String name, List<E> values) {
        List<AnnotationValue> array = new ArrayList<>(values.size());
        for (E value : values) {
            array.add(AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public <E extends Enum<E>> void addArray(EnumArrayProperty<A, E> prop, List<E> values) {
        List<AnnotationValue> array = new ArrayList<>(values.size());
        for (E value : values) {
            array.add(AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void addArray(String name, ClassDesc enumClass, String... enumConstants) {
        List<AnnotationValue> array = new ArrayList<>(enumConstants.length);
        for (String enumConstant : enumConstants) {
            array.add(AnnotationValue.ofEnum(enumClass, enumConstant));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    public <E extends Enum<E>> void addArray(EnumArrayProperty<A, E> prop, ClassDesc enumClass, String... enumConstants) {
        List<AnnotationValue> array = new ArrayList<>(enumConstants.length);
        for (String enumConstant : enumConstants) {
            array.add(AnnotationValue.ofEnum(enumClass, enumConstant));
        }
        add(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public <S extends Annotation> void addArray(String name, Class<S> annotationClass,
            List<Consumer<AnnotationCreator<S>>> builders) {
        List<AnnotationValue> array = new ArrayList<>(builders.size());
        for (Consumer<AnnotationCreator<S>> builder : builders) {
            array.add(AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
        }
        add(name, AnnotationValue.ofArray(array));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Annotation> void addArray(AnnotationArrayProperty<A, S> prop,
            List<Consumer<AnnotationCreator<S>>> builders) {
        assert annotationClass != null;
        try {
            SerializedLambda serializedLambda = Util.serializedLambda(prop);
            String annotationElement = serializedLambda.getImplMethodName();
            Class<S> clazz = (Class<S>) annotationClass.getDeclaredMethod(annotationElement).getReturnType().getComponentType();

            List<AnnotationValue> array = new ArrayList<>(builders.size());
            for (Consumer<AnnotationCreator<S>> builder : builders) {
                array.add(AnnotationValue.ofAnnotation(makeAnnotation(clazz, builder)));
            }
            add(serializedLambda, AnnotationValue.ofArray(array));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public void addArray(String name, ClassDesc annotationClass, List<Consumer<AnnotationCreator<Annotation>>> builders) {
        List<AnnotationValue> array = new ArrayList<>(builders.size());
        for (Consumer<AnnotationCreator<Annotation>> builder : builders) {
            array.add(AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
        }
        add(name, AnnotationValue.ofArray(array));
    }
}
