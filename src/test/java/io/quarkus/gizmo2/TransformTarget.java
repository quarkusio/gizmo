package io.quarkus.gizmo2;

/**
 * Input class for {@link TransformerTest}.
 */
@Deprecated
final class TransformTarget {
    @Deprecated
    private String name = "target";
    private int unused;

    private TransformTarget() {
    }

    public final String hello() {
        return "hello " + name;
    }

    private static String secret() {
        return "secret";
    }

    public void gone() {
    }
}
