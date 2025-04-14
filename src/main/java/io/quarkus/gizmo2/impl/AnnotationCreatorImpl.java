package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.AnnotationElement;
import io.github.dmlloyd.classfile.AnnotationValue;
import io.quarkus.gizmo2.creator.AnnotationCreator;

public final class AnnotationCreatorImpl<A extends Annotation> implements AnnotationCreator<A> {
    /**
     * Factory for creating annotation instances.
     *
     * @param type the annotation class (must not be {@code null})
     * @param builder the builder for the annotation contents (must not be {@code null})
     * @return the constructed annotation (not {@code null})
     * @param <A> the annotation type
     */
    static <A extends Annotation> io.github.dmlloyd.classfile.Annotation makeAnnotation(Class<A> type,
            Consumer<AnnotationCreator<A>> builder) {
        checkNotNullParam("type", type);
        checkNotNullParam("builder", builder);

        AnnotationCreatorImpl<A> creator = new AnnotationCreatorImpl<>(type);
        builder.accept(creator);
        return io.github.dmlloyd.classfile.Annotation.of(Util.classDesc(type), creator.elements);
    }

    /**
     * Factory for creating annotation instances.
     *
     * @param type the annotation class (must not be {@code null})
     * @param builder the builder for the annotation contents (must not be {@code null})
     * @return the constructed annotation (not {@code null})
     */
    static io.github.dmlloyd.classfile.Annotation makeAnnotation(ClassDesc type,
            Consumer<AnnotationCreator<Annotation>> builder) {
        checkNotNullParam("type", type);
        checkNotNullParam("builder", builder);

        AnnotationCreatorImpl<Annotation> creator = new AnnotationCreatorImpl<>(null);
        builder.accept(creator);
        return io.github.dmlloyd.classfile.Annotation.of(type, creator.elements);
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
    public void with(AnnotationElement element) {
        checkNotNullParam("element", element);
        elements.add(element);
    }

    /**
     * Add an annotation value to the annotation under given name.
     *
     * @param name the name of the annotation element (must not be {@code null})
     * @param value the element value (must not be {@code null})
     */
    public void with(String name, AnnotationValue value) {
        checkNotNullParam("name", name);
        checkNotNullParam("value", value);
        with(AnnotationElement.of(name, value));
    }

    private void with(SerializedLambda sl, AnnotationValue value) {
        assert annotationClass != null;
        if (sl.getImplClass().equals(annotationClass.getName().replace('.', '/'))
                && sl.getImplMethodKind() == MethodHandleInfo.REF_invokeInterface) {
            with(sl.getImplMethodName(), value);
        } else {
            throw new IllegalArgumentException("Invalid property name");
        }
    }

    // ---

    @Override
    public void with(String name, boolean value) {
        with(name, AnnotationValue.ofBoolean(value));
    }

    @Override
    public void with(BooleanProperty<A> prop, boolean value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofBoolean(value));
    }

    @Override
    public void with(String name, byte value) {
        with(name, AnnotationValue.ofByte(value));
    }

