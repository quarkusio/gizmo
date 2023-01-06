package io.quarkus.gizmo;

import java.util.Objects;

import io.quarkus.gizmo.SignatureBuilder.FieldSignatureBuilder;

class FieldSignatureBuilderImpl implements FieldSignatureBuilder {
    private Type type;

    @Override
    public String build() {
        StringBuilder signature = new StringBuilder();
        type.appendToSignature(signature);
        return signature.toString();
    }

    @Override
    public FieldSignatureBuilder setType(Type type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }
}
