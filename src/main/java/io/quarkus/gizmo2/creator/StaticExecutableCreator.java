package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;


public non-sealed interface StaticExecutableCreator extends ExecutableCreator {

    void body(Consumer<BlockCreator> builder);
}
