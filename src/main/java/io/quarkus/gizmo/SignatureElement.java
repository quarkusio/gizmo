package io.quarkus.gizmo;

/**
 * An element that has a signature attribute.
 */
public interface SignatureElement<S> {

    String getSignature();

    /**
     * Use the convenient {@link SignatureBuilder} to build signatures for classes, methods and fields.
     *
     * @param signature The generic signature as defined in JVMS 17, chapter "4.7.9.1. Signatures"
     * @return the element
     * @see SignatureBuilder
     */
    S setSignature(String signature);
}
