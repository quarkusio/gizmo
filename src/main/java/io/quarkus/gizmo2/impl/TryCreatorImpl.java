package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.TryCreator;

public final class TryCreatorImpl implements TryCreator {
    private static final int ST_INITIAL = 0;
    private static final int ST_BODY = 1;
    private static final int ST_CATCH = 2;
    private static final int ST_FINALLY = 3;
    private static final int ST_DONE = 4;

    private final BlockCreatorImpl body;
    private TryCatch tryCatch;
    private TryFinally tryFinally;

    private int state = ST_INITIAL;

    public TryCreatorImpl(final BlockCreatorImpl parent) {
        body = new BlockCreatorImpl(parent);
    }

    private void advanceToState(int newState) {
        if (state > newState) {
            throw new IllegalStateException();
        }
        this.state = newState;
    }

    public void body(final Consumer<BlockCreator> builder) {
        advanceToState(ST_BODY);
        body.accept(builder);
        advanceToState(ST_CATCH);
    }

    public void catch_(final Class<? extends Throwable> type, final String caughtName,
            final BiConsumer<BlockCreator, ? super LocalVar> builder) {
        catch_(Util.classDesc(type), caughtName, builder);
    }

    public void catch_(final Set<Class<? extends Throwable>> types, final String caughtName,
            final BiConsumer<BlockCreator, ? super LocalVar> builder) {
        if (types.isEmpty()) {
            // skip
            return;
        }
        Iterator<Class<? extends Throwable>> iterator = types.iterator();
        assert iterator.hasNext();
        // extra subclass check to detect errors from unchecked casts early
        Class<? extends Throwable> type = iterator.next().asSubclass(Throwable.class);
        while (iterator.hasNext()) {
            type = findCommonSuper(type, iterator.next().asSubclass(Throwable.class));
        }
        catch_(Util.classDesc(type), caughtName, builder);
    }

    private Class<? extends Throwable> findCommonSuper(final Class<? extends Throwable> a, final Class<? extends Throwable> b) {
        if (a.isAssignableFrom(b)) {
            return a;
        } else if (b.isAssignableFrom(a)) {
            return b;
        } else {
            Class<?> superA = a.getSuperclass();
            if (superA == Throwable.class) {
                return Throwable.class;
            } else {
                return findCommonSuper(superA.asSubclass(Throwable.class), b);
            }
        }
    }

    public void catch_(final ClassDesc type, final String caughtName,
            final BiConsumer<BlockCreator, ? super LocalVar> builder) {
        catch_(type, Set.of(type), caughtName, builder);
    }

    public void catch_(final ClassDesc superType, final Set<ClassDesc> types, final String caughtName,
            final BiConsumer<BlockCreator, ? super LocalVar> builder) {
        advanceToState(ST_CATCH);
        if (tryCatch == null) {
            tryCatch = new TryCatch(body);
        }
        tryCatch.addCatch(superType, types, caughtName).accept((b0, e) -> {
            builder.accept(b0, b0.define(caughtName, e));
        });
    }

    public void finally_(final Consumer<BlockCreator> builder) {
        advanceToState(ST_FINALLY);
        if (tryFinally != null) {
            throw new IllegalStateException("Only one finally block is allowed");
        }
        if (tryCatch == null) {
            tryFinally = new TryFinally(body, builder);
        } else {
            // both try/catch and try/finally
            BlockCreatorImpl finallyBody = new BlockCreatorImpl(body.parent());
            tryFinally = new TryFinally(finallyBody, builder);
            body.tryFinally = tryFinally;
            tryFinally.body().addItem(tryCatch);
        }
    }

    void addTo(BlockCreatorImpl bci) {
        if (tryFinally != null) {
            // all wrapped with a finally block, or maybe just a finally block by itself
            bci.addItem(tryFinally);
        } else if (tryCatch != null) {
            // simpler try/catch
            bci.addItem(tryCatch);
        } else {
            // nothing!
        }
    }

    public void accept(final Consumer<? super TryCreatorImpl> handler) {
        advanceToState(ST_INITIAL);
        handler.accept(this);
        advanceToState(ST_DONE);
    }
}
