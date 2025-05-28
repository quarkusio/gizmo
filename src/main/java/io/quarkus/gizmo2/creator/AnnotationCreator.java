package io.quarkus.gizmo2.creator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.quarkus.gizmo2.impl.AnnotationCreatorImpl;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * A typesafe creator for an annotation body.
 *
 * @param <A> the annotation type
 */
public sealed interface AnnotationCreator<A extends Annotation> permits AnnotationCreatorImpl {
    /**
     * Get an annotation builder which adds the given annotation to its given creator.
     * The builder may be used more than once.
     *
     * @param annotation the annotation to copy (must not be {@code null})
     * @return the annotation builder (not {@code null})
     * @param <A> the annotation type
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <A extends Annotation> Consumer<AnnotationCreator<A>> from(A annotation) {
        return ac -> {
            Class<A> annotationType = (Class<A>) annotation.annotationType();
            Method[] methods = annotationType.getDeclaredMethods();
            for (Method method : methods) {
                int mods = method.getModifiers();
                if (Modifier.isPublic(mods) && Modifier.isAbstract(mods) && !Modifier.isStatic(mods)) {
                    Object val;
                    try {
                        val = method.invoke(annotation);
                    } catch (IllegalAccessException e) {
                        throw new IllegalAccessError(e.getMessage());
                    } catch (InvocationTargetException e) {
                        try {
                            throw e.getCause();
                        } catch (RuntimeException | Error e2) {
                            throw e2;
                        } catch (Throwable e2) {
                            throw new UndeclaredThrowableException(e2);
                        }
                    }
                    if (val == null || Objects.equals(val, method.getDefaultValue())) {
                        // skip it (use default if there is one, otherwise ignore)
                        continue;
                    }
                    // the ABCs of annotation value types
                    String name = method.getName();
                    if (val instanceof Annotation a) {
                        ac.add(name, a.getClass().asSubclass(Annotation.class), AnnotationCreator.from(a));
                    } else if (val instanceof Boolean b) {
                        ac.add(name, b.booleanValue());
                    } else if (val instanceof Byte b) {
                        ac.add(name, b.byteValue());
                    } else if (val instanceof Class<?> z) {
                        ac.add(name, z);
                    } else if (val instanceof Character c) {
                        ac.add(name, c.charValue());
                    } else if (val instanceof Double d) {
                        ac.add(name, d.doubleValue());
                    } else if (val instanceof Float f) {
                        ac.add(name, f.floatValue());
                    } else if (val instanceof Integer i) {
                        ac.add(name, i.intValue());
                    } else if (val instanceof Long l) {
                        ac.add(name, l.longValue());
                    } else if (val instanceof Short s) {
                        ac.add(name, s.shortValue());
                    } else if (val instanceof String s) {
                        ac.add(name, s);
                    } else if (val instanceof Enum<?> e) {
                        ac.add(name, (Enum) e);
                    } else if (val instanceof Annotation[] array) {
                        ac.doWithArray(name, method.getReturnType().asSubclass(Annotation.class), array);
                    } else if (val instanceof boolean[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof byte[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof Class<?>[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof char[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof double[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof float[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof int[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof long[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof short[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof String[] array) {
                        ac.addArray(name, array);
                    } else if (val instanceof Enum<?>[] array) {
                        ac.addArray(name, Util.classDesc(method.getReturnType().asSubclass(Enum.class)),
                                Stream.of(array).map(Enum::name).toArray(String[]::new));
                    } else {
                        throw Assert.unreachableCode();
                    }
                }
            }

        };
    }

    private <AA extends Annotation> void doWithArray(String name, Class<AA> type, Annotation[] values) {
        addArray(name, type, Stream.of(values).map(type::cast).map(AnnotationCreator::from).toList());
    }

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, boolean value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(BooleanProperty<A> prop, boolean value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, byte value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(ByteProperty<A> prop, byte value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, short value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(ShortProperty<A> prop, short value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, int value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(IntProperty<A> prop, int value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, long value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(LongProperty<A> prop, long value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, float value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(FloatProperty<A> prop, float value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, double value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(DoubleProperty<A> prop, double value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value
     */
    void add(String name, char value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(CharProperty<A> prop, char value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    void add(String name, String value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(StringProperty<A> prop, String value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    void add(String name, Class<?> value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(ClassProperty<A> prop, Class<?> value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    void add(String name, ClassDesc value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    void add(ClassProperty<A> prop, ClassDesc value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param value the property value (must not be {@code null})
     */
    <E extends Enum<E>> void add(String name, E value);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param value the property value
     */
    <E extends Enum<E>> void add(EnumProperty<A, E> prop, E value);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param enumClass the enum class
     * @param enumConstant the name of the enum constant
     */
    void add(String name, ClassDesc enumClass, String enumConstant);

    /**
     * Add an annotation property with the given name and built value.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void add(String name, Class<S> annotationClass, Consumer<AnnotationCreator<S>> builder);

    /**
     * Add an annotation property for the given method and built value.
     *
     * @param prop the annotation property method (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void add(AnnotationProperty<A, S> prop, Consumer<AnnotationCreator<S>> builder);

    /**
     * Add an annotation property with the given name and built value.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builder the builder for the nested annotation (must not be {@code null})
     */
    void add(String name, ClassDesc annotationClass, Consumer<AnnotationCreator<Annotation>> builder);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, boolean... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(BooleanArrayProperty<A> prop, boolean... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, byte... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(ByteArrayProperty<A> prop, byte... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, short... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(ShortArrayProperty<A> prop, short... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, int... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(IntArrayProperty<A> prop, int... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, long... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(LongArrayProperty<A> prop, long... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, float... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(FloatArrayProperty<A> prop, float... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, double... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(DoubleArrayProperty<A> prop, double... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, char... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(CharArrayProperty<A> prop, char... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, String... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(StringArrayProperty<A> prop, String... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, Class<?>... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(ClassArrayProperty<A> prop, Class<?>... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(String name, ClassDesc... values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    void addArray(ClassArrayProperty<A> prop, ClassDesc... values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    <E extends Enum<E>> void addArray(String name, List<E> values);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param values the property values (must not be {@code null})
     */
    <E extends Enum<E>> void addArray(EnumArrayProperty<A, E> prop, List<E> values);

    /**
     * Add an annotation property with the given name and value.
     *
     * @param name the property name (must not be {@code null})
     * @param enumClass the enum class (must not be {@code null})
     * @param enumConstants the enum constants (must not be {@code null})
     */
    void addArray(String name, ClassDesc enumClass, String... enumConstants);

    /**
     * Add an annotation property for the given method and value.
     *
     * @param prop the property method (must not be {@code null})
     * @param enumClass the enum class (must not be {@code null})
     * @param enumConstants the enum constants (must not be {@code null})
     */
    <E extends Enum<E>> void addArray(EnumArrayProperty<A, E> prop, ClassDesc enumClass, String... enumConstants);

    /**
     * Add an annotation property with the given name and built values.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void addArray(String name, Class<S> annotationClass, List<Consumer<AnnotationCreator<S>>> builders);

    /**
     * Add an annotation property for the given method and built values.
     *
     * @param prop the annotation property method (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     * @param <S> the annotation type
     */
    <S extends Annotation> void addArray(AnnotationArrayProperty<A, S> prop, List<Consumer<AnnotationCreator<S>>> builders);

    /**
     * Add an annotation property with the given name and built values.
     *
     * @param name the property name (must not be {@code null})
     * @param annotationClass the class of the nested annotation (must not be {@code null})
     * @param builders the builders for the nested annotations (must not be {@code null})
     */
    void addArray(String name, ClassDesc annotationClass, List<Consumer<AnnotationCreator<Annotation>>> builders);

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
