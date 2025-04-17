package io.quarkus.gizmo2.creator;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.impl.SwitchCreatorImpl;

/**
 * A creator for a {@code switch} statement.
 * The individual switch cases do not fall through.
 * To simulate fall-through behavior, use {@link BlockCreator#gotoCase(SwitchCreator, Const)}
 * or one of its variants at the end of each case.
 */
public sealed interface SwitchCreator extends SimpleTyped permits SwitchCreatorImpl {

    /**
     * Add a switch case.
     *
     * @param builder the switch case builder (must not be {@code null})
     */
    void case_(Consumer<CaseCreator> builder);

    /**
     * Add a simple switch case.
     *
     * @param val the case value to add (must not be {@code null})
     * @param body the switch case body builder (must not be {@code null})
     */
    default void caseOf(Const val, Consumer<BlockCreator> body) {
        case_(cc -> {
            cc.of(val);
            cc.body(body);
        });
    }

    /**
     * Add a simple switch case.
     *
     * @param val the case value to add (must not be {@code null})
     * @param body the switch case body builder (must not be {@code null})
     */
    default void caseOf(Constable val, Consumer<BlockCreator> body) {
        caseOf(Const.of(val), body);
    }

    /**
     * Add a simple switch case.
     *
     * @param val the case value to add (must not be {@code null})
     * @param body the switch case body builder (must not be {@code null})
     */
    default void caseOf(ConstantDesc val, Consumer<BlockCreator> body) {
        caseOf(Const.of(val), body);
    }

    /**
     * Add a simple switch case.
     *
     * @param val the case value to add (must not be {@code null})
     * @param body the switch case body builder (must not be {@code null})
     */
    default void caseOf(String val, Consumer<BlockCreator> body) {
        caseOf(Const.of(val), body);
    }

    /**
     * Add a simple switch case.
     *
     * @param val the case value to add (must not be {@code null})
     * @param body the switch case body builder (must not be {@code null})
     */
    default void caseOf(int val, Consumer<BlockCreator> body) {
        caseOf(Const.of(val), body);
    }

    /**
     * Add a simple switch case.
     *
     * @param val the case value to add (must not be {@code null})
     * @param body the switch case body builder (must not be {@code null})
     */
    default void caseOf(long val, Consumer<BlockCreator> body) {
        caseOf(Const.of(val), body);
    }

    /**
     * Add the default case.
     *
     * @param body the builder for the body of the default case (must not be {@code null})
     */
    void default_(Consumer<BlockCreator> body);
}
