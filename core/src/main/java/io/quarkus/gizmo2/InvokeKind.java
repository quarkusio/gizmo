package io.quarkus.gizmo2;

import java.util.List;

/**
 * The kind of invocation.
 */
public enum InvokeKind {
    /**
     * A virtual invocation.
     */
    VIRTUAL,
    /**
     * An interface invocation.
     */
    INTERFACE,
    /**
     * A so-called "special" invocation.
     */
    SPECIAL,
    /**
     * A static invocation.
     */
    STATIC,
    ;

    /**
     * An immutable list of all values of this type.
     */
    public static final List<InvokeKind> values = List.of(values());
}
