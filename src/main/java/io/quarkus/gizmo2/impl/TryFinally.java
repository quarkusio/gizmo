package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * A {@code try}-{@code finally} block.
 */
final class TryFinally extends Item {
    final BlockCreatorImpl body;
    final BlockCreatorImpl cleanupTemplate;
    final Consumer<BlockCreator> cleanupBuilder;
    Label cleanupAndYield;
    final Map<CleanupKey, Cleanup> cleanups = new LinkedHashMap<>();

    TryFinally(final BlockCreatorImpl body, final Consumer<BlockCreator> cleanupBuilder) {
        this.body = body;
        cleanupTemplate = new BlockCreatorImpl(body.parent());
        this.cleanupBuilder = cleanupBuilder;
        body.tryFinally = this;
        cleanupTemplate.accept(cleanupBuilder);
    }

    BlockCreatorImpl body() {
        return body;
    }

    Label cleanupAndYield() {
        Label cleanupAndYield = this.cleanupAndYield;
        if (cleanupAndYield == null) {
            cleanupAndYield = this.cleanupAndYield = body.newLabel();
        }
        return cleanupAndYield;
    }

    Label cleanup(CleanupKey key) {
        Cleanup cleanup = cleanups.get(key);
        if (cleanup == null) {
            cleanup = new Cleanup(body.newLabel(), key);
            cleanups.put(key, cleanup);
        }
        return cleanup.label();
    }

    public boolean mayFallThrough() {
        return body.mayFallThrough() && cleanupTemplate.mayFallThrough();
    }

    boolean written = false;

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (written) {
            throw new IllegalStateException("Code has already been written");
        }
        written = true;
        boolean bodyFallsThrough = body.mayFallThrough();
        final Label cleanupAndThrow = cb.newLabel();
        body.writeCode(cb, block);
        cb.exceptionCatchAll(body.startLabel(), body.endLabel(), cleanupAndThrow);
        if (bodyFallsThrough) {
            cb.goto_(cleanupAndYield());
        }
        BlockCreatorImpl copy;
        // handle each specific case separately, since they may all have distinct stack maps
        for (Cleanup value : cleanups.values()) {
            cb.labelBinding(value.label());
            if (!cleanupTemplate.mayFallThrough()) {
                // skip it
                switch (TypeKind.from(value.type()).slotSize()) {
                    case 0 -> {
                    }
                    case 1 -> cb.pop();
                    case 2 -> cb.pop2();
                }
                cb.goto_(cleanupAndYield());
            } else {
                copy = new BlockCreatorImpl(body.parent(), value.type());
                copy.accept((b, val) -> {
                    cleanupBuilder.accept(b);
                    if (b.active()) {
                        value.action().terminate(b, val);
                    }
                });
                copy.writeCode(cb, block);
            }
        }
        // penultimate case: handle rethrow
        cb.labelBinding(cleanupAndThrow);
        if (!cleanupTemplate.mayFallThrough() && body.type().equals(CD_void)) {
            // cleanup cannot fall through; go to the simple case
            cb.pop();
            // initialize cleanup-and-yield
            cleanupAndYield();
            // fall thru to it
        } else {
            copy = new BlockCreatorImpl(body.parent(), CD_Throwable);
            copy.accept((b, val) -> {
                cleanupBuilder.accept(b);
                if (b.active()) {
                    b.throw_(val);
                }
            });
            copy.writeCode(cb, block);
        }
        // last case: handle yield (and fall out)
        if (cleanupAndYield != null) {
            cb.labelBinding(cleanupAndYield);
            copy = new BlockCreatorImpl(body.parent(), body.type(), body.type());
            copy.accept((b, val) -> {
                cleanupBuilder.accept(b);
                if (b.active()) {
                    b.yield(val);
                }
            });
            copy.writeCode(cb, block);
        }
    }

    abstract static class CleanupKey {
        CleanupKey() {
        }

        ClassDesc type() {
            return CD_void;
        }

        abstract void terminate(BlockCreatorImpl bci, Expr input);
    }

    record Cleanup(Label label, CleanupKey action) {
        ClassDesc type() {
            return action.type();
        }
    }
}
