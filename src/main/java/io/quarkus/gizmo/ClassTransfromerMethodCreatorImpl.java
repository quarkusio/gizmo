package io.quarkus.gizmo;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import java.util.Objects;

import io.quarkus.gizmo.Switch.EnumSwitch;

class ClassTransfromerMethodCreatorImpl extends MethodCreatorImpl {
    private final ClassTransformer transformer;

    ClassTransfromerMethodCreatorImpl(MethodDescriptor methodDescriptor, ClassTransformer transformer) {
        super(null, methodDescriptor, methodDescriptor.getDeclaringClass(), null);
        this.transformer = transformer;
    }

    @Override
    public FunctionCreator createFunction(Class<?> functionalInterface) {
        // Functional interfaces are implemented as separate classes
        throw new UnsupportedOperationException();
    }

    @Override
    public <E extends Enum<E>> EnumSwitch<E> enumSwitch(ResultHandle value, Class<E> enumClass) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(enumClass);
        EnumSwitchImpl<E> enumSwitch = new ClassTransformerEnumSwitchImpl<E>(value, enumClass);
        operations.add(new BlockOperation(enumSwitch));
        return enumSwitch;
    }

    private class ClassTransformerEnumSwitchImpl<E extends Enum<E>> extends EnumSwitchImpl<E> {
        public ClassTransformerEnumSwitchImpl(ResultHandle value, Class<E> enumClass) {
            super(value, enumClass, ClassTransfromerMethodCreatorImpl.this);
        }

        @Override
        protected ResultHandle callSwitchTableMethod(String methodName, Class<E> enumClass, MethodDescriptor enumOrdinal) {
            MethodDescriptor gizmoSwitchTableDescriptor = MethodDescriptor.ofMethod(transformer.getClassName(),
                    methodName, int[].class);
            // `hasAddedMethod` can't say whether the original class has the method,
            // but the probability of collisions is minuscule
            if (!transformer.hasAddedMethod(gizmoSwitchTableDescriptor)) {
                MethodCreator gizmoSwitchTable = transformer.addMethod(gizmoSwitchTableDescriptor)
                        .setModifiers(ACC_PRIVATE | ACC_STATIC);
                gizmoSwitchTable.returnValue(generateSwitchTable(enumClass, gizmoSwitchTable, enumOrdinal));
            }
            return invokeStaticMethod(gizmoSwitchTableDescriptor);
        }
    }
}
