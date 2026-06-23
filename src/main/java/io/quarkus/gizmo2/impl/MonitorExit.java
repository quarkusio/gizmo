package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

final class MonitorExit extends Item {
    private final Item monitor;

    MonitorExit(final Item monitor) {
        this.monitor = monitor;
    }

    /**
     * {@return the monitor object expression}
     */
    Item monitor() {
        return monitor;
    }

    @Override
    protected boolean isSourceStatement() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        SourceGenerator.stmtMonitorExit(this, sb);
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        monitor.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.monitorexit();
        smb.pop();
        smb.wroteCode();
    }
}
