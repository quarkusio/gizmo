package io.quarkus.gizmo2.impl;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A basic mutable doubly-linked-list node which holds an {@link Item}.
 */
public final class Node {
    private Node next;
    private Node prev;
    private Item item;

    private Node(final Node next, final Node prev, final Item item) {
        this.next = next;
        this.prev = prev;
        this.item = item;
    }

    /**
     * Create a new list with a single item.
     *
     * @param head the initial head item (must not be {@code null})
     * @param tail the initial tail item (must not be {@code null})
     * @return the head node in the new list (not {@code null})
     */
    public static Node newList(Item head, Item tail) {
        Node a = new Node(null, null, Objects.requireNonNull(head, "head"));
        Node b = new Node(null, null, Objects.requireNonNull(tail, "tail"));
        a.next = b;
        b.prev = a;
        return a;
    }

    public boolean hasNext() {
        return next != null;
    }

    public Node next() {
        return next;
    }

    public boolean hasPrev() {
        return prev != null;
    }

    public Node prev() {
        return prev;
    }

    public Item item() {
        return item;
    }

    public Item set(final Item item) {
        Item old = this.item;
        this.item = item;
        return old;
    }

    public Node apply(BiFunction<Item, Node, Node> op) {
        return op.apply(item, this);
    }

    public Node insertPrev(Item item) {
        Node oldPrev = prev;
        if (oldPrev == null) {
            throw new IllegalStateException();
        }
        return oldPrev.next = prev = new Node(this, oldPrev, item);
    }

    public Node insertNext(Item item) {
        Node oldNext = next;
        if (oldNext == null) {
            throw new IllegalStateException();
        }
        return oldNext.prev = next = new Node(oldNext, this, item);
    }

    public void splicePrev(final Node prev) {
        this.prev = prev;
        prev.next = this;
    }

    public void spliceNext(final Node next) {
        this.next = next;
        next.prev = this;
    }

    public Item remove() {
        if (next != null) {
            next.prev = prev;
        }
        if (prev != null) {
            prev.next = next;
        }
        next = null;
        prev = null;
        return item;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        if (prev == null) {
            sb.append("x ");
        } else {
            sb.append("->");
        }
        item.toShortString(sb);
        if (next == null) {
            sb.append(" x");
        } else {
            sb.append("->");
        }
        return sb.toString();
    }
}
