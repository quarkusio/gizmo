package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.FieldDesc;

/**
 * A descriptor for a class member.
 */
public sealed interface MemberDesc permits ConstructorDesc, FieldDesc, MethodDesc {
    /**
     * {@return the descriptor of the class which contains the described member}
     */
    ClassDesc owner();

    /**
     * {@return the member name}
     */
    String name();

    /**
     * Convert this descriptor to a string and append it to the given builder.
     *
     * @param b the string builder to append to (must not be {@code null})
     * @return the string builder
     */
    StringBuilder toString(StringBuilder b);
}
