package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.function.Consumer;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.creator.ParamCreator;

public final class ParamCreatorImpl extends ModifiableCreatorImpl implements ParamCreator {
    boolean typeEstablished;
    ClassDesc type;
    GenericType genericType;

    public ParamCreatorImpl(final GizmoImpl gizmo) {
        super(gizmo);
    }

    public ParamCreatorImpl(final GizmoImpl gizmo, final GenericType type) {
        super(gizmo);
        this.genericType = type;
        typeEstablished = true;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.PARAMETER;
    }

    ParamVarImpl apply(final Consumer<ParamCreator> builder, final String name, final int index, final int slot) {
        builder.accept(this);
        if (type == null && genericType == null) {
            throw new IllegalStateException("Parameter type was not set");
        }
        typeEstablished = true;
        return new ParamVarImpl(type, genericType, name, index, slot, modifiers, List.copyOf(invisible.values()),
                List.copyOf(visible.values()));
    }

    public void setType(final GenericType genericType) {
        checkNotNullParam("type", genericType);
        if (typeEstablished && !genericType.equals(this.genericType)) {
            throw new IllegalArgumentException(
                    "Given type " + genericType + " differs from established type " + this.genericType);
        }
        setType(genericType.desc());
        this.genericType = genericType;
    }

    public void setType(final ClassDesc type) {
        checkNotNullParam("type", type);
        if (type.equals(ConstantDescs.CD_void)) {
            throw new IllegalArgumentException("Bad type for parameter: " + type);
        }
        if (typeEstablished && !type.equals(this.type)) {
            throw new IllegalArgumentException(
                    "Given type " + type + " differs from established type " + this.type);
        }
        this.type = type;
    }

    public ClassDesc type() {
        return type;
    }

    public GenericType genericType() {
        return genericType;
    }

    public ElementType annotationTargetType() {
        return ElementType.PARAMETER;
    }
}
