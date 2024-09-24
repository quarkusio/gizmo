package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;

public abstract class Item {

    public String itemName() {
        return getClass().getSimpleName();
    }

    /**
     * {@return the type of this item}
     * If the type is not {@code CD_void}, then the item must extend {@link ExprImpl}.
     */
    public ClassDesc type() {
        return ConstantDescs.CD_void;
    }

    public boolean bound() {
        return true;
    }

    /**
     * Insert this item into the instruction list at the current point
     * and position the cursor immediately before this item's dependencies (if any).
     *
     * @param block the block being added to (not {@code null})
     * @param iter the item list iterator (not {@code null})
     */
    protected void insert(final BlockCreatorImpl block, ListIterator<Item> iter) {
        iter.add(this);
        iter.previous();
        processDependencies(block, iter, false);
    }

    /**
     * Process a single dependency.
     * If the {@code verifyOnly} flag is not set, all {@linkplain #bound unbound} dependencies will be inserted.
     * Any intervening {@code void}-typed expressions are automatically skipped.
     * Any intervening non-{@code void}-typed expressions are popped from the stack.
     *
     * @param block      the block containing this item (not {@code null})
     * @param iter       the item iterator (not {@code null})
     * @param verifyOnly {@code true} to only verify, {@code false} to insert unbound dependencies
     */
    protected void process(final BlockCreatorImpl block, ListIterator<Item> iter, boolean verifyOnly) {
        if (! verifyOnly && ! bound()) {
            // insert unbound item
            iter.add(this);
            iter.previous();
            processDependencies(block, iter, false);
        } else {
            expect(block, iter, this);
            processDependencies(block, iter, true);
        }
    }

    /**
     * Process this item's dependencies in the item list by calling {@link #process(BlockCreatorImpl, ListIterator, boolean)}
     * on each one.
     * Dependencies must be processed from "right to left", which is to say that items that should be on the top
     * of the stack should be processed first.
     *
     * @param block      the block containing this item (not {@code null})
     * @param iter       the item iterator (not {@code null})
     * @param verifyOnly {@code true} to only verify, {@code false} to insert unbound dependencies
     */
    protected void processDependencies(final BlockCreatorImpl block, ListIterator<Item> iter, final boolean verifyOnly) {
        // no dependencies
    }

    /**
     * Move the cursor before the given {@code item}, popping any unused values in between.
     * If the item is not found, an exception is thrown.
     *
     * @param block the block (must not be {@code null})
     * @param iter  the item list iterator (must not be {@code null})
     * @param item  the item to expect (must not be {@code null})
     */
    static void expect(final BlockCreatorImpl block, ListIterator<Item> iter, Item item) {
        while (iter.hasPrevious()) {
            Item actual = iter.previous();
            if (item.equals(actual)) {
                // found it
                return;
            }
            if (! actual.type().equals(ConstantDescs.CD_void)) {
                // todo: remove with dependencies if not bound
                // pop it
                iter.next();
                Pop pop = new Pop((ExprImpl) item);
                // add the pop and move the cursor before `item` in one step
                pop.insert(block, iter);
                // and move before the thing we popped
                iter.previous();
            }
        }
        throw missing();
    }

    static IllegalStateException missing() {
        return new IllegalStateException("Item is not at its expected location (use a variable to store values which are used away from their definition site");
    }

    public abstract void writeCode(CodeBuilder cb, BlockCreatorImpl block);

    public boolean exitsAll() {
        return false;
    }

    public boolean exitsBlock() {
        return exitsAll();
    }

    public final String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder b) {
        toShortString(b);
        return b;
    }

    public StringBuilder toShortString(StringBuilder b) {
        return b.append(itemName()).append('@').append(Integer.toHexString(hashCode()));
    }
}
