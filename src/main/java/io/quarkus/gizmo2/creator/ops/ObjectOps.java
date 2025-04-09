package io.quarkus.gizmo2.creator.ops;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Objects;

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
    final BlockCreator bc;
    final Expr obj;

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
        Objects.requireNonNull(receiverType, "receiverType");
        Objects.requireNonNull(bc, "bc");
        Objects.requireNonNull(obj, "obj");
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
     * Perform an instance invocation.
     *
     * @param returnType the return type (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param arg0Type the first argument type (must not be {@code null})
     * @param arg1Type the second argument type (must not be {@code null})
     * @param arg2Type the third argument type (must not be {@code null})
     * @param arg0 the first argument value (must not be {@code null})
     * @param arg1 the second argument value (must not be {@code null})
     * @param arg2 the third argument value (must not be {@code null})
     * @return the invocation result expression (not {@code null})
     */
    protected Expr invokeInstance(Class<?> returnType, String name, Class<?> arg0Type, Class<?> arg1Type, Class<?> arg2Type,
            Expr arg0, Expr arg1, Expr arg2) {
        MethodDesc md = MethodDesc.of(receiverType, name, returnType, List.of(arg0Type, arg1Type, arg2Type));
        return receiverType.isInterface()
                ? bc.invokeInterface(md, obj, List.of(arg0, arg1, arg2))
                : bc.invokeVirtual(md, obj, List.of(arg0, arg1, arg2));
    }

    /**
     * Perform an instance invocation.
     *
     * @param returnType the return type (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param arg0Type the first argument type (must not be {@code null})
     * @param arg1Type the second argument type (must not be {@code null})
     * @param arg0 the first argument value (must not be {@code null})
     * @param arg1 the second argument value (must not be {@code null})
     * @return the invocation result expression (not {@code null})
     */
    protected Expr invokeInstance(Class<?> returnType, String name, Class<?> arg0Type, Class<?> arg1Type, Expr arg0,
            Expr arg1) {
        MethodDesc md = MethodDesc.of(receiverType, name, returnType, List.of(arg0Type, arg1Type));
        return receiverType.isInterface()
                ? bc.invokeInterface(md, obj, List.of(arg0, arg1))
                : bc.invokeVirtual(md, obj, List.of(arg0, arg1));
    }

    /**
     * Perform an instance invocation.
     *
     * @param returnType the return type (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param argType the argument type (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     * @return the invocation result expression (not {@code null})
     */
    protected Expr invokeInstance(Class<?> returnType, String name, Class<?> argType, Expr arg) {
        MethodDesc md = MethodDesc.of(receiverType, name, returnType, List.of(argType));
        return receiverType.isInterface() ? bc.invokeInterface(md, obj, List.of(arg)) : bc.invokeVirtual(md, obj, List.of(arg));
    }

    /**
     * Perform an instance invocation.
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
     * Perform a {@code void} instance invocation on the receiver type.
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
    public Expr objGetClass() {
        return invokeInstance(Class.class, "getClass");
    }

    /**
     * Generate a call to {@link Object#toString()}.
     * For a {@code null}-safe variation, use {@link BlockCreator#exprToString(Expr)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr objToString() {
        return invokeInstance(String.class, "toString");
    }

    /**
     * Generate a call to {@link Object#equals(Object)}.
     * For a {@code null}-safe variation, use {@link BlockCreator#exprEquals(Expr, Expr)}.
     *
     * @param otherObj the object to compare (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr objEquals(Expr otherObj) {
        return invokeInstance(boolean.class, "equals", Object.class, otherObj);
    }

    /**
     * Generate a call to {@link Object#hashCode()}.
     * For a {@code null}-safe variation, use {@link BlockCreator#exprHashCode(Expr)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr objHashCode() {
        return invokeInstance(int.class, "hashCode");
    }
}
