package io.quarkus.gizmo;

import java.util.Objects;
import java.util.function.Consumer;

abstract class AbstractSwitch<T> extends BytecodeCreatorImpl implements Switch<T> {

    protected static final Consumer<BytecodeCreator> EMPTY_BLOCK = bc -> {
    };

    protected boolean fallThrough;
    protected Consumer<BytecodeCreator> defaultBlockConsumer;

    AbstractSwitch(BytecodeCreatorImpl enclosing) {
        super(enclosing);
    }

    @Override
    public void fallThrough() {
        fallThrough = true;
    }

    @Override
    public void defaultCase(Consumer<BytecodeCreator> defatultBlockConsumer) {
        Objects.requireNonNull(defatultBlockConsumer);
        this.defaultBlockConsumer = defatultBlockConsumer;
    }

    @Override
    public void doBreak(BytecodeCreator creator) {
        creator.breakScope(this);
    }

}
