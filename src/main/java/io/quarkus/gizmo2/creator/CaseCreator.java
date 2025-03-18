package io.quarkus.gizmo2.creator;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.impl.SwitchCreatorImpl;

/**
 * A creator for {@code switch} statement and expression cases.
 * Each case must declare at least one case value before building the body of the case.
 */
public sealed interface CaseCreator extends BodyCreator permits SwitchCreatorImpl.CaseCreatorImpl {
    /**
     * Add a constant for this case.
     *
     * @param val the case value to add (must not be {@code null})
     */
    void of(Constant val);

    /**
     * Add a constant for this case.
     *
     * @param val the case value to add (must not be {@code null})
     */
    default void of(Constable val) {
        of(Constant.of(val));
    }

    /**
     * Add a constant for this case.
     *
     * @param val the case value to add (must not be {@code null})
     */
    default void of(ConstantDesc val) {
        of(Constant.of(val));
    }

    /**
     * Add a constant for this case.
     *
     * @param val the case value to add (must not be {@code null})
     */
    default void of(String val) {
        of(Constant.of(val));
    }

    /**
     * Add a constant for this case.
     *
     * @param val the case value to add (must not be {@code null})
     */
    default void of(int val) {
        of(Constant.of(val));
    }

    /**
     * Add a constant for this case.
     *
     * @param val the case value to add (must not be {@code null})
     */
    default void of(long val) {
        of(Constant.of(val));
    }

    void body(Consumer<BlockCreator> builder);
}
