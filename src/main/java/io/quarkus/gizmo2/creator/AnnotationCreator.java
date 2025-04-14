package io.quarkus.gizmo2.creator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.Consumer;

import io.quarkus.gizmo2.impl.AnnotationCreatorImpl;

/**
 * A typesafe creator for an annotation body.
 *
 * @param <A> the annotation type
 */
public sealed interface AnnotationCreator<A extends Annotation> permits AnnotationCreatorImpl {
    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, boolean value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(BooleanProperty<A> prop, boolean value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, byte value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(ByteProperty<A> prop, byte value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, short value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(ShortProperty<A> prop, short value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, int value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(IntProperty<A> prop, int value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, long value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(LongProperty<A> prop, long value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, float value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(FloatProperty<A> prop, float value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, double value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(DoubleProperty<A> prop, double value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void with(String name, char value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(CharProperty<A> prop, char value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    void with(String name, String value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(StringProperty<A> prop, String value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    void with(String name, Class<?> value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(ClassProperty<A> prop, Class<?> value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    void with(String name, ClassDesc value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void with(ClassProperty<A> prop, ClassDesc value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    <E extends Enum<E>> void with(String name, E value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    <E extends Enum<E>> void with(EnumProperty<A, E> prop, E value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param enumClass the enum class
     * @param enumConstant the name of the enum constant
     */
    void with(String name, ClassDesc enumClass, String enumConstant);

    /**
     * Add an annotation property with the given name and built value.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void with(String name, Class<S> annotationClass, Consumer<AnnotationCreator<S>> builder);

    /**
     * Add an annotation property for the given method and built value.
     *
     * @param prop the annotation property method (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void with(AnnotationProperty<A, S> prop, Consumer<AnnotationCreator<S>> builder);

    /**
     * Add an annotation property with the given name and built value.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     */
    void with(String name, ClassDesc annotationClass, Consumer<AnnotationCreator<Annotation>> builder);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, boolean... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(BooleanArrayProperty<A> prop, boolean... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, byte... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(ByteArrayProperty<A> prop, byte... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, short... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(ShortArrayProperty<A> prop, short... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, int... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(IntArrayProperty<A> prop, int... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, long... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(LongArrayProperty<A> prop, long... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, float... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(FloatArrayProperty<A> prop, float... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, double... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(DoubleArrayProperty<A> prop, double... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, char... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(CharArrayProperty<A> prop, char... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, String... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(StringArrayProperty<A> prop, String... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, Class<?>... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(ClassArrayProperty<A> prop, Class<?>... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(String name, ClassDesc... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void withArray(ClassArrayProperty<A> prop, ClassDesc... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    <E extends Enum<E>> void withArray(String name, List<E> values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    <E extends Enum<E>> void withArray(EnumArrayProperty<A, E> prop, List<E> values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param enumClass the enum class (must not be {@code null})
     * @param enumConstants the enum constants (must not be {@code null})
     */
    void withArray(String name, ClassDesc enumClass, String... enumConstants);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param enumClass the enum class (must not be {@code null})
     * @param enumConstants the enum constants (must not be {@code null})
     */
    <E extends Enum<E>> void withArray(EnumArrayProperty<A, E> prop, ClassDesc enumClass, String... enumConstants);

    /**
     * Add an annotation property with the given name and built values.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void withArray(String name, Class<S> annotationClass, List<Consumer<AnnotationCreator<S>>> builders);

    /**
     * Add an annotation property for the given method and built values.
     *
     * @param prop the annotation property method (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void withArray(AnnotationArrayProperty<A, S> prop, List<Consumer<AnnotationCreator<S>>> builders);

    /**
     * Add an annotation property with the given name and built values.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     */
    void withArray(String name, ClassDesc annotationClass, List<Consumer<AnnotationCreator<Annotation>>> builders);

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
