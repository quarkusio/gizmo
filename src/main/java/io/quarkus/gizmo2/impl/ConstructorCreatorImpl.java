package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.ElementType;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.Consumer;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.creator.TypeParameterCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;

public final class ConstructorCreatorImpl extends ExecutableCreatorImpl implements ConstructorCreator {
    private final List<Consumer<BlockCreator>> preInits;
    private final List<Consumer<BlockCreator>> postInits;
    private ConstructorDesc desc;

    ConstructorCreatorImpl(final TypeCreatorImpl owner, final List<Consumer<BlockCreator>> preInits,
            final List<Consumer<BlockCreator>> postInits) {
        super(owner);
        this.preInits = preInits;
        this.postInits = postInits;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_CONSTRUCTOR;
    }

    public ConstructorDesc desc() {
        ConstructorDesc desc = this.desc;
        if (desc == null) {
            this.desc = desc = ConstructorDesc.of(owner(), type());
        }
        return desc;
    }

    public void setType(final MethodTypeDesc desc) {
        if (!Util.isVoid(desc.returnType())) {
            throw new IllegalArgumentException("Constructors must return void");
        }
        super.setType(desc);
    }

    public String name() {
        return ConstructorCreator.super.name();
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(b0 -> {
            for (Consumer<BlockCreator> preInit : preInits) {
                preInit.accept(b0);
            }
            ((BlockCreatorImpl) b0).postInit(postInits);
            builder.accept(b0);
        });
    }

    void accept(final Consumer<? super ConstructorCreatorImpl> builder) {
        builder.accept(this);
    }

    void clearType() {
        desc = null;
        super.clearType();
    }

    public ElementType annotationTargetType() {
        return ElementType.CONSTRUCTOR;
    }

    @Override
    public GenericType.OfTypeVariable typeParameter(final String name, final Consumer<TypeParameterCreator> builder) {
        TypeParameterCreatorImpl creator = new TypeParameterCreatorImpl(name);
        builder.accept(creator);
        return addTypeParameter(creator.forConstructor(desc)).genericType();
    }
}
