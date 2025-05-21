package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Preconditions.requireSameLoadableTypeKind;
import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_Byte;
import static java.lang.constant.ConstantDescs.CD_Character;
import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Float;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_Long;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_Short;
import static java.lang.constant.ConstantDescs.CD_Void;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;

import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;

final class Conversions {
    private static final ClassDesc CD_Comparable = Util.classDesc(Comparable.class);
    private static final ClassDesc CD_Constable = Util.classDesc(Constable.class);
    private static final ClassDesc CD_ConstantDesc = Util.classDesc(ConstantDesc.class);
    private static final ClassDesc CD_Number = Util.classDesc(Number.class);
    private static final ClassDesc CD_Serializable = Util.classDesc(Serializable.class);

    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.7
    private static final Map<ClassDesc, ClassDesc> boxTypes = Map.of(
            CD_boolean, CD_Boolean,
            CD_byte, CD_Byte,
            CD_char, CD_Character,
            CD_short, CD_Short,
            CD_int, CD_Integer,
            CD_long, CD_Long,
            CD_float, CD_Float,
            CD_double, CD_Double,
            CD_void, CD_Void);

    private static final Map<ClassDesc, Set<ClassDesc>> additionalBoxTypes = Map.of(
            CD_boolean, Set.of(CD_Object, CD_Comparable, CD_Constable, CD_Serializable),
            CD_byte, Set.of(CD_Object, CD_Number, CD_Comparable, CD_Constable, CD_Serializable),
            CD_char, Set.of(CD_Object, CD_Comparable, CD_Constable, CD_Serializable),
            CD_short, Set.of(CD_Object, CD_Number, CD_Comparable, CD_Constable, CD_Serializable),
            CD_int, Set.of(CD_Object, CD_Number, CD_Comparable, CD_Constable, CD_ConstantDesc, CD_Serializable),
            CD_long, Set.of(CD_Object, CD_Number, CD_Comparable, CD_Constable, CD_ConstantDesc, CD_Serializable),
            CD_float, Set.of(CD_Object, CD_Number, CD_Comparable, CD_Constable, CD_ConstantDesc, CD_Serializable),
            CD_double, Set.of(CD_Object, CD_Number, CD_Comparable, CD_Constable, CD_ConstantDesc, CD_Serializable),
            CD_void, Set.of(CD_Object));

    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.8
    private static final Map<ClassDesc, ClassDesc> unboxTypes = Util.reverseMap(boxTypes);

    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.2
    private static final Map<ClassDesc, Set<ClassDesc>> primitiveWideningConversions = Map.of(
            CD_boolean, Set.of(),
            CD_byte, Set.of(CD_short, CD_int, CD_long, CD_float, CD_double),
            CD_char, Set.of(CD_int, CD_long, CD_float, CD_double),
            CD_short, Set.of(CD_int, CD_long, CD_float, CD_double),
            CD_int, Set.of(CD_long, CD_float, CD_double),
            CD_long, Set.of(CD_float, CD_double),
            CD_float, Set.of(CD_double),
            CD_double, Set.of(),
            CD_void, Set.of());

    /**
     * {@return whether the given {@code type} is primitive}
     */
    static boolean isPrimitive(ClassDesc type) {
        return boxTypes.containsKey(type); // could also be just `type.isPrimitive()`
    }

    /**
     * {@return whether the given {@code type} is a primitive wrapper class}
     */
    static boolean isPrimitiveWrapper(ClassDesc type) {
        return unboxTypes.containsKey(type);
    }

    /**
     * {@return the result of a boxing conversion of given {@code type}}
     * Returns an empty {@code Optional} if no such conversion exists.
     */
    static Optional<ClassDesc> boxingConversion(ClassDesc type) {
        return Optional.ofNullable(boxTypes.get(type));
    }

    /**
     * {@return the result of an unboxing conversion of given {@code type}}
     * Returns an empty {@code Optional} if no such conversion exists.
     */
    static Optional<ClassDesc> unboxingConversion(ClassDesc type) {
        return Optional.ofNullable(unboxTypes.get(type));
    }

