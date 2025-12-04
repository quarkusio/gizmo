package io.quarkus.gizmo2.impl;

import io.smallrye.classfile.CodeBuilder;

/**
 * A no-operation node.
 */
final class Nop extends Item {
    private static final boolean alwaysWriteNop = Boolean.getBoolean("gizmo.writeNop");

    // these two must have separate identities even if alwaysWriteNop is true.
    // unlike VoidConst.INSTANCE, this node cannot be an input, thus it is always replaceable.
    public static final Nop FILL = new Nop(alwaysWriteNop);
    public static final Nop EMITTING = new Nop(true);

    private final boolean writeNop;

    private Nop(boolean writeNop) {
        this.writeNop = writeNop;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        if (writeNop) {
            cb.nop();
            smb.wroteCode();
        }
    }
}
