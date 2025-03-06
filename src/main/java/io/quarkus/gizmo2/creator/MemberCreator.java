package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Annotatable;

/**
 * A generalized creator for any kind of class member.
 */
public sealed interface MemberCreator extends Annotatable permits ExecutableCreator, FieldCreator {
    /**
     * Add the given flag to this member.
     *
     * @param flag the flag to add (must not be {@code null})
     */
    void withFlag(AccessFlag flag);

    /**
     * {@return the descriptor of the class which contains this member}
     */
    ClassDesc owner();

    /**
     * {@return the member name}
     */
    String name();
}
