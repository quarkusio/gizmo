package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class StaticFieldCompareAndSet extends CompareAndSet {
    StaticFieldCompareAndSet(final StaticFieldVarImpl staticFieldVar, final Item expect, final Item update, final boolean weak,
            final MemoryOrder mode) {
        super(expect, update, weak, mode, ConstImpl.ofStaticFieldVarHandle(staticFieldVar.desc()),
                MethodTypeDesc.of(CD_boolean, staticFieldVar.type(), staticFieldVar.type()));
    }
}
