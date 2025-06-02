package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.ClassCreatorImpl;
import io.quarkus.gizmo2.impl.EqualsHashCodeToStringGenerator;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a class type.
 */
public sealed interface ClassCreator extends TypeCreator, SimpleTyped, TypeParameterizedCreator
        permits AnonymousClassCreator, ClassCreatorImpl {
    /**
     * {@return the superclass}
     *
     * @see #extends_(ClassDesc)
     */
    ClassDesc superClass();

    /**
     * Extend the given generic class.
     *
     * @param genericType the generic class (must not be {@code null})
     */
    void extends_(GenericType.OfClass genericType);

    /**
     * Extend the given class.
     *
     * @param desc the class (must not be {@code null})
     */
    void extends_(ClassDesc desc);

    /**
     * Implement a generic interface.
     *
     * @param genericType the generic interface type (must not be {@code null})
     */
    void implements_(GenericType.OfClass genericType);

    /**
     * Implement an interface.
     *
     * @param interface_ the descriptor of the interface (must not be {@code null})
     */
    void implements_(ClassDesc interface_);

    /**
     * Implement an interface.
     *
     * @param interface_ the interface (must not be {@code null})
     */
    default void implements_(Class<?> interface_) {
        if (!interface_.isInterface()) {
            throw new IllegalArgumentException("Only interfaces may be implemented");
        }
        implements_(Util.classDesc(interface_));
    }

    /**
     * Extend the given class.
     *
     * @param clazz the class (must not be {@code null})
     */
    default void extends_(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Classes may only extend classes");
        }
        extends_(Util.classDesc(clazz));
    }

    /**
     * Add an instance field to this class.
     *
     * @param name the field name (must not be {@code null})
     * @param builder the builder (must not be {@code null})
     * @return the field variable (not {@code null})
     */
    FieldDesc field(String name, Consumer<InstanceFieldCreator> builder);

    /**
     * Add an instance field to this class.
     *
     * @param name the field name (must not be {@code null})
     * @param initial the field's initial value (must not be {@code null})
     * @return the field variable (not {@code null})
     */
    default FieldDesc field(String name, Const initial) {
        return field(name, ifc -> {
            ifc.setType(initial.type());
            ifc.setInitial(initial);
        });
    }

    /**
     * Add an instance method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc method(String name, Consumer<InstanceMethodCreator> builder);

    /**
     * Add an instance method to the class having the given predefined type.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc method(String name, MethodTypeDesc type, Consumer<InstanceMethodCreator> builder) {
        return method(name, imc -> {
            imc.setType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add an instance method to the class having the same name and type as the given method.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc method(MethodDesc desc, Consumer<InstanceMethodCreator> builder) {
        return method(desc.name(), desc.type(), builder);
    }

    /**
     * Add an abstract instance method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc abstractMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add an abstract instance method to the class having the given predefined type.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc abstractMethod(String name, MethodTypeDesc type, Consumer<AbstractMethodCreator> builder) {
        return abstractMethod(name, imc -> {
            imc.setType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add an abstract instance method to the class having the same name and type as the given method.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc abstractMethod(MethodDesc desc, Consumer<AbstractMethodCreator> builder) {
        return abstractMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a native instance method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc nativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add a native instance method to the class having the given predefined type.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc nativeMethod(String name, MethodTypeDesc type, Consumer<AbstractMethodCreator> builder) {
        return nativeMethod(name, imc -> {
            imc.setType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a native instance method to the class having the same name and type as the given method.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc nativeMethod(MethodDesc desc, Consumer<AbstractMethodCreator> builder) {
        return nativeMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a native static method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc staticNativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add a native static method to the class having the given predefined type.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc staticNativeMethod(String name, MethodTypeDesc type, Consumer<AbstractMethodCreator> builder) {
        return staticNativeMethod(name, imc -> {
            imc.setType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a native static method to the class having the same name and type as the given method.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc staticNativeMethod(MethodDesc desc, Consumer<AbstractMethodCreator> builder) {
        return staticNativeMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a constructor to the class.
     *
     * @param builder the constructor builder (must not be {@code null})
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    ConstructorDesc constructor(Consumer<ConstructorCreator> builder);

    /**
     * Add a constructor to the class having the given predefined type.
     * The type must have a {@code void} return type.
     *
     * @param type the method type (must not be {@code null})
     * @param builder the constructor builder (must not be {@code null})
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    default ConstructorDesc constructor(MethodTypeDesc type, Consumer<ConstructorCreator> builder) {
        return constructor(imc -> {
            imc.setType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a constructor to the class having the same type as the given constructor.
     *
     * @param desc the original constructor descriptor (must not be {@code null})
     * @param builder the constructor builder (must not be {@code null})
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    default ConstructorDesc constructor(ConstructorDesc desc, Consumer<ConstructorCreator> builder) {
        return constructor(desc.type(), builder);
    }

    /**
     * Add a default constructor to this class.
     *
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    default ConstructorDesc defaultConstructor() {
        ConstructorDesc superCtor = ConstructorDesc.of(superClass());
        return constructor(superCtor, cc -> {
            cc.public_();
            cc.body(bc -> {
                bc.invokeSpecial(superCtor, this_());
                bc.return_();
            });
        });
    }

    /**
     * Add a general instance initializer block to the type.
     * A type may have many instance initializers;
     * they will be concatenated in the order that they are added.
     *
     * @param builder the builder (must not be {@code null})
     */
    void instanceInitializer(Consumer<BlockCreator> builder);

    /**
     * Add the {@code abstract} modifier flag to this creator.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code abstract} modifier flag
     */
    default void abstract_() {
        addFlag(ModifierFlag.ABSTRACT);
    }

    /**
     * Generates a structural {@code equals} method in this class that compares given
     * {@code fields}. The generated code is similar to what IDEs would typically
     * generate from a template:
     * <ol>
     * <li>Reference equality is tested. If {@code this} is identical to the
     * <em>other</em> object, {@code true} is returned.</li>
     * <li>Type of the <em>other</em> object is tested using {@code instanceof}.
     * If the <em>other</em> object is not an instance of this class, {@code false}
     * is returned.</li>
     * <li>All fields are compared. Primitive types are compared using {@code ==},
     * object types are compared using {@code Objects.equals}, single-dimension arrays
     * are compared using {@code Arrays.equals}, and multi-dimensional arrays are
     * compared using {@code Arrays.deepEquals}. If one of the comparisons fails,
     * {@code false} is returned.</li>
     * <li>Otherwise, {@code true} is returned.</li>
     * </ol>
     * <p>
     * If one of the fields doesn't belong to this class, an exception is thrown.
     *
     * @param fields fields to consider in the {@code equals} method (must not be {@code null})
     */
    default void generateEquals(List<FieldDesc> fields) {
        new EqualsHashCodeToStringGenerator(this, fields).generateEquals();
    }

    /**
     * Generates structural {@code equals} and {@code hashCode} methods in this
     * class, based on given {@code fields}. The generated code is similar
     * to what IDEs would typically generate from a template. See
     * {@link #generateEquals(List)} for description of the generated {@code equals}
     * method. The {@code hashCode} method is generated like so:
     * <ol>
     * <li>If no field is given, 0 is returned.</li>
     * <li>Otherwise, a result variable is allocated with initial value of 1.</li>
     * <li>For each field, a hash code is computed. Hash code for primitive types
     * is computed using {@code Integer.hashCode} and equivalent methods, for object
     * types using {@code Objects.hashCode}, for single-dimension arrays using
     * {@code Arrays.hashCode}, and for multi-dimensional arrays using
     * {@code Arrays.deepHashCode}. Then, the result is updated like so:
     * {@code result = 31 * result + fieldHashCode}.</li>
     * <li>At the end, the result is returned.</li>
     * </ol>
     * <p>
     * If one of the fields doesn't belong to this class, an exception is thrown.
     *
     * @param fields fields to consider in the {@code equals} and {@code hashCode} methods (must not be {@code null})
     */
    default void generateEqualsAndHashCode(List<FieldDesc> fields) {
        EqualsHashCodeToStringGenerator generator = new EqualsHashCodeToStringGenerator(this, fields);
        generator.generateEquals();
        generator.generateHashCode();
    }

    /**
     * Generates a {@code toString} methods in this class, based on given {@code fields}.
     * The generated code is similar to what IDEs would typically generate from a template:
     * <ol>
     * <li>An empty {@code StringBuilder} is allocated.</li>
     * <li>Simple name of the class is appended.</li>
     * <li>An opening parenthesis {@code '('} is appended.</li>
     * <li>For each field, its name is appended, followed by the equals sign {@code '='},
     * followed by the field value. Primitive types and object types are appended
     * to the {@code StringBuilder} directly, {@code Arrays.toString} is used for
     * single-dimension arrays, and {@code Arrays.deepToString} for multi-dimensional
     * arrays. A comma followed by a space {@code ", "} are appended between fields.
     * </li>
     * <li>A closing parenthesis {@code ')'} is appended.</li>
     * <li>The {@code StringBuilder.toString()} outcome is returned.</li>
     * </ol>
     * <p>
     * If one of the fields doesn't belong to this class, an exception is thrown.
     *
     * @param fields fields to consider in the {@code toString} methods (must not be {@code null})
     */
    default void generateToString(List<FieldDesc> fields) {
        new EqualsHashCodeToStringGenerator(this, fields).generateToString();
    }
}
