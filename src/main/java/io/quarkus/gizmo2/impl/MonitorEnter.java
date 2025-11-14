package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;

final class MonitorEnter extends Item {
    private final Item monitor;

    MonitorEnter(final Item monitor) {
        this.monitor = monitor;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        monitor.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.monitorenter();
        smb.pop();
        smb.wroteCode();
    }
}
