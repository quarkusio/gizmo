package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.github.dmlloyd.classfile.extras.reflect.ClassFileFormatVersion;
import io.quarkus.gizmo2.Annotatable;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.impl.TypeCreatorImpl;

/**
 * A creator for a type.
 */
public sealed interface TypeCreator extends Annotatable permits ClassCreator, InterfaceCreator, TypeCreatorImpl {
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
        if (! interface_.isInterface()) {
            throw new IllegalArgumentException("Only interfaces may be implemented");
        }
    }

    /**
     * Add a general static initializer block to the type.
     * A type may have many static initializers;
     * they will be concatenated in the order that they are added.
     *
     * @param builder the builder (must not be {@code null})
     */
    void initializer(Consumer<BlockCreator> builder);

    /**
     * Add a static method to this type.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the builder for the method (must not be {@code null})
     * @return the descriptor of the new method (not {@code null})
     */
    MethodDesc staticMethod(String name, Consumer<StaticMethodCreator> builder);

    /**
     * Add a static field to this type.
     *
     * @param name the field name (must not be {@code null})
     * @param builder the builder for the field (must not be {@code null})
     * @return a variable for the static field (not {@code null})
     */
    StaticFieldVar staticField(String name, Consumer<StaticFieldCreator> builder);
}
