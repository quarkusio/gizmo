package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.ListIterator;
import java.util.NoSuchElementException;

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
     * Move the cursor before the given {@code item}, popping any unused values in between.
     * If the item is not found, an exception is thrown.
     *
     * @param iter the item list iterator (must not be {@code null})
     */
    void verify(ListIterator<Item> iter) {
        while (iter.hasPrevious()) {
            Item actual = iter.previous();
            if (equals(actual)) {
                // found it
                return;
            }
            // we don't care about this one
            actual.pop(iter);
        }
        throw missing();
    }

    /**
     * Pop the item's result from the stack during a stack cleanup.
     * This item will have just been returned by {@code iter.previous()}.
     *
     * @param iter the item list iterator (not {@code null})
     */
    public void pop(ListIterator<Item> iter) {
        if (type().equals(ConstantDescs.CD_void)) {
            // no operation
        } else if (! bound()) {
            // we can safely remove ourselves
            iter.remove();
            // and our dependencies
            processDependencies(iter, Op.POP);
        } else {
            iter.next();
            // add an explicit pop
            Pop pop = new Pop((ExprImpl) this);
            pop.insert(iter);
            // move back before `this` again
            iter.previous();
        }
    }

    /**
     * Insert this item into the instruction list at the current point
     * and position the cursor immediately before it.
     *
     * @param iter the item list iterator (not {@code null})
     */
    protected void insert(ListIterator<Item> iter) {
        iter.add(this);
        iter.previous();
    }

    /**
     * Peek at the previous item in the list iterator.
     * If there is no previous item, an exception is thrown.
     * The item will be considered to be the "last returned" for the purposes of {@linkplain ListIterator#remove() removal}
     * or {@linkplain ListIterator#add addition}.
     *
     * @param iter the list iterator (must not be {@code null})
     * @return the previous item (not {@code null})
     * @throws NoSuchElementException if there is no previous item
     */
    protected Item peek(ListIterator<Item> iter) throws NoSuchElementException {
        Item item = iter.previous();
        iter.next();
        return item;
    }

    /**
     * Replace this item with the given replacement.
     * This item must be the previous item in the iterator.
     *
     * @param iter        the list iterator (must not be {@code null})
     * @param replacement the replacement item (must not be {@code null})
     */
    protected void replace(ListIterator<Item> iter, Item replacement) {
        Item item = iter.previous();
        if (item != this) {
            throw new IllegalStateException("Item mismatch");
        }
        iter.remove();
        iter.add(replacement);
    }

    /**
     * Process a single item and recurse to its dependencies in reverse order.
     * Any intervening {@code void}-typed expressions are automatically skipped.
     * Any intervening non-{@code void}-typed expressions are popped from the stack.
     *
     * @param iter the item iterator (not {@code null})
     * @param op   the operation (not {@code null})
     */
    protected void process(ListIterator<Item> iter, Op op) {
        switch (op) {
            case VERIFY -> {
                // expect each dependency to exist in order
                verify(iter);
                processDependencies(iter, op);
            }
            case INSERT -> {
                // insert all unbound dependencies
                if (! bound()) {
                    insert(iter);
                    processDependencies(iter, op);
                }
            }
            case POP -> {
                // remove this value and its dependencies
                pop(iter);
                processDependencies(iter, op);
            }
        }
    }

    /**
     * Process this item's dependencies in the item list by calling {@link #process(ListIterator, Op)}
     * on each one.
     * Dependencies must be processed from "right to left", which is to say that items that should be on the top
     * of the stack should be processed first.
     *
     * @param iter the item iterator (not {@code null})
     * @param op   the operation (not {@code null})
     */
    protected void processDependencies(ListIterator<Item> iter, Op op) {
        // no dependencies
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

    protected enum Op {
        INSERT,
        VERIFY,
        POP,
        ;
    }
}
