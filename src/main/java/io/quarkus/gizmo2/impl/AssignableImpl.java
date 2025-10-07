package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.MemoryOrder;

public non-sealed abstract class AssignableImpl extends Item implements Assignable {
    AssignableImpl() {
    }

    AssignableImpl(final ClassDesc type) {
        super(type);
    }

    AssignableImpl(final GenericType genericType) {
        super(genericType);
    }

    AssignableImpl(final ClassDesc type, final GenericType genericType) {
        super(type, genericType);
    }

    abstract Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode);

    abstract Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode);

    void emitInc(final BlockCreatorImpl block, final Const amount) {
        block.set(this, block.add(this, amount));
    }

    void emitDec(final BlockCreatorImpl block, final Const amount) {
        block.set(this, block.sub(this, amount));
    }
}
