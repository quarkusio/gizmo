package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.impl.AnonymousClassCreatorImpl;

/**
 * A class creator for classes which capture an enclosing lexical scope.
 */
public sealed interface AnonymousClassCreator extends ClassCreator, CapturingCreator permits AnonymousClassCreatorImpl {
}
