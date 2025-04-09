package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PRIVATE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PROTECTED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PUBLIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;

public final class ConstructorCreatorImpl extends ExecutableCreatorImpl implements ConstructorCreator {
    private final List<Consumer<BlockCreator>> preInits;
    private final List<Consumer<BlockCreator>> postInits;
    private ConstructorDesc desc;

    ConstructorCreatorImpl(final TypeCreatorImpl owner, final List<Consumer<BlockCreator>> preInits,
            final List<Consumer<BlockCreator>> postInits) {
        super(owner, Set.of(), Set.of(PUBLIC, PRIVATE, PROTECTED, SYNTHETIC, BRIDGE, VARARGS));
        this.preInits = preInits;
        this.postInits = postInits;
    }

    public ConstructorDesc desc() {
        ConstructorDesc desc = this.desc;
        if (desc == null) {
            this.desc = desc = ConstructorDesc.of(owner(), type());
        }
        return desc;
    }

    public void withType(final MethodTypeDesc desc) {
        if (!desc.returnType().equals(CD_void)) {
            throw new IllegalArgumentException("Constructors must return void");
        }
        super.withType(desc);
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
}
