package io.quarkus.gizmo2.impl;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.creator.MethodCreator;
import io.quarkus.gizmo2.creator.TypeParameterCreator;
import io.quarkus.gizmo2.desc.MethodDesc;

public abstract sealed class MethodCreatorImpl extends ExecutableCreatorImpl implements MethodCreator
        permits AbstractMethodCreatorImpl, DefaultMethodCreatorImpl, InstanceMethodCreatorImpl, NativeMethodCreatorImpl,
        InterfaceMethodCreatorImpl, PrivateInterfaceMethodCreatorImpl, StaticMethodCreatorImpl,
        StaticInterfaceMethodCreatorImpl {
    final String name;
    private MethodDesc desc;

    MethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner);
        this.name = name;
    }

    public MethodDesc desc() {
        MethodDesc desc = this.desc;
        if (desc == null) {
            this.desc = desc = typeCreator.methodDesc(name(), type());
        }
        return desc;
    }

    public void returning(final GenericType type) {
        super.returning(type);
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

    public ElementType annotationTargetType() {
        return ElementType.METHOD;
    }

    @Override
    public GenericType.OfTypeVariable typeParameter(final String name, final Consumer<TypeParameterCreator> builder) {
        TypeParameterCreatorImpl creator = new TypeParameterCreatorImpl(name);
        builder.accept(creator);
        return addTypeParameter(creator.forMethod(desc)).genericType();
    }
}
