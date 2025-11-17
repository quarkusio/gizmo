package io.quarkus.gizmo2.impl;

import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class StaticFieldReadModifyWrite extends ReadModifyWrite {
    StaticFieldReadModifyWrite(final StaticFieldVarImpl staticFieldVar, final String op, final Item value,
            final MemoryOrder mode) {
        super(staticFieldVar.type(), staticFieldVar.hasGenericType() ? staticFieldVar.genericType() : null, op, value, mode,
                ConstImpl.ofStaticFieldVarHandle(staticFieldVar.desc()),
                MethodTypeDesc.of(staticFieldVar.type(), staticFieldVar.type()));
    }
}
