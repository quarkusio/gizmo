package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.impl.constant.IntConstant;

public final class LocalVarImpl extends LValueExprImpl implements LocalVar {
    private final String name;
    private final ClassDesc type;
    private final BlockCreatorImpl owner;
    private int slot = -1;

    LocalVarImpl(final BlockCreatorImpl owner, final String name, final ClassDesc type) {
        this.name = name;
        this.type = type;
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

    public ClassDesc type() {
        return type;
    }

    public boolean bound() {
        return false;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        checkSlot();
        // write the reference to the var
        cb.loadLocal(typeKind(), slot);
    }

    private void checkSlot() {
        if (slot == -1) {
            throw new IllegalStateException("Local variable failed to be allocated");
        }
    }

    Item allocator() {
        return new Item() {
            public String itemName() {
                return "LocalVar$Allocator";
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                int slot = cb.allocateLocal(typeKind());
                // we reserve the slot for the full remainder of the block to avoid control-flow analysis
                cb.localVariable(slot, name, type, cb.newBoundLabel(), block.endLabel());
                if (LocalVarImpl.this.slot != -1 && slot != LocalVarImpl.this.slot) {
                    throw new IllegalStateException("Local variable reallocated into a different slot");
                }
                LocalVarImpl.this.slot = slot;
            }
        };
    }

    ExprImpl emitGet(final BlockCreatorImpl block, final AccessMode mode) {
        return asBound();
    }

    Item emitSet(final BlockCreatorImpl block, final ExprImpl value, final AccessMode mode) {
        return new Item() {
            public String itemName() {
                return "LocalVar$Set";
            }

            protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                value.process(iter, op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                checkSlot();
                cb.storeLocal(typeKind(), slot);
            }
        };
    }

    void emitInc(final BlockCreatorImpl block, final Constant amount) {
        if (typeKind().asLoadable() == TypeKind.INT) {
            block.addItem(new Item() {
                public String itemName() {
                    return "LocalVar$Inc";
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    cb.iinc(slot, ((IntConstant) amount).intValue());
                }
            });
        } else {
            super.emitInc(block, amount);
        }
    }

    void emitDec(final BlockCreatorImpl block, final Constant amount) {
        if (typeKind().asLoadable() == TypeKind.INT) {
            block.addItem(new Item() {
                public String itemName() {
                    return "LocalVar$Dec";
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    cb.iinc(slot, -((IntConstant) amount).intValue());
                }
            });
        } else {
            super.emitInc(block, amount);
        }
    }
}
