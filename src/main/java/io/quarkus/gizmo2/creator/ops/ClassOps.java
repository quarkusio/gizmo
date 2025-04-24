package io.quarkus.gizmo2.creator.ops;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Class}.
 */
public final class ClassOps extends ObjectOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param clazz the receiver class (must not be {@code null})
     */
    public ClassOps(final BlockCreator bc, final Expr clazz) {
        super(Class.class, bc, clazz);
    }

    /**
     * Generate a call to {@link Class#getName()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getName() {
        return invokeInstance(String.class, "getName");
    }

    /**
     * Generate a call to {@link Class#isInterface()} ()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isInterface() {
        return invokeInstance(boolean.class, "isInterface");
    }

    /**
     * Generate a call to {@link Class#getClassLoader()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getClassLoader() {
        return invokeInstance(ClassLoader.class, "getClassLoader");
    }

    /**
     * Generate a call to {@link Class#asSubclass(Class)}.
     *
     * @param subclass the expression of the subclass {@code Class} object (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr asSubclass(Expr subclass) {
        return invokeInstance(Class.class, "asSubclass", Class.class, subclass);
    }

    /**
     * Generate a call to {@link Class#asSubclass(Class)}.
     *
     * @param clazz the subclass {@code Class} object (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr asSubclass(Class<?> clazz) {
        return asSubclass(Const.of(clazz));
    }

    /**
     * Generate a call to {@link Class#cast(Object)}.
     *
     * @param object the expression of the object to cast (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr cast(Expr object) {
        return invokeInstance(Object.class, "cast", Object.class, object);
    }
}
