package io.quarkus.gizmo2.impl.constant;

import java.lang.invoke.VarHandle;

import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.SourceBuilder;
import io.quarkus.gizmo2.impl.SourceGenerator;

public final class FieldVarHandleConst extends VarHandleConst {
    private final FieldDesc field;

    public FieldVarHandleConst(final FieldDesc field) {
        super(VarHandle.VarHandleDesc.ofField(field.owner(), field.name(), field.type()));
        this.field = field;
    }

    public boolean equals(final VarHandleConst obj) {
        return obj instanceof FieldVarHandleConst other && equals(other);
    }

    public boolean equals(final FieldVarHandleConst other) {
        return this == other || other != null && field.equals(other.field);
    }

    public int hashCode() {
        return field.hashCode();
    }

    /**
     * {@return the field descriptor for this var handle}
     */
    public FieldDesc field() {
        return field;
    }

    public StringBuilder toShortString(final StringBuilder b) {
        b.append("VarHandle[");
        field.toString(b);
        return b.append(']');
    }

    /** {@inheritDoc} */
    @Override
    protected StringBuilder appendSourceExpr(StringBuilder buf, SourceBuilder sb) {
        return SourceGenerator.exprFieldVarHandleConst(this, buf, sb);
    }
}
