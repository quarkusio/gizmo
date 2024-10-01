package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.util.List;
import java.util.Optional;

public final class InvokeConstant extends ConstantImpl {
    private final MethodHandleConstant handleConstant;
    private final List<ConstantImpl> args;

    InvokeConstant(final MethodHandleConstant handleConstant, final List<ConstantImpl> args) {
        super(handleConstant.desc().invocationType().returnType());
        this.handleConstant = handleConstant;
        this.args = args;
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof InvokeConstant other && equals(other);
    }

    public boolean equals(final InvokeConstant other) {
        return this == other || other != null && handleConstant.equals(other.handleConstant) && args.equals(other.args);
    }

    public int hashCode() {
        return handleConstant.hashCode() * 19 + args.hashCode();
    }

    public ConstantDesc desc() {
        return DynamicConstantDesc.of(
            ConstantDescs.BSM_INVOKE,
            args.stream().map(ConstantImpl::describeConstable).map(Optional::orElseThrow).toArray(ConstantDesc[]::new)
        );
    }

    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(desc());
    }
}
