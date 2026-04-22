package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.smallrye.classfile.attribute.ConstantValueAttribute;
import io.smallrye.classfile.attribute.SignatureAttribute;
import io.smallrye.common.constraint.Assert;

public sealed abstract class StaticFieldCreatorImpl extends FieldCreatorImpl implements StaticFieldCreator
        permits ClassStaticFieldCreatorImpl, InterfaceStaticFieldCreatorImpl {

    // it is only possible to emit a `ConstantValue` for these types
    // see `ConstantValueAttribute.of()`
    private static final Set<String> CONSTANT_TYPE_DESCS = Set.of(
            CD_int.descriptorString(),
            CD_long.descriptorString(),
            CD_float.descriptorString(),
            CD_double.descriptorString(),
            CD_String.descriptorString());

    private Const initial;
    private Consumer<BlockCreator> initializer;

    protected StaticFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(owner, name, tc);
    }

    public void setInitial(final Const initial) {
        Assert.checkNotNullParam("initial", initial);
        checkOneInit();
        setType(initial.type());
        if (CONSTANT_TYPE_DESCS.contains(initial.type().descriptorString())) {
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
            if (genericType != null && !genericType.isRaw()) {
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
