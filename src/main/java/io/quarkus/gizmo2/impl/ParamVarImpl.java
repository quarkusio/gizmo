package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.List;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.ParamVar;

public final class ParamVarImpl extends AssignableImpl implements ParamVar {
    private final String name;
    private final int index;
    private final int slot;
    final List<Annotation> invisible;
    final List<Annotation> visible;
    private final int flags;

    ParamVarImpl(final ClassDesc type, final GenericType genericType, final String name, final int index,
            final int slot, final int flags, final List<Annotation> invisible, final List<Annotation> visible) {
        super(type, genericType);
        this.name = name;
        this.index = index;
        this.slot = slot;
        this.flags = flags;
        this.invisible = invisible;
        this.visible = visible;
    }

    @Override
    public String itemName() {
        return "ParamVar:" + name;
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

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        return asBound();
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return new ParamSet(this, value);
    }

    public boolean bound() {
        return false;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.loadLocal(Util.actualKindOf(typeKind()), slot);
    }

    public String name() {
        return name;
    }
}
