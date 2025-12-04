package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;
import io.smallrye.classfile.TypeKind;

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
        cleanupTemplate.branchTarget();
        this.cleanupBuilder = cleanupBuilder;
        body.tryFinally = this;
        body.parent().nesting(() -> {
            cleanupTemplate.accept(cleanupBuilder);
        });
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

    IllegalStateException written = null;

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        if (written != null) {
            throw written;
        }
        written = new IllegalStateException();
        boolean bodyFallsThrough = body.mayFallThrough();
        final Label cleanupAndThrow = cb.newLabel();
        StackMapBuilder.Saved saved = smb.save();
        body.writeCode(cb, block, smb);
        cb.exceptionCatchAll(body.startLabel(), body.endLabel(), cleanupAndThrow);
        if (bodyFallsThrough) {
            cb.goto_(cleanupAndYield());
            smb.wroteCode();
        }
        smb.restore(saved);
        BlockCreatorImpl copy;
        // handle each specific case separately, since they may all have distinct stack maps
        for (Cleanup value : cleanups.values()) {
            smb.restore(value.action().saved());
            cb.labelBinding(value.label());
            smb.addFrameInfo(cb);
            if (!cleanupTemplate.mayFallThrough()) {
                // skip it
                switch (TypeKind.from(value.type()).slotSize()) {
                    case 0 -> {
                    }
                    case 1 -> {
                        cb.pop();
                        smb.pop();
                    }
                    case 2 -> {
                        cb.pop2();
                        smb.pop();
                    }
                }
                cb.goto_(cleanupAndYield());
                smb.wroteCode();
            } else {
                copy = new BlockCreatorImpl(body.parent(), value.type());
                copy.accept((b, val) -> {
                    cleanupBuilder.accept(b);
                    if (b.active()) {
                        value.action().terminate(b, val);
                    }
                });
                copy.writeCode(cb, block, smb);
            }
        }
        smb.restore(saved);
        // penultimate case: handle rethrow
        cb.labelBinding(cleanupAndThrow);
        smb.clearStack();
        smb.push(CD_Throwable);
        smb.addFrameInfo(cb);
        if (!cleanupTemplate.mayFallThrough() && Util.isVoid(body.type())) {
            // cleanup cannot fall through; go to the simple case
            cb.pop();
            smb.pop();
            smb.wroteCode();
            // initialize cleanup-and-yield
            cleanupAndYield();
            // fall thru to it
        } else {
            copy = new BlockCreatorImpl(body.parent(), CD_Throwable);
            copy.branchTarget();
            copy.accept((b, val) -> {
                cleanupBuilder.accept(b);
                if (b.active()) {
                    b.throw_(val);
                }
            });
            copy.writeCode(cb, block, smb);
        }
        smb.restore(saved);
        // last case: handle yield (and fall out)
        if (cleanupAndYield != null) {
            cb.labelBinding(cleanupAndYield);
            smb.addFrameInfo(cb);
            copy = new BlockCreatorImpl(body.parent(), body.type(), body.type());
            copy.branchTarget();
            copy.accept((b, val) -> {
                cleanupBuilder.accept(b);
                if (b.active()) {
                    b.yield(val);
                }
            });
            copy.writeCode(cb, block, smb);
        }
    }

    abstract static class CleanupKey {
        private final StackMapBuilder.Saved saved;

        CleanupKey(final StackMapBuilder.Saved saved) {
            this.saved = saved;
        }

        ClassDesc type() {
            return CD_void;
        }

        StackMapBuilder.Saved saved() {
            return saved;
        }

        abstract void terminate(BlockCreatorImpl bci, Expr input);
    }

    record Cleanup(Label label, CleanupKey action) {
        ClassDesc type() {
            return action.type();
        }
    }
}
