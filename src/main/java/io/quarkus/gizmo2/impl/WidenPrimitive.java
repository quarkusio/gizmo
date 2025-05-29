package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.primitiveWideningExists;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

// variant of `PrimitiveCast` that:
// - only does widening conversions
// - is not bound initially
final class WidenPrimitive extends Cast {
    private final PrimitiveCast widening;
    private boolean bound;

    WidenPrimitive(Expr a, ClassDesc toType) {
        super(a, toType);
        if (!primitiveWideningExists(a.type(), toType)) {
            throw new IllegalArgumentException("No primitive widening conversion from " + a.type().displayName()
                    + " to " + toType.displayName());
        }
        this.widening = new PrimitiveCast(a, toType);
    }

    @Override
    public boolean bound() {
        return bound;
    }

    @Override
    protected void bind() {
        bound = true;
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block) {
        widening.writeCode(cb, block);
    }
}
