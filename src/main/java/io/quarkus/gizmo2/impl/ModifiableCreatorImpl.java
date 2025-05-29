package io.quarkus.gizmo2.impl;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ModifiableCreator;
import io.quarkus.gizmo2.creator.Modifier;
import io.quarkus.gizmo2.creator.ModifierFlag;

public abstract sealed class ModifiableCreatorImpl extends AnnotatableCreatorImpl implements ModifiableCreator
        permits ExecutableCreatorImpl, FieldCreatorImpl, ParamCreatorImpl, TypeCreatorImpl {
    int modifiers;

    ModifiableCreatorImpl(GizmoImpl gizmo) {
        modifiers = gizmo.getDefaultModifiers(modifierLocation());
    }

    public void addFlag(final ModifierFlag flag) {
        if (requires(flag)) {
            // ignore (it's always set)
            return;
        } else if (supports(flag)) {
            flag.forEachExclusive(this::removeFlag);
            modifiers |= flag.mask();
        } else {
            throw cannotAdd(flag);
        }
    }

    public void removeFlag(final ModifierFlag flag) {
        if (requires(flag)) {
            throw cannotRemove(flag);
        } else if (!supports(flag)) {
            // ignore (it's always clear
            return;
        } else {
            modifiers &= ~flag.mask();
        }
    }

    private boolean requires(final ModifierFlag flag) {
        return modifierLocation().requires(flag);
    }

    public void setAccess(final AccessLevel access) {
        if (supports(access)) {
            modifiers = modifiers & ~AccessLevel.fullMask() | access.mask();
        } else {
            throw unsupported(access);
        }
    }

    private static IllegalArgumentException unsupported(final AccessLevel modifier) {
        return new IllegalArgumentException("Access level \"%s\" cannot be used here".formatted(modifier));
    }

    private static IllegalArgumentException cannotAdd(final Modifier modifier) {
        return new IllegalArgumentException("Modifier \"%s\" cannot be added here".formatted(modifier));
    }

    private static IllegalArgumentException cannotRemove(final Modifier modifier) {
        return new IllegalArgumentException("Modifier \"%s\" cannot be removed here".formatted(modifier));
    }

}
