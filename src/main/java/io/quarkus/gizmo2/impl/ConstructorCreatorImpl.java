package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;

public final class ConstructorCreatorImpl extends ExecutableCreatorImpl implements ConstructorCreator {
    private ConstructorDesc desc;

    ConstructorCreatorImpl(final TypeCreatorImpl owner) {
        super(owner, 0);
    }

    public ConstructorDesc desc() {
        ConstructorDesc desc = this.desc;
        if (desc == null) {
            this.desc = desc = ConstructorDesc.of(owner(), type());
        }
        return desc;
    }

    public void withType(final MethodTypeDesc desc) {
        if (! desc.returnType().equals(CD_void)) {
            throw new IllegalArgumentException("Constructors must return void");
        }
        super.withType(desc);
    }

    public String name() {
        return ConstructorCreator.super.name();
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    @Override
    public This this_() {
        return super.this_();
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PRIVATE, PROTECTED, SYNTHETIC, BRIDGE, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super ConstructorCreatorImpl> builder) {
        builder.accept(this);
    }


    void clearType() {
        desc = null;
        super.clearType();
    }
}
