package io.quarkus.gizmo2.creator;

import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.InterfaceCreatorImpl;

/**
 * A creator for an interface type.
 */
public sealed interface InterfaceCreator extends TypeCreator permits InterfaceCreatorImpl {

    /**
     * Add a default method to the interface.
     * These methods are always {@code public}.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc defaultMethod(String name, Consumer<InstanceMethodCreator> builder);

    /**
     * Add a default method to the interface having the given predefined type.
     * These methods are always {@code public}.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc defaultMethod(String name, MethodTypeDesc type, Consumer<InstanceMethodCreator> builder) {
        return defaultMethod(name, imc -> {
            imc.withType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a default method to the interface having the same name and type as the given method.
     * These methods are always {@code public}.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc defaultMethod(MethodDesc desc, Consumer<InstanceMethodCreator> builder) {
        return defaultMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a private instance method to the interface.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc privateMethod(String name, Consumer<InstanceMethodCreator> builder);

    /**
     * Add a private method to the interface having the given predefined type.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc privateMethod(String name, MethodTypeDesc type, Consumer<InstanceMethodCreator> builder) {
        return privateMethod(name, imc -> {
            imc.withType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a private method to the interface having the same name and type as the given method.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc privateMethod(MethodDesc desc, Consumer<InstanceMethodCreator> builder) {
        return privateMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add an interface method to the interface.
     * These methods are always {@code public}.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc method(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add an interface method to the interface having the given predefined type.
     * These methods are always {@code public}.
     *
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc method(String name, MethodTypeDesc type, Consumer<AbstractMethodCreator> builder) {
        return method(name, amc -> {
            amc.withType(type);
            builder.accept(amc);
        });
    }

    /**
     * Add an interface method to the interface having the same name and type as the given method.
     * These methods are always {@code public}.
     *
     * @param desc the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc method(MethodDesc desc, Consumer<AbstractMethodCreator> builder) {
        return method(desc.name(), desc.type(), builder);
    }
}
