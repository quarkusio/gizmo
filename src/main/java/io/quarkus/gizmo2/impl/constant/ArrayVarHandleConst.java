package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.invoke.VarHandle;

import io.quarkus.gizmo2.impl.SourceBuilder;
import io.quarkus.gizmo2.impl.SourceGenerator;
import io.quarkus.gizmo2.impl.Util;

public final class ArrayVarHandleConst extends VarHandleConst {
    private final ClassDesc arrayType;

    public ArrayVarHandleConst(final ClassDesc arrayType) {
        super(VarHandle.VarHandleDesc.ofArray(arrayType));
        this.arrayType = arrayType;
    }

    public boolean equals(final VarHandleConst obj) {
        return obj instanceof ArrayVarHandleConst other && equals(other);
    }

    public boolean equals(final ArrayVarHandleConst other) {
        return this == other || other != null && arrayType.equals(other.arrayType);
    }

    public int hashCode() {
        return arrayType.hashCode();
    }

    /**
     * {@return the array type descriptor for this array var handle}
     */
    public ClassDesc arrayType() {
        return arrayType;
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return Util.descName(b.append("VarHandle["), arrayType).append(']');
    }

    /** {@inheritDoc} */
    @Override
    protected StringBuilder appendSourceExpr(StringBuilder buf, SourceBuilder sb) {
        return SourceGenerator.exprArrayVarHandleConst(this, buf, sb);
    }
}
