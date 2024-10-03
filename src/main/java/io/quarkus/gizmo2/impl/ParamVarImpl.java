package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.ParamVar;

public final class ParamVarImpl extends LValueExprImpl implements ParamVar {
    private final ClassDesc type;
    private final String name;
    private final int index;
    private final int slot;
    final List<Annotation> invisible;
    final List<Annotation> visible;
    private final int flags;

    public ParamVarImpl(final ClassDesc type, final String name, final int index, final int slot, final int flags, final List<Annotation> invisible, final List<Annotation> visible) {
        this.type = type;
        this.name = name;
        this.index = index;
        this.slot = slot;
        this.flags = flags;
        this.invisible = invisible;
        this.visible = visible;
    }

    int flags() {
        return flags;
    }

    public int slot() {
        return slot;
    }

    public int index() {
        return index;
    }

    ExprImpl emitGet(final BlockCreatorImpl block, final AccessMode mode) {
        return asBound();
    }

    Item emitSet(final BlockCreatorImpl block, final ExprImpl value, final AccessMode mode) {
        return new Item() {
            protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                value.process(iter, op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.storeLocal(typeKind(), slot);
            }
        };
    }

    public ClassDesc type() {
        return type;
    }

    public boolean bound() {
        return false;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.loadLocal(typeKind(), slot);
    }

    public String name() {
        return name;
    }
}