    static boolean primitiveWideningExists(ClassDesc fromType, ClassDesc toType) {
        return fromType.isPrimitive() && primitiveWideningConversions.get(fromType).contains(toType);
    }

    /**
     * {@return the given {@code expr} converted to given {@code toType}}
     * If no conversion is possible, returns the {@code expr} unchanged after verifying
     * the type of {@code expr} is of the same loadable type kind as {@code toType}.
     * <p>
     * The conversions applied are:
     * <ul>
     * <li>identity conversion</li>
     * <li>boxing conversion</li>
     * <li>unboxing conversion</li>
     * <li>primitive widening conversion</li>
     * <li>unboxing conversion followed by primitive widening conversion</li>
     * <li>primitive widening conversion followed by boxing conversion</li>
     * </ul>
     * <p>
     * Boxing conversion is supposed to exist from any primitive type to the corresponding
     * wrapper class and all its superclasses and superinterfaces (as existing in Java 17).
     * <p>
     * Unboxing conversion is supposed to exist from any primitive wrapper class to
     * the corresponding primitive type.
     */
    static Item convert(Expr expr, ClassDesc toType) {
        Item item = (Item) expr;
        ClassDesc fromType = item.type();
        if (fromType.equals(toType)) {
            // identity
            return item;
        } else if (toType.equals(boxTypes.get(fromType))) {
            // boxing
            return new Box(item);
        } else if (fromType.isPrimitive() && additionalBoxTypes.get(fromType).contains(toType)) {
            // boxing to a superclass/superinterface of the wrapper class
            return new Box(item, toType);
        } else if (fromType.isPrimitive() && unboxTypes.containsKey(toType)) {
            // primitive widening + boxing
            ClassDesc widerType = unboxTypes.get(toType);
            if (primitiveWideningConversions.get(fromType).contains(widerType)) {
                return new Box(new WidenPrimitive(item, widerType));
            }
        } else if (toType.equals(unboxTypes.get(fromType))) {
            // unboxing
            return new Unbox(item);
        } else if (toType.isPrimitive() && unboxTypes.containsKey(fromType)
                && primitiveWideningConversions.get(unboxTypes.get(fromType)).contains(toType)) {
            // unboxing + primitive widening
            return new WidenPrimitive(new Unbox(item), toType);
        } else if (fromType.isPrimitive() && toType.isPrimitive()
                && primitiveWideningConversions.get(fromType).contains(toType)) {
            // primitive widening
            return new WidenPrimitive(item, toType);
        }

        requireSameLoadableTypeKind(fromType, toType);
        return item;
    }

    /**
     * {@return the promoted type of a binary expression with given input types {@code a} and {@code b}}
     * Returns an empty {@code Optional} if no such promotion exists.
     */
    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.6
    static Optional<ClassDesc> numericPromotion(ClassDesc a, ClassDesc b) {
        if (a.isClassOrInterface()) {
            a = unboxTypes.getOrDefault(a, a);
        }
        if (b.isClassOrInterface()) {
            b = unboxTypes.getOrDefault(b, b);
        }
        if (a.equals(b)) {
            // also covers the case when both `a` and `b` are `boolean`,
            // which may occur in case of `Rel` with `If.Kind.EQ` and `NE`
            return Optional.of(a);
        }
        if (a.isPrimitive() && b.isPrimitive()) {
            TypeKind aKind = TypeKind.from(a);
            TypeKind bKind = TypeKind.from(b);
            if (aKind == TypeKind.DOUBLE || bKind == TypeKind.DOUBLE) {
                return Optional.of(CD_double);
            } else if (aKind == TypeKind.FLOAT || bKind == TypeKind.FLOAT) {
                return Optional.of(CD_float);
            } else if (aKind == TypeKind.LONG || bKind == TypeKind.LONG) {
                return Optional.of(CD_long);
            } else {
                return Optional.of(CD_int);
            }
        }
        return Optional.empty();
    }
}