    @Override
    public void with(ByteProperty<A> prop, byte value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofByte(value));
    }

    @Override
    public void with(String name, short value) {
        with(name, AnnotationValue.ofShort(value));
    }

    @Override
    public void with(ShortProperty<A> prop, short value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofShort(value));
    }

    @Override
    public void with(String name, int value) {
        with(name, AnnotationValue.ofInt(value));
    }

    @Override
    public void with(IntProperty<A> prop, int value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofInt(value));
    }

    @Override
    public void with(String name, long value) {
        with(name, AnnotationValue.ofLong(value));
    }

    @Override
    public void with(LongProperty<A> prop, long value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofLong(value));
    }

    @Override
    public void with(String name, float value) {
        with(name, AnnotationValue.ofFloat(value));
    }

    @Override
    public void with(FloatProperty<A> prop, float value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofFloat(value));
    }

    @Override
    public void with(String name, double value) {
        with(name, AnnotationValue.ofDouble(value));
    }

    @Override
    public void with(DoubleProperty<A> prop, double value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofDouble(value));
    }

    @Override
    public void with(String name, char value) {
        with(name, AnnotationValue.ofChar(value));
    }

    @Override
    public void with(CharProperty<A> prop, char value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofChar(value));
    }

    @Override
    public void with(String name, String value) {
        with(name, AnnotationValue.ofString(value));
    }

    @Override
    public void with(StringProperty<A> prop, String value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofString(value));
    }

    @Override
    public void with(String name, Class<?> value) {
        with(name, AnnotationValue.ofClass(Util.classDesc(value)));
    }

    @Override
    public void with(ClassProperty<A> prop, Class<?> value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofClass(Util.classDesc(value)));
    }

    @Override
    public void with(String name, ClassDesc value) {
        with(name, AnnotationValue.ofClass(value));
    }

    @Override
    public void with(ClassProperty<A> prop, ClassDesc value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofClass(value));
    }

    @Override
    public <E extends Enum<E>> void with(String name, E value) {
        with(name, AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
    }

    @Override
    public <E extends Enum<E>> void with(EnumProperty<A, E> prop, E value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
    }

    @Override
    public void with(String name, ClassDesc enumClass, String enumConstant) {
        with(name, AnnotationValue.ofEnum(enumClass, enumConstant));
    }

    @Override
    public <S extends Annotation> void with(String name, Class<S> annotationClass, Consumer<AnnotationCreator<S>> builder) {
        with(name, AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Annotation> void with(AnnotationProperty<A, S> prop, Consumer<AnnotationCreator<S>> builder) {
        assert annotationClass != null;
        try {
            SerializedLambda serializedLambda = Util.serializedLambda(prop);
            String annotationElement = serializedLambda.getImplMethodName();
            Class<S> clazz = (Class<S>) annotationClass.getDeclaredMethod(annotationElement).getReturnType();
            with(serializedLambda, AnnotationValue.ofAnnotation(makeAnnotation(clazz, builder)));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public void with(String name, ClassDesc annotationClass, Consumer<AnnotationCreator<Annotation>> builder) {
        with(name, AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
    }

    @Override
    public void withArray(String name, boolean... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (boolean value : values) {
            array.add(AnnotationValue.ofBoolean(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(BooleanArrayProperty<A> prop, boolean... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (boolean value : values) {
            array.add(AnnotationValue.ofBoolean(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, byte... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (byte value : values) {
            array.add(AnnotationValue.ofByte(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(ByteArrayProperty<A> prop, byte... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (byte value : values) {
            array.add(AnnotationValue.ofByte(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, short... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (short value : values) {
            array.add(AnnotationValue.ofShort(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(ShortArrayProperty<A> prop, short... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (short value : values) {
            array.add(AnnotationValue.ofShort(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, int... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (int value : values) {
            array.add(AnnotationValue.ofInt(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(IntArrayProperty<A> prop, int... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (int value : values) {
            array.add(AnnotationValue.ofInt(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, long... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (long value : values) {
            array.add(AnnotationValue.ofLong(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(LongArrayProperty<A> prop, long... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (long value : values) {
            array.add(AnnotationValue.ofLong(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, float... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (float value : values) {
            array.add(AnnotationValue.ofFloat(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(FloatArrayProperty<A> prop, float... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (float value : values) {
            array.add(AnnotationValue.ofFloat(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, double... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (double value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(DoubleArrayProperty<A> prop, double... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (double value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, char... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (char value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(CharArrayProperty<A> prop, char... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (char value : values) {
            array.add(AnnotationValue.ofChar(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, String... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (String value : values) {
            array.add(AnnotationValue.ofString(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(StringArrayProperty<A> prop, String... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (String value : values) {
            array.add(AnnotationValue.ofString(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, Class<?>... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (Class<?> value : values) {
            array.add(AnnotationValue.ofClass(Util.classDesc(value)));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(ClassArrayProperty<A> prop, Class<?>... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (Class<?> value : values) {
            array.add(AnnotationValue.ofClass(Util.classDesc(value)));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, ClassDesc... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (ClassDesc value : values) {
            array.add(AnnotationValue.ofClass(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(ClassArrayProperty<A> prop, ClassDesc... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (ClassDesc value : values) {
            array.add(AnnotationValue.ofClass(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public <E extends Enum<E>> void withArray(String name, List<E> values) {
        List<AnnotationValue> array = new ArrayList<>(values.size());
        for (E value : values) {
            array.add(AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public <E extends Enum<E>> void withArray(EnumArrayProperty<A, E> prop, List<E> values) {
        List<AnnotationValue> array = new ArrayList<>(values.size());
        for (E value : values) {
            array.add(AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public void withArray(String name, ClassDesc enumClass, String... enumConstants) {
        List<AnnotationValue> array = new ArrayList<>(enumConstants.length);
        for (String enumConstant : enumConstants) {
            array.add(AnnotationValue.ofEnum(enumClass, enumConstant));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    public <E extends Enum<E>> void withArray(EnumArrayProperty<A, E> prop, ClassDesc enumClass, String... enumConstants) {
        List<AnnotationValue> array = new ArrayList<>(enumConstants.length);
        for (String enumConstant : enumConstants) {
            array.add(AnnotationValue.ofEnum(enumClass, enumConstant));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    @Override
    public <S extends Annotation> void withArray(String name, Class<S> annotationClass,
            List<Consumer<AnnotationCreator<S>>> builders) {
        List<AnnotationValue> array = new ArrayList<>(builders.size());
        for (Consumer<AnnotationCreator<S>> builder : builders) {
            array.add(AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Annotation> void withArray(AnnotationArrayProperty<A, S> prop,
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
            with(serializedLambda, AnnotationValue.ofArray(array));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    public void withArray(String name, ClassDesc annotationClass, List<Consumer<AnnotationCreator<Annotation>>> builders) {
        List<AnnotationValue> array = new ArrayList<>(builders.size());
        for (Consumer<AnnotationCreator<Annotation>> builder : builders) {
            array.add(AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
        }
        with(name, AnnotationValue.ofArray(array));
    }
}
