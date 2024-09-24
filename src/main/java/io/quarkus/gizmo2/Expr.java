package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;

import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.impl.ExprImpl;
import io.quarkus.gizmo2.impl.GizmoImpl;

public sealed interface Expr permits Constant, LValueExpr, ExprImpl {
    ClassDesc type();

    default TypeKind typeKind() {
        return TypeKind.from(type());
    }

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

    InstanceFieldVar field(FieldDesc desc);

    default InstanceFieldVar field(ClassDesc owner, String name, ClassDesc type) {
        return field(FieldDesc.of(owner, name, type));
    }

    static StaticFieldVar staticField(FieldDesc desc) {
        return GizmoImpl.current().staticField(desc);
    }
}
