package io.quarkus.gizmo2;

import java.util.List;

/**
 * Possible lambda generation strategies.
 */
public enum LambdaStrategy {
    /**
     * The default optimized lambda generation strategy.
     * A hidden class is generated which implements the lambda type.
     */
    OPTIMIZED,
    /**
     * The classic lambda generation strategy.
     * The JDK lambda metafactory is used to create a SAM implementation/method handle pair.
     */
    CLASSIC,
    /**
     * The anonymous class lambda generation strategy.
     * The lambda body is generated as an anonymous class which is loaded by the host class loader.
     */
    ANONYMOUS_CLASS,
    ;

    /**
     * The list of possible values for this type.
     */
    public static final List<LambdaStrategy> values = List.of(values());
}
