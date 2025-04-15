package io.quarkus.gizmo2.impl;

import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.MemoryOrder;

public non-sealed abstract class AssignableImpl extends Item implements Assignable {
    AssignableImpl() {
    }

    abstract Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode);

    abstract Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode);

    void emitInc(final BlockCreatorImpl block, final Constant amount) {
        block.set(this, block.add(this, amount));
    }

    void emitDec(final BlockCreatorImpl block, final Constant amount) {
        block.set(this, block.sub(this, amount));
    }
}
