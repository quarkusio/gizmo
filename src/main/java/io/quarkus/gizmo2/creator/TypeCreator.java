package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.github.dmlloyd.classfile.extras.reflect.ClassFileFormatVersion;
import io.quarkus.gizmo2.Annotatable;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.TypeCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a type.
 */
public sealed interface TypeCreator extends Annotatable, SimpleTyped permits ClassCreator, InterfaceCreator, TypeCreatorImpl {
    /**
     * Set the class file version to correspond with a run time version.
     *
     * @param version the run time version (must not be {@code null})
     */
    default void withVersion(Runtime.Version version) {
        withVersion(ClassFileFormatVersion.valueOf(version));
    }

    /**
     * Set the class file version.
     *
     * @param version the class file version (must not be {@code null})
     */
    void withVersion(ClassFileFormatVersion version);

    /**
     * Add a type parameter.
     *
     * @param param the type parameter specification (must not be {@code null})
     */
    void withTypeParam(Signature.TypeParam param);

    /**
     * Add a flag to the type.
     *
     * @param flag the flag to add (must not be {@code null})
     */
    void withFlag(AccessFlag flag);

    /**
     * Add several flags to the type.
     *
     * @param flags the flags to add
     */
    default void withFlags(Collection<AccessFlag> flags) {
        flags.forEach(this::withFlag);
    }

    /**
     * Add several flags to the type.
     *
     * @param flags the flags to add
     */
    default void withFlags(AccessFlag... flags) {
        for (AccessFlag flag : flags) {
            withFlag(flag);
        }
    }

    /**
     * Add the {@code public} access flag to the type.
     */
    void public_();

    /**
     * Remove the {@code public} access flag.
     */
    void packagePrivate();

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
     * Implement a generic interface.
     *
     * @param genericType the generic interface type (must not be {@code null})
     */
    void implements_(Signature.ClassTypeSig genericType);

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
     * Add a general instance initializer block to the type.
     * A type may have many instance initializers;
     * they will be concatenated in the order that they are added.
     *
     * @param builder the builder (must not be {@code null})
     */
    void instanceInitializer(Consumer<BlockCreator> builder);

    /**
     * Add a static method to this type.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the builder for the method (must not be {@code null})
     * @return the descriptor of the new method (not {@code null})
     */
    MethodDesc staticMethod(String name, Consumer<StaticMethodCreator> builder);

    /**
     * Add a static method to this type having the given predefined method type.
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
     *
     * @param name the field name (must not be {@code null})
     * @param builder the builder for the field (must not be {@code null})
     * @return a variable for the static field (not {@code null})
     */
    StaticFieldVar staticField(String name, Consumer<StaticFieldCreator> builder);

    /**
     * Add a public static final field to this type.
     * 
     * @param name the field name (must not be {@code null})
     * @param value the constant value (must not be {@code null})
     * @return a variable for the static field (not {@code null})
     */
    default StaticFieldVar constantField(String name, Constant value) {
        return staticField(name, sfc -> {
            sfc.public_();
            sfc.final_();
            sfc.withType(value.type());
            sfc.withInitial(value);
        });
    }

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
