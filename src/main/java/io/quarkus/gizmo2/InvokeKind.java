package io.quarkus.gizmo2;

import java.util.List;

/**
 * The kind of invocation.
 */
public enum InvokeKind {
    VIRTUAL,
    INTERFACE,
    SPECIAL,
    STATIC,
    ;

    public static final List<InvokeKind> values = List.of(values());
}
