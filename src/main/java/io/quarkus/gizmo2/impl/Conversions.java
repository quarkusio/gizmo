package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Preconditions.*;
import static java.lang.constant.ConstantDescs.*;

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

    private static final String DS_boolean = "Z";
    private static final String DS_byte = "B";
    private static final String DS_char = "C";
    private static final String DS_double = "D";
    private static final String DS_float = "F";
    private static final String DS_int = "I";
    private static final String DS_long = "J";
    private static final String DS_short = "S";
    private static final String DS_void = "V";

    private static final String DS_Object = Object.class.descriptorString().intern();

    private static final String DS_Boolean = Boolean.class.descriptorString().intern();
    private static final String DS_Byte = Byte.class.descriptorString().intern();
    private static final String DS_Character = Character.class.descriptorString().intern();
    private static final String DS_Double = Double.class.descriptorString().intern();
    private static final String DS_Float = Float.class.descriptorString().intern();
    private static final String DS_Integer = Integer.class.descriptorString().intern();
    private static final String DS_Long = Long.class.descriptorString().intern();
    private static final String DS_Short = Short.class.descriptorString().intern();
    private static final String DS_Void = Void.class.descriptorString().intern();

    private static final String DS_Comparable = Comparable.class.descriptorString().intern();
    private static final String DS_Constable = Constable.class.descriptorString().intern();
    private static final String DS_Number = Number.class.descriptorString().intern();
    private static final String DS_Serializable = Serializable.class.descriptorString().intern();
    private static final String DS_ConstantDesc = ConstantDesc.class.descriptorString().intern();

    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.7
    private static final Map<String, ClassDesc> boxTypes = Map.of(
            DS_boolean, CD_Boolean,
            DS_byte, CD_Byte,
            DS_char, CD_Character,
            DS_short, CD_Short,
            DS_int, CD_Integer,
            DS_long, CD_Long,
            DS_float, CD_Float,
            DS_double, CD_Double,
            DS_void, CD_Void);

    private static final Map<String, Set<String>> additionalBoxTypes = Map.of(
            DS_boolean, Set.of(DS_Object, DS_Comparable, DS_Constable, DS_Serializable),
            DS_byte, Set.of(DS_Object, DS_Number, DS_Comparable, DS_Constable, DS_Serializable),
            DS_char, Set.of(DS_Object, DS_Comparable, DS_Constable, DS_Serializable),
            DS_short, Set.of(DS_Object, DS_Number, DS_Comparable, DS_Constable, DS_Serializable),
            DS_int, Set.of(DS_Object, DS_Number, DS_Comparable, DS_Constable, DS_ConstantDesc, DS_Serializable),
            DS_long, Set.of(DS_Object, DS_Number, DS_Comparable, DS_Constable, DS_ConstantDesc, DS_Serializable),
            DS_float, Set.of(DS_Object, DS_Number, DS_Comparable, DS_Constable, DS_ConstantDesc, DS_Serializable),
            DS_double, Set.of(DS_Object, DS_Number, DS_Comparable, DS_Constable, DS_ConstantDesc, DS_Serializable),
            DS_void, Set.of(DS_Object));

    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.8
    private static final Map<String, ClassDesc> unboxTypes = Map.of(
            DS_Boolean, CD_boolean,
            DS_Byte, CD_byte,
            DS_Character, CD_char,
            DS_Short, CD_short,
            DS_Integer, CD_int,
            DS_Long, CD_long,
            DS_Float, CD_float,
            DS_Double, CD_double,
            DS_Void, CD_void);

    // https://docs.oracle.com/javase/specs/jls/se17/html/jls-5.html#jls-5.1.2
    private static final Map<String, Set<String>> primitiveWideningConversions = Map.of(
            DS_boolean, Set.of(),
            DS_byte, Set.of(DS_short, DS_int, DS_long, DS_float, DS_double),
            DS_char, Set.of(DS_int, DS_long, DS_float, DS_double),
            DS_short, Set.of(DS_int, DS_long, DS_float, DS_double),
            DS_int, Set.of(DS_long, DS_float, DS_double),
            DS_long, Set.of(DS_float, DS_double),
            DS_float, Set.of(DS_double),
            DS_double, Set.of(),
            DS_void, Set.of());

    /**
     * {@return whether the given {@code type} is primitive}
     */
    static boolean isPrimitive(ClassDesc type) {
        return boxTypes.containsKey(type.descriptorString()); // could also be just `type.isPrimitive()`
    }

    /**
     * {@return whether the given {@code type} is a primitive wrapper class}
     */
    static boolean isPrimitiveWrapper(ClassDesc type) {
        return unboxTypes.containsKey(type.descriptorString());
    }

    /**
     * {@return the result of a boxing conversion of given {@code type}}
     * Returns an empty {@code Optional} if no such conversion exists.
     */
    static Optional<ClassDesc> boxingConversion(ClassDesc type) {
        return Optional.ofNullable(boxTypes.get(type.descriptorString()));
    }

    /**
     * {@return the result of an unboxing conversion of given {@code type}}
     * Returns an empty {@code Optional} if no such conversion exists.
     */
    static Optional<ClassDesc> unboxingConversion(ClassDesc type) {
        return Optional.ofNullable(unboxTypes.get(type.descriptorString()));
    }

    /**
     * {@return the given {@code expr} converted to given {@code toType}}
     * If no conversion is possible, returns the {@code expr} unchanged after verifying
     * the type of {@code expr} is of the same loadable type kind as {@code toType}.
     * <p>
     * The conversions applied are:
     * <ul>
     * <li>identity conversion</li>
     * <li>boxing conversion (Boxing conversion is supposed to exist from any primitive
     * type to the corresponding wrapper class and all its superclasses and superinterfaces,
     * as existing in Java 17.)</li>
     * <li>unboxing conversion (Unboxing conversion is supposed to exist from any primitive
     * wrapper class to the corresponding primitive type.)</li>
     * <li>primitive widening conversion</li>
     * <li>unboxing conversion followed by primitive widening conversion</li>
     * <li>primitive widening conversion followed by boxing conversion</li>
     * <li>reference narrowing conversion (Reference narrowing conversion is supposed to exist
     * only from {@code java.lang.Object} to any reference type.)</li>
     * <li>reference narrowing conversion followed by unboxing conversion</li>
     * </ul>
     */
    static Item convert(Expr expr, ClassDesc toType) {
        Item item = (Item) expr;
        ClassDesc fromType = item.type();
        String toDesc = toType.descriptorString();
        String fromDesc = fromType.descriptorString();
        if (fromDesc.equals(toDesc)) {
            // identity
            return item;
        } else if (toType.equals(boxTypes.get(fromDesc))) {
            // boxing
            return new Box(item);
        } else if (fromType.isPrimitive() && additionalBoxTypes.get(fromDesc).contains(toDesc)) {
            // boxing to a superclass/superinterface of the wrapper class
            return new Box(item, toType);
        } else if (fromType.isPrimitive() && unboxTypes.containsKey(toDesc)) {
            // primitive widening + boxing
            ClassDesc widerType = unboxTypes.get(toDesc);
            if (primitiveWideningConversions.get(fromDesc).contains(widerType.descriptorString())) {
                return new Box(new PrimitiveCast(item, widerType));
            }
        } else if (toType.equals(unboxTypes.get(fromDesc))) {
            // unboxing
            return new Unbox(item);
        } else if (toType.isPrimitive() && unboxTypes.containsKey(fromDesc)
                && primitiveWideningConversions.get(unboxTypes.get(fromDesc).descriptorString()).contains(toDesc)) {
            // unboxing + primitive widening
            return new PrimitiveCast(new Unbox(item), toType);
        } else if (fromType.isPrimitive() && toType.isPrimitive()
                && primitiveWideningConversions.get(fromDesc).contains(toDesc)) {
            // primitive widening
            return new PrimitiveCast(item, toType);
        } else if (DS_Object.equals(fromDesc)) {
            if (toType.isPrimitive()) {
                return new Unbox(new CheckCast(item, boxTypes.get(toDesc), null));
            } else {
                return new CheckCast(item, toType, null);
            }
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
            a = unboxTypes.getOrDefault(a.descriptorString(), a);
        }
        if (b.isClassOrInterface()) {
            b = unboxTypes.getOrDefault(b.descriptorString(), b);
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

    /**
     * {@return whether a logical operation of given {@code kind} with given argument types {@code a} and {@code b}
     * requires numeric promotion}
     *
     * @param kind the kind of the logical operation (must not be {@code null})
     * @param a the type of the first argument (must not be {@code null})
     * @param b the type of the second argument (must not be {@code null})
     */
    static boolean numericPromotionRequired(If.Kind kind, ClassDesc a, ClassDesc b) {
        if (kind != If.Kind.EQ && kind != If.Kind.NE) {
            // non-equality operations (<, <=, >, >=) all require numeric promotion
            return true;
        }

        // equality operations only require promotion if performed on numeric types
        // that is, all primitive types except `boolean` (and `void`, but that never occurs here)
        return a.isPrimitive() && !DS_boolean.equals(a.descriptorString())
                || b.isPrimitive() && !DS_boolean.equals(b.descriptorString());
    }
}
