package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.impl.FieldCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a field.
 */
public sealed interface FieldCreator extends MemberCreator permits InstanceFieldCreator, StaticFieldCreator, FieldCreatorImpl {
    /**
     * {@return the field type descriptor}
     */
    FieldDesc desc();

    /**
     * Change the type of the field to the given generic type.
     *
     * @param type the class type signature (must not be {@code null})
     */
    void withTypeSignature(Signature.ClassTypeSig type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type descriptor (must not be {@code null})
     */
    void withType(ClassDesc type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type (must not be {@code null})
     */
    default void withType(Class<?> type) {
        withType(Util.classDesc(type));
    }
}
