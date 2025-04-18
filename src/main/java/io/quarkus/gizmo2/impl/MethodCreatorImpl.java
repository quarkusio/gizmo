package io.quarkus.gizmo2.impl;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.util.Set;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.MethodCreator;
import io.quarkus.gizmo2.desc.MethodDesc;

public abstract sealed class MethodCreatorImpl extends ExecutableCreatorImpl implements MethodCreator
        permits AbstractMethodCreatorImpl, DefaultMethodCreatorImpl, InstanceMethodCreatorImpl, NativeMethodCreatorImpl,
        InterfaceMethodCreatorImpl, PrivateInterfaceMethodCreatorImpl, StaticMethodCreatorImpl,
        StaticInterfaceMethodCreatorImpl, StaticNativeMethodCreatorImpl {
    final String name;
    private MethodDesc desc;

    MethodCreatorImpl(final TypeCreatorImpl owner, final String name, final Set<AccessFlag> defaultFlags,
            Set<AccessFlag> allowedFlags) {
        super(owner, defaultFlags, allowedFlags);
        this.name = name;
    }

    public MethodDesc desc() {
        MethodDesc desc = this.desc;
        if (desc == null) {
            this.desc = desc = typeCreator.methodDesc(name(), type());
        }
        return desc;
    }

    public void returning(final ClassDesc type) {
        super.returning(type);
    }

    void clearType() {
        desc = null;
        super.clearType();
    }

    public String name() {
        return name;
    }

    ElementType annotationTargetType() {
        return ElementType.METHOD;
    }
}
