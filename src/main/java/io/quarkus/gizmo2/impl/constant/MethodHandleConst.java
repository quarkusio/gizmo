package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.util.Optional;

import io.quarkus.gizmo2.InvokeKind;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public final class MethodHandleConst extends ConstImpl {
    private final MethodHandleDesc desc;

    MethodHandleConst(final InvokeKind kind, final MethodDesc desc) {
        this(MethodHandleDesc.ofMethod(switch (kind) {
            case STATIC -> desc instanceof InterfaceMethodDesc
                    ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC
                    : DirectMethodHandleDesc.Kind.STATIC;
            case VIRTUAL -> desc instanceof InterfaceMethodDesc
                    ? DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL
                    : DirectMethodHandleDesc.Kind.VIRTUAL;
            case INTERFACE -> DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL;
            case SPECIAL -> desc instanceof InterfaceMethodDesc
                    ? DirectMethodHandleDesc.Kind.INTERFACE_SPECIAL
                    : DirectMethodHandleDesc.Kind.SPECIAL;
        }, desc.owner(), desc.name(), desc.type()));
    }

    MethodHandleConst(final ConstructorDesc desc) {
        this(MethodHandleDesc.ofConstructor(desc.owner(), desc.type().parameterArray()));
    }

    MethodHandleConst(final FieldDesc desc, final boolean static_, final boolean getter) {
        this(MethodHandleDesc.ofField(
                static_ ? getter ? DirectMethodHandleDesc.Kind.STATIC_GETTER : DirectMethodHandleDesc.Kind.STATIC_SETTER
                        : getter ? DirectMethodHandleDesc.Kind.GETTER : DirectMethodHandleDesc.Kind.SETTER,
                desc.owner(),
                desc.name(),
                desc.type()));
    }

    MethodHandleConst(MethodHandleDesc desc) {
        super(ConstantDescs.CD_MethodHandle);
        this.desc = desc;
    }

    public boolean isNonZero() {
        return true;
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof MethodHandleConst other && equals(other);
    }

    public int hashCode() {
        return desc.hashCode();
    }

    public MethodHandleDesc desc() {
        return desc;
    }

    public Optional<MethodHandleDesc> describeConstable() {
        return Optional.of(desc);
    }
}
