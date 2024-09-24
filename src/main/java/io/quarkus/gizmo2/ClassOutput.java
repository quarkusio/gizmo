package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;

/**
 * A container for created classes with a specific output strategy.
 */
public interface ClassOutput {
    ClassDesc class_(ClassDesc desc, Consumer<ClassCreator> builder);

    ClassDesc interface_(ClassDesc desc, Consumer<InterfaceCreator> builder);
}
