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

    void case_(Constant val, Consumer<BlockCreator> body);

    default void case_(ConstantDesc val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    default void case_(Constable val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    default void case_(int val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    default void case_(long val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    default void case_(String val, Consumer<BlockCreator> body) {
        case_(Constant.of(val), body);
    }

    void default_(Consumer<BlockCreator> body);
}
