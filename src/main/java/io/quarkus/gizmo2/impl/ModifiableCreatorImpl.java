package io.quarkus.gizmo2.impl;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ModifiableCreator;
import io.quarkus.gizmo2.creator.Modifier;
import io.quarkus.gizmo2.creator.ModifierFlag;

public abstract sealed class ModifiableCreatorImpl extends AnnotatableCreatorImpl implements ModifiableCreator
        permits ExecutableCreatorImpl, FieldCreatorImpl, ParamCreatorImpl, TypeCreatorImpl {
    int modifiers;

    ModifiableCreatorImpl() {
    }

    public void addFlag(final ModifierFlag flag) {
        if (supports(flag)) {
            modifiers |= flag.mask();
        } else {
            throw modifierUnsupported(flag);
        }
    }

    public void removeFlag(final ModifierFlag flag) {
        if (supports(flag)) {
            modifiers &= ~flag.mask();
        } else {
            throw modifierUnsupported(flag);
        }
    }

    public void setAccess(final AccessLevel access) {
        if (supports(access)) {
            modifiers = modifiers & ~AccessLevel.fullMask() | access.mask();
        } else {
            throw modifierUnsupported(access);
        }
    }

    private static IllegalArgumentException modifierUnsupported(final Modifier modifier) {
        return new IllegalArgumentException("Modifier \"%s\" is not supported here".formatted(modifier));
    }

}
