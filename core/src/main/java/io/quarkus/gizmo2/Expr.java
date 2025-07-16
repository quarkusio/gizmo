package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.Item;
import io.quarkus.gizmo2.impl.StaticFieldVarImpl;

/**
 * An expression.
 */
public sealed interface Expr extends SimpleTyped permits Const, Assignable, This, Item {
    /**
     * {@return the expression type (not {@code null})}
     */
    ClassDesc type();

    /**
     * {@return the generic expression type (not {@code null})}
     */
    default GenericType genericType() {
        return GenericType.of(type());
    }

    /**
     * {@return an assignable for an element of this array}
     *
     * @param index the array index (must not be {@code null})
     */
    Assignable elem(Expr index);

    /**
     * {@return an assignable for an element of this array}
     *
     * @param index the array index (must not be {@code null})
     */
    default Assignable elem(Integer index) {
        return elem(Const.of(index));
    }

    /**
     * {@return an assignable for an element of this array}
     *
     * @param index the array index
     */
    default Assignable elem(int index) {
        return elem(Const.of(index));
    }

    /**
     * {@return the length of this array}
     */
    Expr length();

    /**
     * {@return an assignable for a field of this object}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    default InstanceFieldVar field(FieldDesc desc) {
        return field(desc, GenericType.of(desc.type()));
    }

    /**
     * {@return an assignable for a field of this object}
     *
     * @param desc the field descriptor (must not be {@code null})
     * @param genericType the field's expected generic type (must not be {@code null})
     */
    InstanceFieldVar field(FieldDesc desc, GenericType genericType);

    /**
     * {@return an assignable for a field of this object}
     *
     * @param owner the descriptor of the owner of this field (must not be {@code null})
     * @param name the name of the field (must not be {@code null})
     * @param type the descriptor for the type of the field (must not be {@code null})
     */
    default InstanceFieldVar field(ClassDesc owner, String name, ClassDesc type) {
        return field(FieldDesc.of(owner, name, type));
    }

    /**
     * {@return an assignable for a field of this object}
     *
     * @param owner the descriptor of the owner of this field (must not be {@code null})
     * @param name the name of the field (must not be {@code null})
     * @param type the generic type of the field (must not be {@code null})
     */
    default InstanceFieldVar field(ClassDesc owner, String name, GenericType type) {
        return field(FieldDesc.of(owner, name, type.desc()), type);
    }

    /**
     * {@return an assignable for a static field}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static StaticFieldVar staticField(FieldDesc desc) {
        return staticField(desc, GenericType.of(desc.type()));
    }

    /**
     * {@return an assignable for a static field}
     *
     * @param desc the field descriptor (must not be {@code null})
     * @param genericType the field's expected generic type (must not be {@code null})
     */
    static StaticFieldVar staticField(FieldDesc desc, GenericType genericType) {
        if (!desc.type().equals(genericType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match field type %s".formatted(genericType, desc.type()));
        }
        return new StaticFieldVarImpl(desc, genericType);
    }
}
