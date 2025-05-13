package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class MonitorExit extends Item {
    private final Item monitor;

    MonitorExit(final Item monitor) {
        this.monitor = monitor;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return monitor.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.monitorexit();
    }
}
