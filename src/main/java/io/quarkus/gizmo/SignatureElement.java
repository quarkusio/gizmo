package io.quarkus.gizmo;

/**
 * An element that has a signature attribute
 */
public interface SignatureElement<S> {

    String getSignature();

    S setSignature(String signature);
}
