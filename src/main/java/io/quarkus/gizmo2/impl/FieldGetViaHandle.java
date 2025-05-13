package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class FieldGetViaHandle extends Item {
    private final FieldDeref fieldDeref;
    private final MemoryOrder mode;

    FieldGetViaHandle(final FieldDeref fieldDeref, final MemoryOrder mode) {
        this.fieldDeref = fieldDeref;
        this.mode = mode;
    }

    public ClassDesc type() {
        return fieldDeref.type();
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return ConstImpl.ofFieldVarHandle(fieldDeref.desc()).process(fieldDeref.process(node.prev(), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "get";
            case Opaque -> "getOpaque";
            case Acquire -> "getAcquire";
            case Volatile -> "getVolatile";
            default -> throw new IllegalStateException();
        }, MethodTypeDesc.of(type()));
    }
}
