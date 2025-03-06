package io.quarkus.gizmo2.creator;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.impl.SwitchCreatorImpl;

/**
 * A creator for a {@code switch} statement.
 * The individual switch cases do not fall through.
 * To simulate fall-through behavior, use {@link BlockCreator#redo(SwitchCreator, Constant)}
 * or one of its variants at the end of each case.
 */
public sealed interface SwitchCreator permits SwitchCreatorImpl {
    // todo: the ability to have multiple values point to the same case

    /**
     * Add a switch case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    void case_(Constant val, Consumer<BlockCreator> body);

    /**
     * Add a switch case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(ConstantDesc val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(Constable val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(int val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(long val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(String val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add the default case.
     *
     * @param body the builder for the body of the default case (must not be {@code null})
     */
    void default_(Consumer<BlockCreator> body);
}
