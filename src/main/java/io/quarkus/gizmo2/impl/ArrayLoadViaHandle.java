package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class ArrayLoadViaHandle extends Item {
    private final ArrayDeref arrayDeref;
    private final MemoryOrder mode;

    ArrayLoadViaHandle(final ArrayDeref arrayDeref, final MemoryOrder mode) {
        this.arrayDeref = arrayDeref;
        this.mode = mode;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return ConstImpl.ofArrayVarHandle(arrayDeref.array().type())
                .process(arrayDeref.array().process(arrayDeref.index().process(node.prev(), op), op), op);
    }

    protected void computeType() {
        initType(arrayDeref.type());
        if (arrayDeref.hasGenericType()) {
            initGenericType(arrayDeref.genericType());
        }
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "get";
            case Opaque -> "getOpaque";
            case Acquire -> "getAcquire";
            case Volatile -> "getVolatile";
            default -> throw impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(type(), arrayDeref.array().type(), CD_int));
    }
}
