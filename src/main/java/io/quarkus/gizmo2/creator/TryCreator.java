package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.TryImpl;

public sealed interface TryCreator permits TryImpl {

    void body(Consumer<BlockCreator> builder);

    void catch_(ClassDesc type, BiConsumer<BlockCreator, Expr> body);

    /**
     * Add a catch clause.
     *
     * @param superType the common supertype of all the given types (must not be {@code null})
     * @param types the exception types to catch (must not be {@code null})
     * @param builder the builder for the catch block (must not be {@code null})
     */
    void catch_(ClassDesc superType, Set<ClassDesc> types, BiConsumer<BlockCreator, Expr> builder);

    void catch_(Class<? extends Throwable> type, BiConsumer<BlockCreator, Expr> builder);

    void catch_(Set<Class<? extends Throwable>> types, BiConsumer<BlockCreator, Expr> builder);

    /**
     * Add the {@code finally} clause.
     * Note that the builder will be called <em>multiple times</em>
     * if there are multiple exit points to the method (which is usually the case).
     *
     * @param builder the builder for the {@code finally} block (must not be {@code null})
     */
    void finally_(Consumer<BlockCreator> builder);
}
