package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * This class is used for special cases where we need
 * an item which represents an indy which yields a static
 * call site, from which we obtain a static method handle.
 */
public final class BootstrappedMethodHandleImpl extends Item {
    private final ClassDesc owner;
    private final MethodDesc bootstrapMethodDesc;
    private final MethodTypeDesc methodHandleType;
    private final List<Const> bootstrapArguments;

    public BootstrappedMethodHandleImpl(final ClassDesc owner, final MethodDesc bootstrapMethodDesc,
            final MethodTypeDesc methodHandleType, final List<Const> bootstrapArguments) {
        this.owner = owner;
        this.bootstrapMethodDesc = bootstrapMethodDesc;
        this.methodHandleType = methodHandleType;
        this.bootstrapArguments = bootstrapArguments;
    }

    public ClassDesc type() {
        return ConstantDescs.CD_MethodHandle;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokedynamic(DynamicCallSiteDesc.of(
                MethodHandleDesc.ofMethod(
                        bootstrapMethodDesc instanceof InterfaceMethodDesc ? DirectMethodHandleDesc.Kind.INTERFACE_STATIC
                                : DirectMethodHandleDesc.Kind.STATIC,
                        owner,
                        bootstrapMethodDesc.name(),
                        bootstrapMethodDesc.type()),
                "_",
                methodHandleType,
                bootstrapArguments.stream().map(Const::desc).toArray(ConstantDesc[]::new)));
        // now extract the method handle from the call site
        cb.invokevirtual(
                ConstantDescs.CD_MethodHandle,
                "getMethodHandle",
                MethodTypeDesc.of(ConstantDescs.CD_MethodHandle));
    }
}
