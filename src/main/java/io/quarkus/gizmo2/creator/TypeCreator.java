package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.Set;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.github.dmlloyd.classfile.extras.reflect.ClassFileFormatVersion;
import io.quarkus.gizmo2.Annotatable;
import io.quarkus.gizmo2.MethodDesc;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.impl.TypeCreatorImpl;

public sealed interface TypeCreator extends Annotatable permits ClassCreator, InterfaceCreator, TypeCreatorImpl {
    default void withVersion(Runtime.Version version) {
        withVersion(ClassFileFormatVersion.valueOf(version));
    }

    void withVersion(ClassFileFormatVersion version);

    void withTypeParam(Signature.TypeParam param);

    void withFlag(AccessFlag flag);

    void withFlags(Set<AccessFlag> flags);

    void sourceFile(String name);

    ClassDesc type();

    void implements_(Signature.ClassTypeSig genericType);

    void implements_(ClassDesc interface_);

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

    MethodDesc staticMethod(String name, Consumer<StaticMethodCreator> builder);

    StaticFieldVar staticField(String name, Consumer<StaticFieldCreator> builder);
}
