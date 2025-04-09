package io.quarkus.gizmo2.creator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.AnnotationElement;
import io.github.dmlloyd.classfile.AnnotationValue;
import io.quarkus.gizmo2.impl.Util;

/**
 * A typesafe creator for an annotation body.
 *
 * @param <A> the annotation type
 */
public interface AnnotationCreator<A extends Annotation> {
    /**
     * Factory for creating annotation instances.
     *
     * @param type the annotation class (must not be {@code null})
     * @param maker the builder for the annotation contents (must not be {@code null})
     * @return the constructed annotation (not {@code null})
     * @param <A> the annotation type
     */
    static <A extends Annotation> io.github.dmlloyd.classfile.Annotation makeAnnotation(Class<A> type,
            Consumer<AnnotationCreator<A>> maker) {
        var c = new AnnotationCreator<A>() {
            final List<AnnotationElement> elements = new ArrayList<>();

            public Class<A> type() {
                return type;
            }

            public void with(final AnnotationElement element) {
                elements.add(Objects.requireNonNull(element, "element"));
            }
        };
        maker.accept(c);
        return io.github.dmlloyd.classfile.Annotation.of(Util.classDesc(type), c.elements);
    }

    /**
     * {@return the type class of the annotation}
     */
    Class<A> type();

    /**
     * Add a whole annotation element directly to the annotation.
     *
     * @param element the element (must not be {@code null})
     */
    void with(AnnotationElement element);

    /**
     * Add an annotation value to the annotation.
     *
     * @param name the name of the annotation value property (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    default void with(String name, AnnotationValue value) {
        with(AnnotationElement.of(name, value));
    }

    private void with(SerializedLambda sl, AnnotationValue value) {
        if (sl.getImplClass().equals(type().getName().replace('.', '/'))
                && sl.getImplMethodKind() == MethodHandleInfo.REF_invokeInterface) {
            with(sl.getImplMethodName(), value);
        } else {
            throw new IllegalArgumentException("Invalid property name");
        }
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, boolean value) {
        with(name, AnnotationValue.ofBoolean(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(BooleanProperty<A> prop, boolean value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofBoolean(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, byte value) {
        with(name, AnnotationValue.ofByte(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(ByteProperty<A> prop, byte value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofByte(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, short value) {
        with(name, AnnotationValue.ofShort(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(ShortProperty<A> prop, short value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofShort(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, int value) {
        with(name, AnnotationValue.ofInt(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(IntProperty<A> prop, int value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofInt(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, long value) {
        with(name, AnnotationValue.ofLong(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(LongProperty<A> prop, long value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofLong(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, float value) {
        with(name, AnnotationValue.ofFloat(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(FloatProperty<A> prop, float value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofFloat(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, double value) {
        with(name, AnnotationValue.ofDouble(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(DoubleProperty<A> prop, double value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofDouble(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    default void with(String name, char value) {
        with(name, AnnotationValue.ofChar(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(CharProperty<A> prop, char value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofChar(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    default void with(String name, String value) {
        with(name, AnnotationValue.ofString(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(StringProperty<A> prop, String value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofString(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    default void with(String name, Class<?> value) {
        with(name, AnnotationValue.ofClass(Util.classDesc(value)));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(ClassProperty<A> prop, Class<?> value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofClass(Util.classDesc(value)));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    default void with(String name, ClassDesc value) {
        with(name, AnnotationValue.ofClass(value));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default void with(ClassProperty<A> prop, ClassDesc value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofClass(value));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    default <E extends Enum<E>> void with(String name, E value) {
        with(name, AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    default <E extends Enum<E>> void with(EnumProperty<A, E> prop, E value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param enumClass the enum class
     * @param enumConstant the name of the enum constant
     */
    default void with(String name, ClassDesc enumClass, String enumConstant) {
        with(name, AnnotationValue.ofEnum(enumClass, enumConstant));
    }

