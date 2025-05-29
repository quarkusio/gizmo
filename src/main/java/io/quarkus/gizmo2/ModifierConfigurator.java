package io.quarkus.gizmo2;

import java.util.List;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.creator.ModifierLocation;

/**
 * A configurator for default modifiers.
 */
public interface ModifierConfigurator {
    void remove(ModifierLocation location, ModifierFlag modifierFlag);

    default void remove(ModifierLocation location, ModifierFlag... modifierFlags) {
        remove(location, List.of(modifierFlags));
    }

    default void remove(ModifierLocation location, List<ModifierFlag> modifierFlags) {
        modifierFlags.forEach(flag -> remove(location, flag));
    }

    void add(ModifierLocation location, ModifierFlag modifierFlag);

    default void add(ModifierLocation location, ModifierFlag... modifierFlags) {
        add(location, List.of(modifierFlags));
    }

    default void add(ModifierLocation location, List<ModifierFlag> modifierFlags) {
        modifierFlags.forEach(flag -> add(location, flag));
    }

    void set(ModifierLocation location, AccessLevel accessLevel);
}
