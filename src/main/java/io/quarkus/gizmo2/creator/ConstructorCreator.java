package io.quarkus.gizmo2.creator;

import io.github.dmlloyd.classfile.extras.constant.ExtraConstantDescs;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.impl.ConstructorCreatorImpl;

/**
 * A creator for an instance constructor.
 */
public sealed interface ConstructorCreator extends InstanceExecutableCreator permits ConstructorCreatorImpl {
    /**
     * {@return the descriptor of the constructor (not {@code null})}
     */
    ConstructorDesc desc();

    default String name() {
        return ExtraConstantDescs.INIT_NAME;
    }
}
