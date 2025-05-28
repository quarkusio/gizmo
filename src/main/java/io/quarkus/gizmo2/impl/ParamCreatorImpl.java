package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.creator.ParamCreator;

public final class ParamCreatorImpl extends ModifiableCreatorImpl implements ParamCreator {
    boolean typeEstablished;
    ClassDesc type;

    public ParamCreatorImpl() {
    }

    public ParamCreatorImpl(final ClassDesc type) {
        this.type = type;
        typeEstablished = true;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.PARAMETER;
    }

    ParamVarImpl apply(final Consumer<ParamCreator> builder, final String name, final int index, final int slot) {
        builder.accept(this);
        if (type == null) {
            throw new IllegalStateException("Parameter type was not set");
        }
        typeEstablished = true;
        return new ParamVarImpl(type, name, index, slot, modifiers, List.copyOf(invisible.values()),
                List.copyOf(visible.values()));
    }

    public void setType(final ClassDesc type) {
        checkNotNullParam("type", type);
        if (type.equals(ConstantDescs.CD_void)) {
            throw new IllegalArgumentException("Parameter cannot have a type of void");
        }
        if (typeEstablished && !type.equals(this.type)) {
            throw new IllegalArgumentException("Given type " + type.displayName()
                    + " differs from established type " + this.type.displayName());
        }
        this.type = type;
    }

    public ClassDesc type() {
        return type;
    }

    public ElementType annotationTargetType() {
        return ElementType.PARAMETER;
    }
}
