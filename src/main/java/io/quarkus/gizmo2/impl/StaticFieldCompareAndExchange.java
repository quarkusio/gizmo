package io.quarkus.gizmo2.impl;

import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class StaticFieldCompareAndExchange extends CompareAndExchange {
    StaticFieldCompareAndExchange(final StaticFieldVarImpl staticFieldVar, final Item expect, final Item update,
            final MemoryOrder mode) {
        super(staticFieldVar.type(), staticFieldVar.hasGenericType() ? staticFieldVar.genericType() : null, expect, update,
                mode, ConstImpl.ofStaticFieldVarHandle(staticFieldVar.desc()),
                MethodTypeDesc.of(staticFieldVar.type(), staticFieldVar.type(), staticFieldVar.type()));
    }
}
