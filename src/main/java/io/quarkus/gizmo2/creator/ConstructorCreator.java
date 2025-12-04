package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.impl.ConstructorCreatorImpl;
import io.smallrye.classfile.extras.constant.ExtraConstantDescs;

/**
 * A creator for an instance constructor.
 */
public sealed interface ConstructorCreator extends InstanceExecutableCreator, MemberCreator permits ConstructorCreatorImpl {
    /**
     * {@return the descriptor of the constructor (not {@code null})}
     */
    ConstructorDesc desc();

    default String name() {
        return ExtraConstantDescs.INIT_NAME;
    }
}
