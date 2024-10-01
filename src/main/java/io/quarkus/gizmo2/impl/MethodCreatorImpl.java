package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.creator.MethodCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public abstract sealed class MethodCreatorImpl extends ExecutableCreatorImpl implements MethodCreator permits AbstractMethodCreatorImpl, DefaultMethodCreatorImpl, InstanceMethodCreatorImpl, NativeMethodCreatorImpl, PrivateInterfaceMethodCreatorImpl, StaticMethodCreatorImpl, StaticNativeMethodCreatorImpl {
    protected final String name;
    private ClassDesc returnType = ConstantDescs.CD_void;
    private MethodDesc desc;

    MethodCreatorImpl(final TypeCreatorImpl owner, final String name, final int flags) {
        super(owner, flags);
        this.name = name;
    }

    public MethodDesc desc() {
        MethodDesc desc = this.desc;
        if (desc == null) {
            MethodTypeDesc mtd = MethodTypeDesc.of(returnType, params.stream().map(ParamVarImpl::type).toArray(ClassDesc[]::new));
            this.desc = desc = owner instanceof InterfaceCreatorImpl ? InterfaceMethodDesc.of(owner.type(), name, mtd) : ClassMethodDesc.of(owner.type(), name, mtd);
        }
        return desc;
    }

    public void returning(final ClassDesc type) {
        this.desc = null;
        returnType = type;
    }

    public void returning(final Class<?> type) {
        returning(Util.classDesc(type));
    }

    public MethodTypeDesc type() {
        return desc().type();
    }

    public String name() {
        return name;
    }
}
