package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class StaticFieldGetViaHandle extends Item {
    private final StaticFieldVarImpl staticFieldVar;
    private final MemoryOrder mode;

    StaticFieldGetViaHandle(final StaticFieldVarImpl staticFieldVar, final MemoryOrder mode) {
        super(staticFieldVar.type(), staticFieldVar.hasGenericType() ? staticFieldVar.genericType() : null);
        this.staticFieldVar = staticFieldVar;
        this.mode = mode;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return ConstImpl.ofStaticFieldVarHandle(staticFieldVar.desc()).process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "get";
            case Opaque -> "getOpaque";
            case Acquire -> "getAcquire";
            case Volatile -> "getVolatile";
            default -> throw impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(type()));
    }
}
