package io.quarkus.gizmo2.creator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    static <A extends java.lang.annotation.Annotation> io.github.dmlloyd.classfile.Annotation makeAnnotation(Class<A> type, Consumer<AnnotationCreator<A>> maker) {
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
        if (
            sl.getImplClass().equals(type().getName().replace('.', '/')) &&
            sl.getImplMethodKind() == MethodHandleInfo.REF_invokeInterface
        ) {
            with(sl.getImplMethodName(), value);
        } else {
            throw new IllegalArgumentException("Invalid property name");
        }
    }

    private AnnotationValue arrayOf(Stream<AnnotationValue> values) {
        return AnnotationValue.ofArray(values.toArray(AnnotationValue[]::new));
    }

    /**
     * Add an annotation property whose value is another annotation.
     *
     * @param prop the annotation property method (must not be {@code null})
     * @param subBuilder the builder for the nested annotation (must not be {@code null})
     * @param <S> the annotation type
     */
    @SuppressWarnings("unchecked")
    default <S extends Annotation> void with(AnnotationProperty<A, S> prop, Consumer<AnnotationCreator<S>> subBuilder) {
        // get return type off of lambda implementation class
        Class<S> clazz;
        try {
            clazz = (Class<S>) prop.getClass().getDeclaredMethod("get", Annotation.class, Annotation.class).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
        with(Util.serializedLambda(prop), AnnotationValue.ofAnnotation(makeAnnotation(clazz, subBuilder)));
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
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, boolean... values) {
        with(name, arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofBoolean(values[i]))));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void with(BooleanArrayProperty<A> prop, boolean... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofBoolean(values[i]))));
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
     * @param values the property values (must not be {@code null})
     */
    default void withArray(String name, int... values) {
        with(name, arrayOf(IntStream.of(values).mapToObj(AnnotationValue::ofInt)));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void with(IntArrayProperty<A> prop, int... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.of(values).mapToObj(AnnotationValue::ofInt)));
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
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void with(ByteArrayProperty<A> prop, byte... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofByte(values[i]))));
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
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void with(CharArrayProperty<A> prop, char... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofChar(values[i]))));
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
     * @param values the property values (must not be {@code null})
     */
    default void with(String name, List<String> values) {
        with(name, arrayOf(values.stream().map(AnnotationValue::ofString)));
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void with(String name, String... values) {
        with(name, Arrays.asList(values));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void with(StringArrayProperty<A> prop, List<String> values) {
        with(Util.serializedLambda(prop), arrayOf(values.stream().map(AnnotationValue::ofString)));
    }

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    default void with(StringArrayProperty<A> prop, String... values) {
        with(prop, Arrays.asList(values));
    }

    /**
     * An interface which maps the type of an annotation method which returns another annotation.
     *
     * @param <A> the enclosing annotation type
     * @param <S> the nested annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code boolean}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code boolean[]}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns an {@code int}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns an {@code int[]}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code byte}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code byte[]}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code char}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code char[]}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code Class}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code Class[]}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code String}.
     *
     * @param <A> the enclosing annotation type
     */
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
     * An interface which maps the type of an annotation method which returns a {@code String[]}.
     *
     * @param <A> the enclosing annotation type
     */
    interface StringArrayProperty<A extends Annotation> extends Serializable {
        /**
         * A method which reflects the corresponding annotation method.
         *
         * @param annotation the annotation (must not be {@code null})
         * @return the annotation value
         */
        String[] get(A annotation);
    }
}
