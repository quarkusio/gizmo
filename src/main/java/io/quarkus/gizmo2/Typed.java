package io.quarkus.gizmo2;

import java.lang.constant.ConstantDesc;

import io.quarkus.gizmo2.creator.MemberCreator;
import io.quarkus.gizmo2.desc.MemberDesc;

/**
 * A thing which has a type.
 */
public sealed interface Typed permits MethodTyped, SimpleTyped, MemberCreator, MemberDesc {
    /**
     * {@return the type of this entity (not {@code null})}
     */
    ConstantDesc type();
}
