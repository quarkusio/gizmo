package io.quarkus.gizmo2.creator;

import io.github.dmlloyd.classfile.extras.constant.ExtraConstantDescs;
import io.quarkus.gizmo2.ConstructorDesc;

public interface ConstructorCreator extends InstanceExecutableCreator {
    ConstructorDesc desc();

    default String name() {
        return ExtraConstantDescs.INIT_NAME;
    }
}
