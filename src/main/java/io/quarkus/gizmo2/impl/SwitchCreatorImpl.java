package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;

public sealed abstract class SwitchCreatorImpl<C extends Constant> extends Item implements SwitchCreator permits IntSwitch {
    public static final double TABLESWITCH_DENSITY = 0.9;
    final BlockCreatorImpl enclosing;
    final Item switchVal;

    SwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal) {
        this.enclosing = enclosing;
        this.switchVal = (Item) switchVal;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return switchVal.process(node.prev(), op);
    }

    public BlockCreatorImpl enclosing() {
        return enclosing;
    }

    public Expr switchVal() {
        return switchVal;
    }

    abstract Case<C> findCase(final Constant val);

    abstract BlockCreatorImpl findDefault();

    static final class Case<C> extends BlockCreatorImpl {
        final C value;

        Case(final BlockCreatorImpl parent, final C value) {
            super(parent);
            this.value = value;
        }
    }

    static final class Default {
        Label label;
    }
}
