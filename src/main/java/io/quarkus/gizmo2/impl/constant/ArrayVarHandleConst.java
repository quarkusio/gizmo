package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.invoke.VarHandle;

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

    public StringBuilder toShortString(final StringBuilder b) {
        return Util.descName(b.append("VarHandle["), arrayType).append(']');
    }
}
