package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class StaticFieldSetViaHandle extends Item {
    private final StaticFieldVarImpl staticFieldVar;
    private final MemoryOrder mode;
    private final Item value;

    StaticFieldSetViaHandle(final StaticFieldVarImpl staticFieldVar, final MemoryOrder mode, final Item value) {
        this.staticFieldVar = staticFieldVar;
        this.mode = mode;
        this.value = value;
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        return ConstImpl.ofStaticFieldVarHandle(staticFieldVar.desc()).process(value.process(node.prev(), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "set";
            case Opaque -> "setOpaque";
            case Release -> "setRelease";
            case Volatile -> "setVolatile";
            default -> throw impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(CD_void, staticFieldVar.desc().type()));
    }
}
