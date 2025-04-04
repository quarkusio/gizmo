package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.GizmoImpl;
import io.quarkus.gizmo2.impl.Item;

/**
 * An expression. All expressions have a value (or "evaluate to a value"). {@link Assignable}
 * expressions also have a location (address), so they can be assigned to. Expressions must be used
 * in a stack-like manner, except of {@linkplain Constant constants}, the {@linkplain This this}
 * reference, and {@linkplain Var variables}; those can be used arbitrarily.
 */
public sealed interface Expr extends SimpleTyped permits Assignable, Constant, Item, This {
    /**
     * {@return the expression type (not {@code null})}
     */
    ClassDesc type();

    /**
     * {@return true if the expression is bound to one location, or false if it may be reused many times}
     */
    boolean bound();

    /**
     * {@return an assignable for an element of this array}
     * @param index the array index (must not be {@code null})
     */
    Assignable elem(Expr index);

    /**
     * {@return an assignable for an element of this array}
     * @param index the array index (must not be {@code null})
     */
    default Assignable elem(Integer index) {
        return elem(Constant.of(index));
    }

    /**
     * {@return an assignable for an element of this array}
     * @param index the array index
     */
    default Assignable elem(int index) {
        return elem(Constant.of(index));
    }

    /**
     * {@return the length of the array represented by this expression}
     */
    Expr length();

    /**
     * {@return an assignable for a field of this object}
     * @param desc the field descriptor (must not be {@code null})
     */
    InstanceFieldVar field(FieldDesc desc);

    /**
     * {@return an assignable for a field of this object}
     * @param owner the descriptor of the owner of this field
     * @param name the name of the field
     * @param type the descriptor for the type of the field
     */
    default InstanceFieldVar field(ClassDesc owner, String name, ClassDesc type) {
        return field(FieldDesc.of(owner, name, type));
    }

    /**
     * {@return an assignable for a static field}
     * @param desc the field descriptor (must not be {@code null})
     */
    static StaticFieldVar staticField(FieldDesc desc) {
        return GizmoImpl.current().staticField(desc);
    }
}
