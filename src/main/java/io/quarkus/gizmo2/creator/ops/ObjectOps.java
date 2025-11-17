package io.quarkus.gizmo2.creator.ops;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.constant.ClassDesc;
import java.util.List;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.Util;

/**
 * Operations on {@link Object}.
 */
public class ObjectOps {
    final Class<?> receiverType;
    final ClassDesc receiverTypeDesc;
    /**
     * The block creator (not {@code null}).
     */
    protected final BlockCreator bc;
    /**
     * The receiver object (not {@code null}).
     */
    protected final Expr obj;

    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver object (must not be {@code null})
     */
    public ObjectOps(final BlockCreator bc, final Expr obj) {
        this(Object.class, bc, obj);
    }

    /**
     * Construct a new subclass instance.
     *
     * @param receiverType the type of the receiver (must not be {@code null})
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver object (must not be {@code null})
     */
    protected ObjectOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        checkNotNullParam("receiverType", receiverType);
        checkNotNullParam("bc", bc);
        checkNotNullParam("obj", obj);
        this.receiverType = receiverType;
        receiverTypeDesc = Util.classDesc(receiverType);
        this.bc = bc;
        this.obj = obj;
    }

    /**
     * {@return the receiver type}
     */
    protected Class<?> receiverType() {
        return receiverType;
    }

    /**
     * {@return the block creator}
     */
    protected BlockCreator blockCreator() {
        return bc;
    }

    /**
     * {@return the receiver object expression}
     */
    protected Expr receiver() {
        return obj;
    }

    /**
     * Perform an instance invocation of a method with 3 parameters.
     *
     * @param returnType the return type (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param param0Type the first parameter type (must not be {@code null})
     * @param param1Type the second parameter type (must not be {@code null})
     * @param param2Type the third parameter type (must not be {@code null})
     * @param arg0 the first argument value (must not be {@code null})
     * @param arg1 the second argument value (must not be {@code null})
     * @param arg2 the third argument value (must not be {@code null})
     * @return the invocation result expression (not {@code null})
     */
    protected Expr invokeInstance(Class<?> returnType, String name, Class<?> param0Type, Class<?> param1Type,
            Class<?> param2Type, Expr arg0, Expr arg1, Expr arg2) {
        MethodDesc md = MethodDesc.of(receiverType, name, returnType, List.of(param0Type, param1Type, param2Type));
        return receiverType.isInterface()
                ? bc.invokeInterface(md, obj, List.of(arg0, arg1, arg2))
                : bc.invokeVirtual(md, obj, List.of(arg0, arg1, arg2));
    }

    /**
     * Perform an instance invocation of a method with 2 parameters.
     *
     * @param returnType the return type (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param param0Type the first parameter type (must not be {@code null})
     * @param param1Type the second parameter type (must not be {@code null})
     * @param arg0 the first argument value (must not be {@code null})
     * @param arg1 the second argument value (must not be {@code null})
     * @return the invocation result expression (not {@code null})
     */
    protected Expr invokeInstance(Class<?> returnType, String name, Class<?> param0Type, Class<?> param1Type, Expr arg0,
            Expr arg1) {
        MethodDesc md = MethodDesc.of(receiverType, name, returnType, List.of(param0Type, param1Type));
        return receiverType.isInterface()
                ? bc.invokeInterface(md, obj, List.of(arg0, arg1))
                : bc.invokeVirtual(md, obj, List.of(arg0, arg1));
    }

    /**
     * Perform an instance invocation of a method with 1 parameter.
     *
     * @param returnType the return type (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param paramType the parameter type (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     * @return the invocation result expression (not {@code null})
     */
    protected Expr invokeInstance(Class<?> returnType, String name, Class<?> paramType, Expr arg) {
        MethodDesc md = MethodDesc.of(receiverType, name, returnType, List.of(paramType));
        return receiverType.isInterface() ? bc.invokeInterface(md, obj, List.of(arg)) : bc.invokeVirtual(md, obj, List.of(arg));
    }

    /**
     * Perform an instance invocation of a method with no parameter.
     *
     * @param returnType the return type (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @return the invocation result expression (not {@code null})
     */
    protected Expr invokeInstance(Class<?> returnType, String name) {
        MethodDesc md = MethodDesc.of(receiverType, name, returnType, List.of());
        return receiverType.isInterface() ? bc.invokeInterface(md, obj, List.of()) : bc.invokeVirtual(md, obj, List.of());
    }

    /**
     * Perform an instance invocation of a method with no parameter that returns {@code void}.
     *
     * @param name the method name (must not be {@code null})
     */
    protected void invokeInstance(String name) {
        MethodDesc md = MethodDesc.of(receiverType, name, void.class, List.of());
        if (receiverType.isInterface()) {
            bc.invokeInterface(md, obj, List.of());
        } else {
            bc.invokeVirtual(md, obj, List.of());
        }
    }

    /**
     * Generate a call to {@link Object#getClass()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getClass_() {
        return invokeInstance(Class.class, "getClass");
    }

    /**
     * Generate a call to {@link Object#toString()}.
     * For a {@code null}-safe variation, use {@link BlockCreator#objToString(Expr)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr toString_() {
        return invokeInstance(String.class, "toString");
    }

    /**
     * Generate a call to {@link Object#equals(Object)}.
     * For a {@code null}-safe variation, use {@link BlockCreator#objEquals(Expr, Expr)}.
     *
     * @param otherObj the object to compare (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr equals_(Expr otherObj) {
        return invokeInstance(boolean.class, "equals", Object.class, otherObj);
    }

    /**
     * Generate a call to {@link Object#hashCode()}.
     * For a {@code null}-safe variation, use {@link BlockCreator#objHashCode(Expr)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr hashCode_() {
        return invokeInstance(int.class, "hashCode");
    }
}
