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
     * Add the {@code public} access flag to the member.
     * Remove {@code private} and {@code protected}.
     */
    void public_();

    /**
     * Remove {@code public}, {@code private} and {@code protected} access flags.
     */
    void packagePrivate();

    /**
     * Add the {@code private} access flag to the member.
     * Remove {@code public} and {@code protected}.
     */
    void private_();

    /**
     * Add the {@code protected} access flag to the member.
     * Remove {@code public} and {@code private}.
     */
    void protected_();

    /**
     * Add the {@code final} access flag to the member.
     */
    void final_();

    /**
     * {@return the descriptor of the class which contains this member}
     */
    ClassDesc owner();

    /**
     * {@return the member name}
     */
    String name();
}