    /**
     * Add an annotation property with the given name and built value.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     * @param <S> the annotation type
     */
    default <S extends Annotation> void with(String name, Class<S> annotationClass, Consumer<AnnotationCreator<S>> builder) {
        with(name, AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
    }

    /**
     * Add an annotation property for the given method and built value.
     *
     * @param prop the annotation property method (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     * @param <S> the annotation type
     */
    @SuppressWarnings("unchecked")
    default <S extends Annotation> void with(AnnotationProperty<A, S> prop, Consumer<AnnotationCreator<S>> builder) {
        // the impl method is always an annotation element, which never takes any parameter,
        // so the signature starts with `()` and the rest is the descriptor of the desired annotation class
        String sig = Util.serializedLambda(prop).getImplMethodSignature();
        assert sig.startsWith("()L") && sig.endsWith(";");
        String annotationClass = sig.substring(3, sig.length() - 1).replace('/', '.');
        Class<S> clazz;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = AnnotationCreator.class.getClassLoader();
            }
            clazz = (Class<S>) Class.forName(annotationClass, false, cl);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }

        with(Util.serializedLambda(prop), AnnotationValue.ofAnnotation(makeAnnotation(clazz, builder)));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, boolean... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (boolean value : values) {
            array.add(AnnotationValue.ofBoolean(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(BooleanArrayProperty<A> prop, boolean... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (boolean value : values) {
            array.add(AnnotationValue.ofBoolean(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, byte... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (byte value : values) {
            array.add(AnnotationValue.ofByte(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(ByteArrayProperty<A> prop, byte... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (byte value : values) {
            array.add(AnnotationValue.ofByte(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, short... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (short value : values) {
            array.add(AnnotationValue.ofShort(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(ShortArrayProperty<A> prop, short... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (short value : values) {
            array.add(AnnotationValue.ofShort(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, int... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (int value : values) {
            array.add(AnnotationValue.ofInt(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(IntArrayProperty<A> prop, int... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (int value : values) {
            array.add(AnnotationValue.ofInt(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, long... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (long value : values) {
            array.add(AnnotationValue.ofLong(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(LongArrayProperty<A> prop, long... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (long value : values) {
            array.add(AnnotationValue.ofLong(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, float... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (float value : values) {
            array.add(AnnotationValue.ofFloat(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(FloatArrayProperty<A> prop, float... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (float value : values) {
            array.add(AnnotationValue.ofFloat(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, double... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (double value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(DoubleArrayProperty<A> prop, double... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (double value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, char... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (char value : values) {
            array.add(AnnotationValue.ofDouble(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(CharArrayProperty<A> prop, char... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (char value : values) {
            array.add(AnnotationValue.ofChar(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, String... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (String value : values) {
            array.add(AnnotationValue.ofString(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(StringArrayProperty<A> prop, String... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (String value : values) {
            array.add(AnnotationValue.ofString(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, Class<?>... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (Class<?> value : values) {
            array.add(AnnotationValue.ofClass(Util.classDesc(value)));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(ClassArrayProperty<A> prop, Class<?>... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (Class<?> value : values) {
            array.add(AnnotationValue.ofClass(Util.classDesc(value)));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, ClassDesc... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (ClassDesc value : values) {
            array.add(AnnotationValue.ofClass(value));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void withArray(ClassArrayProperty<A> prop, ClassDesc... values) {
        List<AnnotationValue> array = new ArrayList<>(values.length);
        for (ClassDesc value : values) {
            array.add(AnnotationValue.ofClass(value));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default <E extends Enum<E>> void withArray(String name, List<E> values) {
        List<AnnotationValue> array = new ArrayList<>(values.size());
        for (E value : values) {
            array.add(AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default <E extends Enum<E>> void withArray(EnumArrayProperty<A, E> prop, List<E> values) {
        List<AnnotationValue> array = new ArrayList<>(values.size());
        for (E value : values) {
            array.add(AnnotationValue.ofEnum(Util.classDesc(value.getDeclaringClass()), value.name()));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param enumClass the enum class (must not be {@code null})
     * @param enumConstants the enum constants (must not be {@code null})
     */
    default void withArray(String name, ClassDesc enumClass, String... enumConstants) {
        List<AnnotationValue> array = new ArrayList<>(enumConstants.length);
        for (String enumConstant : enumConstants) {
            array.add(AnnotationValue.ofEnum(enumClass, enumConstant));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param enumClass the enum class (must not be {@code null})
     * @param enumConstants the enum constants (must not be {@code null})
     */
    default <E extends Enum<E>> void withArray(EnumArrayProperty<A, E> prop, ClassDesc enumClass, String... enumConstants) {
        List<AnnotationValue> array = new ArrayList<>(enumConstants.length);
        for (String enumConstant : enumConstants) {
            array.add(AnnotationValue.ofEnum(enumClass, enumConstant));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property with the given name and built values.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     * @param <S> the annotation type
     */
    default <S extends Annotation> void withArray(String name, Class<S> annotationClass,
            List<Consumer<AnnotationCreator<S>>> builders) {
        List<AnnotationValue> array = new ArrayList<>(builders.size());
        for (Consumer<AnnotationCreator<S>> builder : builders) {
            array.add(AnnotationValue.ofAnnotation(makeAnnotation(annotationClass, builder)));
        }
        with(name, AnnotationValue.ofArray(array));
    }

    /**
     * Add an annotation property for the given method and built values.
     *
     * @param prop the annotation property method (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     * @param <S> the annotation type
     */
    @SuppressWarnings("unchecked")
    default <S extends Annotation> void withArray(AnnotationArrayProperty<A, S> prop,
            List<Consumer<AnnotationCreator<S>>> builders) {
        // the impl method is always an annotation element, which never takes any parameter,
        // so the signature starts with `()` and the rest is the descriptor of the desired annotation array
        String sig = Util.serializedLambda(prop).getImplMethodSignature();
        assert sig.startsWith("()[L") && sig.endsWith(";");
        String annotationClass = sig.substring(4, sig.length() - 1).replace('/', '.');
        Class<S> clazz;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = AnnotationCreator.class.getClassLoader();
            }
            clazz = (Class<S>) Class.forName(annotationClass, false, cl);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }

        List<AnnotationValue> array = new ArrayList<>(builders.size());
        for (Consumer<AnnotationCreator<S>> builder : builders) {
            array.add(AnnotationValue.ofAnnotation(makeAnnotation(clazz, builder)));
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofArray(array));
    }

    /**
     * Maps the annotation type to a method which returns {@code boolean}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface BooleanProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        boolean get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code byte}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface ByteProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        byte get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code short}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface ShortProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        short get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code int}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface IntProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        int get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code long}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface LongProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        long get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code float}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface FloatProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        float get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code double}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface DoubleProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        double get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code char}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface CharProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        char get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code String}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface StringProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        String get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code Class}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface ClassProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        Class<?> get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns an enum value.
     *
     * @param <A> the enclosing annotation type
     * @param <E> the enum type
     */
    @FunctionalInterface
    interface EnumProperty<A extends Annotation, E extends Enum<E>> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        E get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns another annotation.
     *
     * @param <A> the enclosing annotation type
     * @param <S> the nested annotation type
     */
    @FunctionalInterface
    interface AnnotationProperty<A extends Annotation, S extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        S get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code boolean[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface BooleanArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        boolean[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code byte[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface ByteArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        byte[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code short[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface ShortArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        short[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code int[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface IntArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        int[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code long[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface LongArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        long[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code float[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface FloatArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        float[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code double[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface DoubleArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        double[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code char[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface CharArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        char[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code String[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface StringArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        String[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns {@code Class[]}.
     *
     * @param <A> the enclosing annotation type
     */
    @FunctionalInterface
    interface ClassArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        Class<?>[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns an array of enum values.
     *
     * @param <A> the enclosing annotation type
     * @param <E> the enum type
     */
    @FunctionalInterface
    interface EnumArrayProperty<A extends Annotation, E extends Enum<E>> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        E[] get(A annotation);
    }

    /**
     * Maps the annotation type to a method which returns an array of other annotations.
     *
     * @param <A> the enclosing annotation type
     * @param <S> the nested annotation type
     */
    @FunctionalInterface
    interface AnnotationArrayProperty<A extends Annotation, S extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        S[] get(A annotation);
    }
}
