package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class ArrayStoreViaHandle extends Item {
    private final ArrayDeref arrayDeref;
    private final Item value;

    ArrayStoreViaHandle(final ArrayDeref arrayDeref, final Item value) {
        this.arrayDeref = arrayDeref;
        this.value = value;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return ConstImpl.ofArrayVarHandle(arrayDeref.array().type())
                .process(arrayDeref.array().process(arrayDeref.index().process(value.process(node.prev(), op), op), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokevirtual(CD_VarHandle, "setVolatile", MethodTypeDesc.of(
                type(),
                arrayDeref.array().type(),
                CD_int));
    }
}
