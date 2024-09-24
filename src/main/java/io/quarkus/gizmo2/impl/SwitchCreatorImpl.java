package io.quarkus.gizmo2.impl;

import java.util.ListIterator;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;

public sealed abstract class SwitchCreatorImpl<C extends Constant> extends Item implements SwitchCreator permits IntSwitch {
    public static final double TABLESWITCH_DENSITY = 0.9;
    final BlockCreatorImpl enclosing;
    final ExprImpl switchVal;

    SwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal) {
        this.enclosing = enclosing;
        this.switchVal = (ExprImpl) switchVal;
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        switchVal.process(block, iter, verifyOnly);
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
