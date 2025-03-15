package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Throwable;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.TryCreator;

public final class TryImpl extends Item implements TryCreator {
    private static final int ST_INITIAL = 0;
    private static final int ST_BODY = 1;
    private static final int ST_CATCH = 2;
    private static final int ST_FINALLY = 3;
    private static final int ST_DONE = 4;

    private final ArrayList<Catch> catches = new ArrayList<>(4);
    private BlockCreatorImpl body;
    private BlockCreatorImpl finally_;

    private int state = ST_INITIAL;

    public TryImpl(final BlockCreatorImpl parent) {
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
        builder.accept(body);
        advanceToState(ST_CATCH);
    }

    public void catch_(final Class<? extends Throwable> type, final BiConsumer<BlockCreator, Expr> builder) {
        catch_(Util.classDesc(type), builder);
    }

    public void catch_(final Set<Class<? extends Throwable>> types, final BiConsumer<BlockCreator, Expr> builder) {
        if (types.isEmpty()) {
            // skip
            return;
        }
        Iterator<Class<? extends Throwable>> iterator = types.iterator();
        assert iterator.hasNext();
        // extra subclass check to detect errors from unsafe casts early
        Class<? extends Throwable> type = iterator.next().asSubclass(Throwable.class);
        while (iterator.hasNext()) {
            type = findCommonSuper(type, iterator.next().asSubclass(Throwable.class));
        }
        catch_(Util.classDesc(type), builder);
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

    public void catch_(final ClassDesc type, final BiConsumer<BlockCreator, Expr> builder) {
        catch_(type, Set.of(type), builder);
    }

    public void catch_(final ClassDesc superType, final Set<ClassDesc> types, final BiConsumer<BlockCreator, Expr> builder) {
        advanceToState(ST_CATCH);
        Catch c = new Catch(types, new BlockCreatorImpl(body, superType));
        catches.add(c);
        c.accept(builder);
    }

    public void finally_(final Consumer<BlockCreator> builder) {
        advanceToState(ST_FINALLY);
        if (finally_ != null) {
            throw new IllegalStateException("Only one finally block is allowed");
        }
        finally_ = new BlockCreatorImpl(body);
        builder.accept(finally_);
        Cleanup cleanup = finally_::writeCode;
        body.cleanup(cleanup);
        catches.forEach(c -> c.body().cleanup(cleanup));
    }

    public void accept(final Consumer<? super TryImpl> handler) {
        advanceToState(ST_INITIAL);
        handler.accept(this);
        noAddCleanup: if (finally_ != null) {
            for (Catch catch_ : catches) {
                if (catch_.types.contains(CD_Throwable)) {
                    break noAddCleanup;
                }
            }
            // no catch-all; add one!
            Catch c = new Catch(Set.of(CD_Throwable), new BlockCreatorImpl(body, CD_Throwable));
            catches.add(c);
            c.accept(BlockCreator::throw_);
            c.body().cleanup(finally_::writeCode);
        }
        advanceToState(ST_DONE);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (this.state != ST_DONE) {
            throw new IllegalStateException("Incomplete try");
        }
        Label after = cb.newLabel();
        for (Catch catch_ : catches) {
            for (ClassDesc type : catch_.types()) {
                if (type.equals(CD_Throwable)) {
                    cb.exceptionCatchAll(body.startLabel(), body.endLabel(), catch_.body().startLabel());
                } else {
                    cb.exceptionCatch(body.startLabel(), body.endLabel(), catch_.body().startLabel(), type);
                }
            }
        }
        body.writeCode(cb, block);
        if (body.mayFallThrough()) {
            cb.goto_(after);
        }
        Iterator<Catch> iterator = catches.iterator();
        while (iterator.hasNext()) {
            final Catch catch_ = iterator.next();
            catch_.body().writeCode(cb, block);
            if (catch_.body().mayFallThrough() && iterator.hasNext()) {
                cb.goto_(after);
            }
        }
        cb.labelBinding(after);
    }

    static final class Catch {
        private final Set<ClassDesc> types;
        private final BlockCreatorImpl body;

        Catch(final Set<ClassDesc> types, final BlockCreatorImpl body) {
            this.types = types;
            this.body = body;
        }

        Set<ClassDesc> types() {
            return types;
        }

        BlockCreatorImpl body() {
            return body;
        }

        void accept(final BiConsumer<BlockCreator, Expr> builder) {
            body().accept(builder);
        }
    }
}
