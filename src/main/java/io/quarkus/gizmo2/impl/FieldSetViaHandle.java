package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class FieldSetViaHandle extends Item {
    private final FieldDeref fieldDeref;
    private final Item value;
    private final MemoryOrder mode;

    FieldSetViaHandle(final FieldDeref fieldDeref, final Item value, final MemoryOrder mode) {
        this.fieldDeref = fieldDeref;
        this.value = value;
        this.mode = mode;
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        return ConstImpl.ofFieldVarHandle(fieldDeref.desc())
                .process(fieldDeref.process(value.process(node.prev(), op), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "set";
            case Opaque -> "setOpaque";
            case Release -> "setRelease";
            case Volatile -> "setVolatile";
            default -> throw new IllegalStateException();
        }, MethodTypeDesc.of(
                fieldDeref.desc().type(),
                Util.NO_DESCS));
    }
}
