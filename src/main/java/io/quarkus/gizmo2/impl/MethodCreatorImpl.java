package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.creator.MethodCreator;
import io.quarkus.gizmo2.desc.MethodDesc;

public abstract sealed class MethodCreatorImpl extends ExecutableCreatorImpl implements MethodCreator permits AbstractMethodCreatorImpl, DefaultMethodCreatorImpl, InstanceMethodCreatorImpl, NativeMethodCreatorImpl, InterfaceMethodCreatorImpl, PrivateInterfaceMethodCreatorImpl, StaticMethodCreatorImpl, StaticInterfaceMethodCreatorImpl, StaticNativeMethodCreatorImpl {
    final String name;
    private MethodDesc desc;

    MethodCreatorImpl(final TypeCreatorImpl owner, final String name, final int flags) {
        super(owner, flags);
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
}
