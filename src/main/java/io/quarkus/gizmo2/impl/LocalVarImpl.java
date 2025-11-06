package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.creator.BlockCreator;

public final class LocalVarImpl extends AssignableImpl implements LocalVar {
    private final String name;
    private final BlockCreatorImpl owner;
    int slot = -1;

    LocalVarImpl(final BlockCreatorImpl owner, final String name, final ClassDesc type, final GenericType genericType) {
        super(type, genericType);
        this.name = name;
        this.owner = owner;
    }

    public String itemName() {
        return "LocalVar:" + name;
    }

    public BlockCreator block() {
        return owner;
    }

    public String name() {
        return name;
    }

    public boolean bound() {
        return false;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        checkSlot();
        // write the reference to the var
        cb.loadLocal(Util.actualKindOf(typeKind()), slot);
        smb.push(type()); // variable type
        smb.wroteCode();
    }

    void checkSlot() {
        if (slot == -1) {
            if (creationSite == null) {
                throw new IllegalStateException("Local variable '" + name + "' was not allocated (check if it was"
                        + " declared on the correct BlockCreator or if it was captured if you're generating a lambda)"
                        + Util.trackingMessage);
            } else {
                throw new IllegalStateException("Local variable '" + name + "' created at " + creationSite
                        + " was not allocated (check if it was declared on the correct BlockCreator or if it was"
                        + " captured if you're generating a lambda)");
            }
        }
    }

    Item allocator() {
        return new LocalVarAllocator(this);
    }

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        return asBound();
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return new LocalVarSet(this, value);
    }

    void emitInc(final BlockCreatorImpl block, final Const amount) {
        if (typeKind().asLoadable() == TypeKind.INT) {
            block.addItem(new LocalVarIncrement(this, amount));
        } else {
            super.emitInc(block, amount);
        }
    }

    void emitDec(final BlockCreatorImpl block, final Const amount) {
        if (typeKind().asLoadable() == TypeKind.INT) {
            block.addItem(new LocalVarDecrement(this, amount));
        } else {
            super.emitDec(block, amount);
        }
    }
}
