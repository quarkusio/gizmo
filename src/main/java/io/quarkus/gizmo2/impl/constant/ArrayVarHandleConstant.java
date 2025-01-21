package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.invoke.VarHandle;

import io.quarkus.gizmo2.impl.Util;

public final class ArrayVarHandleConstant extends VarHandleConstant {
    private final ClassDesc arrayType;

    public ArrayVarHandleConstant(final ClassDesc arrayType) {
        super(VarHandle.VarHandleDesc.ofArray(arrayType));
        this.arrayType = arrayType;
    }

    public boolean equals(final VarHandleConstant obj) {
        return obj instanceof ArrayVarHandleConstant other && equals(other);
    }

    public boolean equals(final ArrayVarHandleConstant other) {
        return this == other || other != null && arrayType.equals(other.arrayType);
    }

    public int hashCode() {
        return arrayType.hashCode();
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return Util.descName(b.append("VarHandle["), arrayType).append(']');
    }
}
