package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.attribute.ConstantValueAttribute;
import io.github.dmlloyd.classfile.attribute.SignatureAttribute;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.smallrye.common.constraint.Assert;

public sealed abstract class StaticFieldCreatorImpl extends FieldCreatorImpl implements StaticFieldCreator
        permits ClassStaticFieldCreatorImpl, InterfaceStaticFieldCreatorImpl {
    private Const initial;
    private Consumer<BlockCreator> initializer;

    protected StaticFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(owner, name, tc);
    }

    public void setInitial(final Const initial) {
        Assert.checkNotNullParam("initial", initial);
        checkOneInit();
        setType(initial.type());
        if (initial.type().isPrimitive() || initial.type().equals(CD_String)) {
            this.initial = initial;
        } else {
            initializer = bc -> bc.setStaticField(desc(), initial);
        }
    }

    public void setInitializer(final Consumer<BlockCreator> init) {
        Assert.checkNotNullParam("init", init);
        checkOneInit();
        initializer = (b0 -> {
            FieldDesc desc = desc();
            b0.setStaticField(desc, b0.blockExpr(desc.type(), init));
        });
    }

    private void checkOneInit() {
        if (initial != null || initializer != null) {
            throw new IllegalStateException("A static field may have only one initializer");
        }
    }

    void accept(Consumer<StaticFieldCreator> builder) {
        builder.accept(this);
        if (initializer != null) {
            tc.staticInitializer(initializer);
        }
        tc.zb.withField(name(), desc().type(), fb -> {
            fb.withFlags(modifiers);
            if (!genericType.isRaw() || genericType.hasAnnotations()) {
                fb.with(SignatureAttribute.of(Util.signatureOf(genericType())));
            }
            addVisible(fb);
            addInvisible(fb);
            if (initial != null) {
                fb.with(ConstantValueAttribute.of(initial.desc()));
            }
        });
    }
}
