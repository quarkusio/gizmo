package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.Optional;

import io.quarkus.gizmo2.impl.SourceBuilder;
import io.quarkus.gizmo2.impl.SourceGenerator;

public final class MethodTypeConst extends ConstImpl {
    private final MethodTypeDesc desc;

    MethodTypeConst(final MethodTypeDesc desc) {
        super(ConstantDescs.CD_MethodType);
        this.desc = desc;
    }

    /**
     * {@return the method type descriptor}
     */
    public MethodTypeDesc methodTypeDesc() {
        return desc;
    }

    public boolean isNonZero() {
        return true;
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof MethodTypeConst other && equals(other);
    }

    public boolean equals(final MethodTypeConst other) {
        return this == other || other != null && desc.equals(other.desc);
    }

    public int hashCode() {
        return desc.hashCode();
    }

    public ConstantDesc desc() {
        return desc;
    }

    public Optional<MethodTypeDesc> describeConstable() {
        return Optional.of(desc);
    }

    public StringBuilder toShortString(final StringBuilder b) {
        b.append('(');
        int pc = desc.parameterCount();
        for (int i = 0; i < pc; i++) {
            b.append(desc.parameterType(i).descriptorString());
        }
        return b.append(')').append(desc.returnType().descriptorString());
    }

    /** {@inheritDoc} */
    @Override
    protected StringBuilder appendSourceExpr(StringBuilder buf, SourceBuilder sb) {
        return SourceGenerator.exprMethodTypeConst(this, buf, sb);
    }
}
