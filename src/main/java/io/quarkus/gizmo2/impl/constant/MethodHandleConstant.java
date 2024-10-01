package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodHandleDesc;
import java.util.Optional;

import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.InvokeKind;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public final class MethodHandleConstant extends ConstantImpl {
    private final MethodHandleDesc desc;

    MethodHandleConstant(final InvokeKind kind, final MethodDesc desc) {
        this(MethodHandleDesc.ofMethod(switch (kind) {
            case STATIC -> desc instanceof InterfaceMethodDesc ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC : DirectMethodHandleDesc.Kind.STATIC;
            case VIRTUAL -> desc instanceof InterfaceMethodDesc ? DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL : DirectMethodHandleDesc.Kind.VIRTUAL;
            case INTERFACE -> DirectMethodHandleDesc.Kind.INTERFACE_VIRTUAL;
            case SPECIAL -> desc instanceof InterfaceMethodDesc ? DirectMethodHandleDesc.Kind.INTERFACE_SPECIAL : DirectMethodHandleDesc.Kind.SPECIAL;
        }, desc.owner(), desc.name(), desc.type()));
    }

    MethodHandleConstant(final ConstructorDesc desc) {
        this(MethodHandleDesc.ofConstructor(desc.owner(), desc.type().parameterArray()));
    }

    MethodHandleConstant(final FieldDesc desc, final boolean static_, final boolean getter) {
        this(MethodHandleDesc.ofField(
            static_ ?
            getter ? DirectMethodHandleDesc.Kind.STATIC_GETTER : DirectMethodHandleDesc.Kind.STATIC_SETTER :
            getter ? DirectMethodHandleDesc.Kind.GETTER : DirectMethodHandleDesc.Kind.SETTER,
            desc.owner(),
            desc.name(),
            desc.type()
        ));
    }

    MethodHandleConstant(MethodHandleDesc desc) {
        super(ConstantDescs.CD_MethodHandle);
        this.desc = desc;
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof MethodHandleConstant other && equals(other);
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
