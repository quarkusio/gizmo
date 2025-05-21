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
    GenericType genericType;

    public ParamCreatorImpl() {
    }

    public ParamCreatorImpl(final GenericType type) {
        this.genericType = type;
        typeEstablished = true;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.PARAMETER;
    }

    ParamVarImpl apply(final Consumer<ParamCreator> builder, final String name, final int index, final int slot) {
        builder.accept(this);
        if (genericType == null) {
            throw new IllegalStateException("Parameter type was not set");
        }
        typeEstablished = true;
        return new ParamVarImpl(genericType, name, index, slot, modifiers, List.copyOf(invisible.values()),
                List.copyOf(visible.values()));
    }

    public void withType(final GenericType genericType) {
        checkNotNullParam("type", genericType);
        if (genericType.desc().equals(ConstantDescs.CD_void)) {
            throw new IllegalArgumentException("Bad genericType for parameter: " + genericType);
        }
        if (typeEstablished && !genericType.equals(this.genericType)) {
            throw new IllegalArgumentException(
                    "Given type " + genericType + " differs from established type " + this.genericType);
        }
        this.genericType = genericType;
    }

    public void withType(final ClassDesc type) {
        checkNotNullParam("type", type);
        withType(GenericType.of(type));
    }

    public ClassDesc type() {
        return genericType.desc();
    }

    public GenericType genericType() {
        return genericType;
    }

    public ElementType annotationTargetType() {
        return ElementType.PARAMETER;
    }
}
