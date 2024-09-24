package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.MethodDesc;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

/**
 *
 */
public final class InstanceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    InstanceMethodCreatorImpl(final TypeCreatorImpl owner, final String name, final int flags) {
        super(owner, name, flags);
    }

    public void body(final BiConsumer<BlockCreator, Expr> builder) {

    }

    public MethodDesc desc() {
        return null;
    }

    public void returning(final ClassDesc type) {

    }

    public void returning(final Class<?> type) {

    }

    public MethodTypeDesc type() {
        return null;
    }

    public ParamVar parameter(final String name, final ClassDesc type) {
        return null;
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PRIVATE, PROTECTED, SYNCHRONIZED, SYNTHETIC, BRIDGE -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    public ClassDesc owner() {
        return null;
    }

    public String name() {
        return "";
    }

    public void withAnnotation(final RetentionPolicy retention, final Annotation annotation) {

    }
}
