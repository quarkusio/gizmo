package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Annotatable;
import io.quarkus.gizmo2.impl.ParamCreatorImpl;

/**
 * A creator interface for parameters.
 */
public sealed interface ParamCreator extends Annotatable permits ParamCreatorImpl {
    void withFlag(AccessFlag flag);

    void withType(ClassDesc type);
}
