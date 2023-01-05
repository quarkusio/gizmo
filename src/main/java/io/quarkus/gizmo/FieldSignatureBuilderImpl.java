package io.quarkus.gizmo;

import java.util.Objects;

import io.quarkus.gizmo.SignatureBuilder.FieldSignatureBuilder;

class FieldSignatureBuilderImpl implements FieldSignatureBuilder {

    private Type type;

    @Override
    public String build() {
        return type.toSignature();
    }

    @Override
    public FieldSignatureBuilder setType(Type type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }

}