package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;

import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.impl.Item;
import io.quarkus.gizmo2.impl.GizmoImpl;

/**
 * An expression.
 */
public sealed interface Expr permits Constant, LValueExpr, Item {
    /**
     * {@return the expression type (not {@code null})}
     */
    ClassDesc type();

    /**
     * {@return the expression type kind (not {@code null})}
     */
    default TypeKind typeKind() {
        return TypeKind.from(type());
    }

    /**
     * {@return {@code true} if this expression has {@code void} type, or {@code false} otherwise}
     */
    default boolean isVoid() {
        return typeKind() == TypeKind.VOID;
    }

    /**
     * {@return the Java stack slot size of this expression}
     */
    default int slotSize() {
        return typeKind().slotSize();
    }

    /**
     * {@return true if the expression is bound to one location, or false if it may be reused many times}
     */
    boolean bound();

    /**
     * {@return an lvalue for an element of this array}
     * @param index the array index (must not be {@code null})
     */
    LValueExpr elem(Expr index);

    /**
     * {@return an lvalue for an element of this array}
     * @param index the array index (must not be {@code null})
     */
    default LValueExpr elem(Constant index) {
        return elem((Expr) index);
    }

    /**
     * {@return an lvalue for an element of this array}
     * @param index the array index (must not be {@code null})
     */
    default LValueExpr elem(ConstantDesc index) {
        return elem(Constant.of(index));
    }

    /**
     * {@return an lvalue for an element of this array}
     * @param index the array index (must not be {@code null})
     */
    default LValueExpr elem(Constable index) {
        return elem(Constant.of(index));
    }

    /**
     * {@return an lvalue for an element of this array}
     * @param index the array index
     */
    default LValueExpr elem(int index) {
        return elem(Constant.of(index));
    }

    /**
     * {@return the length of the array represented by this expression}
     */
    Expr length();

    /**
     * {@return an lvalue for a field of this object}
     * @param desc the field descriptor (must not be {@code null})
     */
    InstanceFieldVar field(FieldDesc desc);

    /**
     * {@return an lvalue for a field of this object}
     * @param owner the descriptor of the owner of this field
     * @param name the name of the field
     * @param type the descriptor for the type of the field
     */
    default InstanceFieldVar field(ClassDesc owner, String name, ClassDesc type) {
        return field(FieldDesc.of(owner, name, type));
    }

    /**
     * {@return an lvalue for a static field}
     * @param desc the field descriptor (must not be {@code null})
     */
    static StaticFieldVar staticField(FieldDesc desc) {
        return GizmoImpl.current().staticField(desc);
    }
}
