package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.impl.constant.IntBasedConst;

public final class LocalVarImpl extends AssignableImpl implements LocalVar {
    private final String name;
    private final GenericType type;
    private final BlockCreatorImpl owner;
    private int slot = -1;
    private Label startScope;
    private Label endScope;

    LocalVarImpl(final BlockCreatorImpl owner, final String name, final GenericType type) {
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
        return type.desc();
    }

    public GenericType genericType() {
        return type;
    }

    public boolean bound() {
        return false;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        checkSlot();
        // write the reference to the var
        cb.loadLocal(Util.actualKindOf(typeKind()), slot);
    }

    private void checkSlot() {
        if (slot == -1) {
            if (creationSite == null) {
                throw new IllegalStateException("Local variable '" + name + "' was not allocated (check if it was"
                        + " declared on the correct BlockCreator)" + Util.trackingMessage);
            } else {
                throw new IllegalStateException("Local variable '" + name + "' created at " + creationSite
                        + " was not allocated (check if it was declared on the correct BlockCreator)");
            }
        }
    }

    Item allocator() {
        return new Item() {
            public String itemName() {
                return "LocalVar$Allocator";
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                int slot = cb.allocateLocal(Util.actualKindOf(LocalVarImpl.this.typeKind()));
                if (LocalVarImpl.this.slot != -1 && slot != LocalVarImpl.this.slot) {
                    throw new IllegalStateException("Local variable reallocated into a different slot");
                }
                // we reserve the slot for the full remainder of the block to avoid control-flow analysis
                startScope = cb.newBoundLabel();
                endScope = block.endLabel();
                cb.localVariable(slot, name, type.desc(), startScope, endScope);
                GenericType gt = genericType();
                if (gt instanceof GenericType.OfClass oc && !oc.typeArguments().isEmpty()) {
                    cb.localVariableType(slot, name, Util.signatureOf(gt), startScope, endScope);
                }
                LocalVarImpl.this.slot = slot;
            }

            public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
                if (genericType().hasAnnotations(retention)) {
                    Util.computeAnnotations(type, retention, TypeAnnotation.TargetInfo.ofLocalVariable(
                            List.of(
                                    TypeAnnotation.LocalVarTargetInfo.of(startScope, endScope, slot))),
                            annotations, new ArrayDeque<>());
                }
            }
        };
    }

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        return asBound();
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return new Item() {
            public String itemName() {
                return "LocalVar$Set";
            }

            protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                return value.process(node.prev(), op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                checkSlot();
                cb.storeLocal(Util.actualKindOf(LocalVarImpl.this.typeKind()), slot);
            }
        };
    }

    void emitInc(final BlockCreatorImpl block, final Const amount) {
        if (typeKind().asLoadable() == TypeKind.INT) {
            block.addItem(new Item() {
                public String itemName() {
                    return "LocalVar$Inc";
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    checkSlot();
                    cb.iinc(slot, ((IntBasedConst) amount).intValue());
                }
            });
        } else {
            super.emitInc(block, amount);
        }
    }

    void emitDec(final BlockCreatorImpl block, final Const amount) {
        if (typeKind().asLoadable() == TypeKind.INT) {
            block.addItem(new Item() {
                public String itemName() {
                    return "LocalVar$Dec";
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    checkSlot();
                    cb.iinc(slot, -((IntBasedConst) amount).intValue());
                }
            });
        } else {
            super.emitInc(block, amount);
        }
    }
}
