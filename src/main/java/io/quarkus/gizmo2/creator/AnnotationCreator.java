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

public interface AnnotationCreator<A extends Annotation> {
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

    Class<A> type();

    void with(AnnotationElement element);

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

    default void with(String name, boolean value) {
        with(name, AnnotationValue.ofBoolean(value));
    }

    default void with(BooleanProperty<A> prop, boolean value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofBoolean(value));
    }

    default void withArray(String name, boolean... values) {
        with(name, arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofBoolean(values[i]))));
    }

    default void with(BooleanArrayProperty<A> prop, boolean... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofBoolean(values[i]))));
    }

    default void with(String name, int value) {
        with(name, AnnotationValue.ofInt(value));
    }

    default void with(IntProperty<A> prop, int value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofInt(value));
    }

    default void withArray(String name, int... values) {
        with(name, arrayOf(IntStream.of(values).mapToObj(AnnotationValue::ofInt)));
    }

    default void with(IntArrayProperty<A> prop, int... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.of(values).mapToObj(AnnotationValue::ofInt)));
    }

    default void with(ByteProperty<A> prop, byte value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofByte(value));
    }

    default void with(ByteArrayProperty<A> prop, byte... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofByte(values[i]))));
    }

    default void with(CharProperty<A> prop, char value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofChar(value));
    }

    default void with(CharArrayProperty<A> prop, char... values) {
        with(Util.serializedLambda(prop), arrayOf(IntStream.range(0, values.length).mapToObj(i -> AnnotationValue.ofChar(values[i]))));
    }

    default void with(String name, String value) {
        with(name, AnnotationValue.ofString(value));
    }

    default void with(StringProperty<A> prop, String value) {
        with(Util.serializedLambda(prop), AnnotationValue.ofString(value));
    }

    default void with(String name, List<String> values) {
        with(name, arrayOf(values.stream().map(AnnotationValue::ofString)));
    }

    default void with(String name, String... values) {
        with(name, Arrays.asList(values));
    }

    default void with(StringArrayProperty<A> prop, List<String> values) {
        with(Util.serializedLambda(prop), arrayOf(values.stream().map(AnnotationValue::ofString)));
    }

    default void with(StringArrayProperty<A> prop, String... values) {
        with(prop, Arrays.asList(values));
    }

    interface AnnotationProperty<A extends Annotation, S extends Annotation> extends Serializable {
        S get(A annotation);
    }

    interface BooleanProperty<A extends Annotation> extends Serializable {
        boolean get(A annotation);
    }

    interface BooleanArrayProperty<A extends Annotation> extends Serializable {
        boolean[] get(A annotation);
    }

    interface IntProperty<A extends Annotation> extends Serializable {
        int get(A annotation);
    }

    interface IntArrayProperty<A extends Annotation> extends Serializable {
        int[] get(A annotation);
    }

    interface ByteProperty<A extends Annotation> extends Serializable {
        byte get(A annotation);
    }

    interface ByteArrayProperty<A extends Annotation> extends Serializable {
        byte[] get(A annotation);
    }

    interface CharProperty<A extends Annotation> extends Serializable {
        char get(A annotation);
    }

    interface CharArrayProperty<A extends Annotation> extends Serializable {
        char[] get(A annotation);
    }

    interface ClassProperty<A extends Annotation> extends Serializable {
        Class<?> get(A annotation);
    }

    interface ClassArrayProperty<A extends Annotation> extends Serializable {
        Class<?>[] get(A annotation);
    }

    interface StringProperty<A extends Annotation> extends Serializable {
        String get(A annotation);
    }

    interface StringArrayProperty<A extends Annotation> extends Serializable {
        String[] get(A annotation);
    }
}
