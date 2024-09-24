package io.quarkus.gizmo2.impl.constant;

import java.lang.invoke.VarHandle;

import io.quarkus.gizmo2.FieldDesc;

public final class FieldVarHandleConstant extends VarHandleConstant {
    private final FieldDesc field;

    public FieldVarHandleConstant(final FieldDesc field) {
        super(VarHandle.VarHandleDesc.ofField(field.owner(), field.name(), field.type()));
        this.field = field;
    }

    public boolean equals(final VarHandleConstant obj) {
        return obj instanceof FieldVarHandleConstant other && equals(other);
    }

    public boolean equals(final FieldVarHandleConstant other) {
        return this == other || other != null && field.equals(other.field);
    }

    public int hashCode() {
        return field.hashCode();
    }
}
