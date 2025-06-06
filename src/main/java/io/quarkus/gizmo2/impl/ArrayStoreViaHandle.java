package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.smallrye.common.constraint.Assert;

final class ArrayStoreViaHandle extends Item {
    private final ArrayDeref arrayDeref;
    private final Item value;
    private final MemoryOrder mode;

    ArrayStoreViaHandle(final ArrayDeref arrayDeref, final Item value, final MemoryOrder mode) {
        this.arrayDeref = arrayDeref;
        this.value = value;
        this.mode = mode;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return ConstImpl.ofArrayVarHandle(arrayDeref.array().type())
                .process(arrayDeref.array().process(arrayDeref.index().process(value.process(node.prev(), op), op), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "set";
            case Opaque -> "setOpaque";
            case Release -> "setRelease";
            case Volatile -> "setVolatile";
            default -> throw Assert.impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(CD_void, arrayDeref.array().type(), CD_int, arrayDeref.type()));
    }
}
