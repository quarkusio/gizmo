package io.quarkus.gizmo;

import io.quarkus.gizmo.Switch.EnumSwitch;

/**
 * 
 */
class ClassTransfromerMethodCreatorImpl extends MethodCreatorImpl {

    ClassTransfromerMethodCreatorImpl(MethodDescriptor methodDescriptor) {
        super(null, methodDescriptor, methodDescriptor.getDeclaringClass(), null);
    }

    @Override
    public FunctionCreator createFunction(Class<?> functionalInterface) {
        // Functional interfaces are implemented as separate classes
        throw new UnsupportedOperationException();
    }

    @Override
    public <E extends Enum<E>> EnumSwitch<E> enumSwitch(ResultHandle value, Class<E> enumClass) {
        // Enum switch needs to add a switchtable method to the existing class
        throw new UnsupportedOperationException();
    }

}
