package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class InvokeConst extends ConstImpl {
    private final MethodHandleConst handleConstant;
    private final List<ConstImpl> args;

    InvokeConst(final MethodHandleConst handleConstant, final List<ConstImpl> args) {
        super(handleConstant.desc().invocationType().returnType());
        this.handleConstant = handleConstant;
        this.args = args;
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof InvokeConst other && equals(other);
    }

    public boolean equals(final InvokeConst other) {
        return this == other || other != null && handleConstant.equals(other.handleConstant) && args.equals(other.args);
    }

    public int hashCode() {
        return handleConstant.hashCode() * 19 + args.hashCode();
    }

    public ConstantDesc desc() {
        return DynamicConstantDesc.of(
                ConstantDescs.BSM_INVOKE,
                Stream.concat(
                        Stream.of(handleConstant.desc()),
                        args.stream().map(ConstImpl::describeConstable).map(Optional::orElseThrow))
                        .toArray(ConstantDesc[]::new));
    }

    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(desc());
    }

    public StringBuilder toShortString(final StringBuilder b) {
        b.append("Invoke[");
        handleConstant.toShortString(b).append("](");
        Iterator<ConstImpl> iterator = args.iterator();
        if (iterator.hasNext()) {
            handleConstant.toShortString(b);
            while (iterator.hasNext()) {
                b.append(',');
                handleConstant.toShortString(b);
            }
        }
        return b.append(')');
    }
}
