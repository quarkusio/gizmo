package io.quarkus.gizmo2;

/**
 * The possible memory order modes.
 */
public enum MemoryOrder {
    /**
     * Use the declared mode of the variable.
     * For fields, this could be {@link #Plain} or {@link #Volatile}.
     * For all other types, this is {@link #Plain}.
     */
    AsDeclared(true, true),
    /**
     * Access using "plain" semantics.
     * All assignables support this type of access.
     */
    Plain(true, true),
    /**
     * Access using "opaque" semantics.
     */
    Opaque(true, true),
    /**
     * Access using "acquire" semantics.
     */
    Acquire(true, false),
    /**
     * Access using "release" semantics.
     */
    Release(false, true),
    /**
     * Access using "volatile" semantics.
     */
    Volatile(true, true),
    ;

    private final boolean reads, writes;

    MemoryOrder(final boolean reads, final boolean writes) {
        this.reads = reads;
        this.writes = writes;
    }

    /**
     * {@return true if this mode may be used for read operations, or false if it cannot}
     */
    public boolean validForReads() {
        return reads;
    }

    /**
     * {@return true if this mode may be used for write operations, or false if it cannot}
     */
    public boolean validForWrites() {
        return writes;
    }
}
