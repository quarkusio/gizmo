package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.ClassVersion;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.GenericTyped;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.TypeCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a type.
 */
public sealed interface TypeCreator extends AnnotatableCreator, GenericTyped
        permits ClassCreator, InterfaceCreator, TypeCreatorImpl {
    /**
     * Set the class file version to correspond with a run time version.
     * If not called, the generated class has the version of Java 17.
     *
     * @param version the run time version (must not be {@code null})
     */
    void withVersion(Runtime.Version version);

    /**
     * Set the class file version.
     * If not called, the generated class has the version of Java 17.
     *
     * @param version the class file version (must not be {@code null})
     */
    void withVersion(ClassVersion version);

    /**
     * Set the source file name for this type.
     *
     * @param name the source file name (must not be {@code null})
     */
    void sourceFile(String name);

    /**
     * {@return the descriptor of the type of this class}
     */
    ClassDesc type();

    /**
     * {@return the generic type of this class}
     */
    GenericType.OfClass genericType();

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
     * Add a general static initializer block to the type.
     * A type may have many static initializers;
     * they will be concatenated in the order that they are added.
     *
     * @param builder the builder (must not be {@code null})
     */
    void staticInitializer(Consumer<BlockCreator> builder);

    /**
     * Add a static method to this type.
     * <p>
     * Static methods on interfaces are always {@code public}.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the builder for the method (must not be {@code null})
     * @return the descriptor of the new method (not {@code null})
     */
    MethodDesc staticMethod(String name, Consumer<StaticMethodCreator> builder);

    /**
     * Add a static method to this type having the given predefined method type.
     * <p>
     * Static methods on interfaces are always {@code public}.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the builder for the method (must not be {@code null})
     * @return the descriptor of the new method (not {@code null})
     */
    default MethodDesc staticMethod(String name, MethodTypeDesc type, Consumer<StaticMethodCreator> builder) {
        return staticMethod(name, smc -> {
            smc.withType(type);
            builder.accept(smc);
        });
    }

    /**
     * Add a static method to this type having the same name and type as the given method.
     * <p>
     * Static methods on interfaces are always {@code public}.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the builder for the method (must not be {@code null})
     * @return the descriptor of the new method (not {@code null})
     */
    default MethodDesc staticMethod(MethodDesc desc, Consumer<StaticMethodCreator> builder) {
        return staticMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a static field to this type.
     * <p>
     * Static fields on interfaces are always {@code public} and {@code final}.
     *
     * @param name the field name (must not be {@code null})
     * @param builder the builder for the field (must not be {@code null})
     * @return a variable for the static field (not {@code null})
     */
    StaticFieldVar staticField(String name, Consumer<StaticFieldCreator> builder);

    /**
     * Add a static field to this type.
     *
     * @param name the field name (must not be {@code null})
     * @param initial the field's initial value (must not be {@code null})
     * @return a variable for the static field (not {@code null})
     */
    default StaticFieldVar staticField(String name, Const initial) {
        return staticField(name, sfc -> {
            sfc.withInitial(initial);
        });
    }

    /**
     * Add a public static final field to this type. The field is initialized to the given {@code value}.
     *
     * @param name the field name (must not be {@code null})
     * @param value the constant value (must not be {@code null})
     * @return a variable for the static field (not {@code null})
     */
    default StaticFieldVar constantField(String name, Const value) {
        return staticField(name, sfc -> {
            sfc.withAccess(AccessLevel.PUBLIC);
            sfc.withFlag(ModifierFlag.FINAL);
            sfc.withInitial(value);
        });
    }

    /**
     * Create a private constant which loads the given list of strings from a generated resource file.
     * The constant may not be used outside of this class.
     * Any number of strings may be stored in the constant;
     * however, for smaller lists, {@link Const#of(List)} is preferred.
     *
     * @param name the constant name (must not be {@code null})
     * @param items the list of strings for the constant (must not be {@code null})
     * @return the constant (not {@code null})
     */
    Const stringListResourceConstant(String name, List<String> items);

    /**
     * Create a private constant which loads the given set of strings from a generated resource file.
     * The constant may not be used outside of this class.
     * Any number of strings may be stored in the constant;
     * however, for smaller sets, {@link Const#of(Set)} is preferred.
     *
     * @param name the constant name (must not be {@code null})
     * @param items the set of strings for the constant (must not be {@code null})
     * @return the constant (not {@code null})
     */
    Const stringSetResourceConstant(String name, Set<String> items);

    /**
     * Create a private constant which loads the given map of strings from a generated resource file.
     * The constant may not be used outside of this class.
     * Any number of strings may be stored in the constant;
     * however, for smaller maps, {@link Const#of(Map)} is preferred.
     *
     * @param name the constant name (must not be {@code null})
     * @param items the map of strings for the constant (must not be {@code null})
     * @return the constant (not {@code null})
     */
    Const stringMapResourceConstant(String name, Map<String, String> items);

    /**
     * {@return the {@code this} expression}
     * This expression is only valid for instance methods and constructors.
     */
    This this_();

    /**
     * {@return a list of descriptors of all static fields added to this class so far}
     */
    List<FieldDesc> staticFields();

    /**
     * {@return a list of descriptors of all instance fields added to this class so far}
     */
    List<FieldDesc> instanceFields();

    /**
     * {@return a list of descriptors of all static methods added to this class so far}
     */
    List<MethodDesc> staticMethods();

    /**
     * {@return a list of descriptors of all instance methods added to this class so far}
     */
    List<MethodDesc> instanceMethods();

    /**
     * {@return a list of descriptors of all constructors added to this class so far}
     */
    List<ConstructorDesc> constructors();

}
