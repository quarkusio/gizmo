package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.impl.TryCreatorImpl;

/**
 * A creator for a {@code try}-{@code catch}-{@code finally} construct.
 */
public sealed interface TryCreator extends BodyCreator permits TryCreatorImpl {

    /**
     * Build the body of the {@code try} statement.
     *
     * @param builder the builder (must not be {@code null})
     */
    void body(Consumer<BlockCreator> builder);

    /**
     * Add a catch clause which receives the thrown exception.
     *
     * @param type the descriptor of the exception type to catch (must not be {@code null})
     * @param caughtName the name of the caught exception variable (must not be {@code null})
     * @param builder the builder for the catch block (must not be {@code null})
     */
    default void catch_(ClassDesc type, String caughtName, BiConsumer<BlockCreator, ? super LocalVar> builder) {
        catch_(type, Set.of(type), caughtName, builder);
    }

    /**
     * Add a catch clause for multiple throwable types which receives the thrown exception.
     *
     * @param superType the common supertype of all the given types (must not be {@code null})
     * @param types the exception types to catch (must not be {@code null})
     * @param caughtName the name of the caught exception variable (must not be {@code null})
     * @param builder the builder for the catch block (must not be {@code null})
     */
    void catch_(ClassDesc superType, Set<ClassDesc> types, String caughtName,
            BiConsumer<BlockCreator, ? super LocalVar> builder);

    /**
     * Add a catch clause which receives the thrown exception.
     *
     * @param type the exception type to catch (must not be {@code null})
     * @param caughtName the name of the caught exception variable (must not be {@code null})
     * @param builder the builder for the catch block (must not be {@code null})
     */
    void catch_(Class<? extends Throwable> type, String caughtName, BiConsumer<BlockCreator, ? super LocalVar> builder);

    /**
     * Add a catch clause for multiple throwable types which receives the thrown exception.
     *
     * @param types the exception types to catch (must not be {@code null})
     * @param caughtName the name of the caught exception variable (must not be {@code null})
     * @param builder the builder for the catch block (must not be {@code null})
     */
    void catch_(Set<Class<? extends Throwable>> types, String caughtName, BiConsumer<BlockCreator, ? super LocalVar> builder);

    /**
     * Add the {@code finally} clause.
     * Note that the builder will be called <em>multiple times</em>
     * if there are multiple exit points to the method (which is usually the case).
     *
     * @param builder the builder for the {@code finally} block (must not be {@code null})
     */
    void finally_(Consumer<BlockCreator> builder);
}
