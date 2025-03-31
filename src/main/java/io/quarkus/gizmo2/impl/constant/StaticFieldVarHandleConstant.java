package io.quarkus.gizmo2.impl.constant;

import java.lang.invoke.VarHandle;

import io.quarkus.gizmo2.desc.FieldDesc;

public final class StaticFieldVarHandleConstant extends VarHandleConstant {
    private final FieldDesc field;

    public StaticFieldVarHandleConstant(final FieldDesc field) {
        super(VarHandle.VarHandleDesc.ofStaticField(field.owner(), field.name(), field.type()));
        this.field = field;
    }

    public boolean equals(final VarHandleConstant obj) {
        return obj instanceof StaticFieldVarHandleConstant other && equals(other);
    }

    public boolean equals(final StaticFieldVarHandleConstant other) {
        return this == other || other != null && field.equals(other.field);
    }

    public int hashCode() {
        return field.hashCode();
    }

    public StringBuilder toShortString(final StringBuilder b) {
        b.append("VarHandle[static ");
        field.toString(b);
        return b.append(']');
    }
}
