package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

interface Scoped<S extends Scoped<S>> {
    void accept(Consumer<? super S> handler);
}
