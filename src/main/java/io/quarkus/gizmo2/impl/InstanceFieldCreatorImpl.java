package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.attribute.SignatureAttribute;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.smallrye.common.constraint.Assert;

public final class InstanceFieldCreatorImpl extends FieldCreatorImpl implements InstanceFieldCreator {

    private Const initial;
    private Consumer<BlockCreator> initializer;

    public InstanceFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(owner, name, tc);
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_INSTANCE_FIELD;
    }

    public void setInitial(final Const initial) {
        Assert.checkNotNullParam("initial", initial);
        checkOneInit();
        setType(initial.type());
        this.initial = initial;
    }

    public void setInitializer(final Consumer<BlockCreator> init) {
        Assert.checkNotNullParam("init", init);
        checkOneInit();
        initializer = (b0 -> {
            FieldDesc desc = desc();
            b0.set(tc.this_().field(desc), b0.blockExpr(desc.type(), init));
        });
    }

    private void checkOneInit() {
        if (initial != null || initializer != null) {
            throw new IllegalStateException("An instance field may have only one initializer");
        }
    }

    void accept(final Consumer<InstanceFieldCreator> builder) {
        builder.accept(this);
        if (initial != null) {
            tc.instancePreinitializer(b0 -> b0.set(tc.this_().field(desc()), initial));
        } else if (initializer != null) {
            tc.instanceInitializer(initializer);
        }
        tc.zb.withField(name(), desc().type(), fb -> {
            fb.withFlags(modifiers);
            fb.with(SignatureAttribute.of(Util.signatureOf(genericType())));
            addVisible(fb);
            addInvisible(fb);
            addTypeAnnotations(fb);
        });
    }
}
