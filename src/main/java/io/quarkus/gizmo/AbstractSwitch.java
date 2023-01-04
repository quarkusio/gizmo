package io.quarkus.gizmo;

import java.util.Objects;
import java.util.function.Consumer;

abstract class AbstractSwitch<T> extends BytecodeCreatorImpl implements Switch<T> {

    protected boolean fallThrough;
    protected final BytecodeCreatorImpl defaultBlock;

    AbstractSwitch(BytecodeCreatorImpl enclosing) {
        super(enclosing);
        this.defaultBlock = new BytecodeCreatorImpl(this);
    }

    @Override
    public void fallThrough() {
        fallThrough = true;
    }

    @Override
    public void defaultCase(Consumer<BytecodeCreator> defatultBlockConsumer) {
        Objects.requireNonNull(defatultBlockConsumer);
        defatultBlockConsumer.accept(defaultBlock);
    }

    @Override
    public void doBreak(BytecodeCreator creator) {
        creator.breakScope(this);
    }

}
