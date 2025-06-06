package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

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
        checkNotNullParam("head", head);
        checkNotNullParam("tail", tail);
        Node a = new Node(null, null, head);
        Node b = new Node(null, null, tail);
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

    public String debugFind(Item item) {
        if (item().equals(item)) {
            return "Found at this node";
        }
        Node current = next();
        int cnt = 1;
        while (current != null) {
            if (current.item().equals(item)) {
                return "Found at +" + cnt;
            }
            cnt++;
            current = current.next();
        }
        cnt = 1;
        current = prev();
        while (current != null) {
            if (current.item().equals(item)) {
                return "Found at -" + cnt;
            }
            cnt++;
            current = current.prev();
        }
        return "Not found";
    }

    public String debugList() {
        Node first = this;
        while (first.prev() != null) {
            first = first.prev();
        }

        StringBuilder sb = new StringBuilder();

        Node current = first;
        while (current != null) {
            if (current == this) {
                sb.append('|');
            }
            current.item.toShortString(sb);
            if (current == this) {
                sb.append('|');
            }
            if (current.next != null) {
                sb.append(" -> ");
            }
            current = current.next();
        }

        return sb.toString();
    }
}
