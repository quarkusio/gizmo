package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

public final class InstanceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    InstanceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, 0);
    }

    public void body(final BiConsumer<BlockCreator, Expr> builder) {
        super.body(bc -> builder.accept(bc, new ExprImpl() {
            public boolean bound() {
                return false;
            }

            public ClassDesc type() {
                return owner.type();
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.aload(0);
            }
        }));
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PRIVATE, PROTECTED, SYNCHRONIZED, SYNTHETIC, BRIDGE, FINAL -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }
}
