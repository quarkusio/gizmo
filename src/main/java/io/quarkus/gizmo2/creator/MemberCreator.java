package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;

import io.quarkus.gizmo2.Typed;
import io.quarkus.gizmo2.desc.MemberDesc;

/**
 * A generalized creator for any kind of class member.
 */
public sealed interface MemberCreator extends ModifiableCreator, Typed
        permits ConstructorCreator, FieldCreator, MethodCreator {

    /**
     * {@return the descriptor of the member}
     */
    MemberDesc desc();

    /**
     * {@return the type of this member (not {@code null})}
     */
    ConstantDesc type();

    /**
     * {@return the descriptor of the class which contains this member}
     */
    ClassDesc owner();

    /**
     * {@return the member name}
     */
    String name();
}
