package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.Util;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

/**
 * An expression which wraps a {@link ConstantDesc}.
 */
public sealed interface Const extends Expr, Constable permits ConstImpl {
    /**
     * {@return the description of the constant}
     */
    ConstantDesc desc();

    /**
     * {@return a constant for the given Constable}
     *
     * @param constable the object to create a constant from (must not be {@code null})
     */
    static Const of(Constable constable) {
        return ConstImpl.of(constable);
    }

    /**
     * {@return a constant for the given description}
     *
     * @param constantDesc the object to create a constant from (must not be {@code null})
     */
    static Const of(ConstantDesc constantDesc) {
        return ConstImpl.of(constantDesc);
    }

    /**
     * {@return a constant for the given description}
     *
     * @param dcd the object to create a constant from (must not be {@code null})
     */
    static Const of(DynamicConstantDesc<?> dcd) {
        return ConstImpl.of(dcd);
    }

    /**
     * {@return a {@code null} constant with the given type}
     *
     * @param type the type of the null constant (used for inference of type) (must not be {@code null})
     */
    static Const ofNull(ClassDesc type) {
        return ConstImpl.ofNull(type);
    }

    /**
     * {@return a {@code null} constant with the given type}
     *
     * @param type the type of the null constant (used for inference of type) (must not be {@code null})
     */
    static Const ofNull(Class<?> type) {
        return ConstImpl.ofNull(type);
    }

    /**
     * {@return a constant with the default value of given {@code type}}
     * This is zero for primitive types and {@code null} for reference types.
     *
     * @param type the type of the constant (must not be {@code null})
     */
    static Const ofDefault(ClassDesc type) {
        return switch (type.descriptorString()) {
            case "B" -> of((byte) 0);
            case "S" -> of((short) 0);
            case "C" -> of('\0');
            case "I" -> of(0);
            case "J" -> of(0L);
            case "F" -> of(0.0F);
            case "D" -> of(0.0);
            case "Z" -> of(false);
            case "V" -> ofVoid();
            default -> ofNull(type);
        };
    }

    /**
     * {@return a constant with the default value of given {@code type}}
     * This is zero for primitive types and {@code null} for reference types.
     *
     * @param type the type of the constant (must not be {@code null})
     */
    static Const ofDefault(Class<?> type) {
        return ofDefault(Util.classDesc(type));
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(ClassDesc value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Class<?> value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(VarHandle.VarHandleDesc value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Enum.EnumDesc<?> value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Byte value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(byte value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Short value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(short value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Character value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(char value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Integer value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(int value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Long value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(long value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Float value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(float value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Double value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(double value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(Boolean value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(boolean value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(int value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(long value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(float value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(double value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(String value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return the {@code void} constant}
     */
    static Const ofVoid() {
        return ConstImpl.ofVoid();
    }

    /**
     * {@return a {@code VarHandle} constant for the given instance field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Const ofFieldVarHandle(FieldDesc desc) {
        return ConstImpl.ofFieldVarHandle(desc);
    }

    /**
     * {@return a {@code VarHandle} constant for the given static field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Const ofStaticFieldVarHandle(FieldDesc desc) {
        return ConstImpl.ofStaticFieldVarHandle(desc);
    }

    /**
     * {@return a constant with the value of the given {@code static final} field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Const ofStaticFinalField(FieldDesc desc) {
        return ConstImpl.ofStaticFinalField(desc);
    }

    /**
     * {@return a {@code VarHandle} constant for the given array type}
     *
     * @param arrayType the array type (must not be {@code null})
     */
    static Const ofArrayVarHandle(ClassDesc arrayType) {
        return ConstImpl.ofArrayVarHandle(arrayType);
    }

    /**
     * {@return a constant whose value is the result of a one-time method invocation}
     * Note that due to the nature of dynamic constants,
     * parallel threads may cause the method to be invoked more than once.
     *
     * @param methodHandle the method handle to invoke (must not be {@code null})
     * @param args the list of arguments (must not be {@code null})
     */
    static Const ofInvoke(Const methodHandle, List<Const> args) {
        return ConstImpl.ofInvoke(methodHandle, args);
    }

    /**
     * {@return a constant whose value is the result of a one-time method invocation}
     * Note that due to the nature of dynamic constants,
     * parallel threads may cause the method to be invoked more than once.
     *
     * @param methodHandle the method handle to invoke (must not be {@code null})
     * @param args the list of arguments (must not be {@code null})
     */
    static Const ofInvoke(Const methodHandle, Const... args) {
        return ConstImpl.ofInvoke(methodHandle, List.of(args));
    }

    /**
     * {@return a method handle constant from the given descriptor}
     *
     * @param desc the method handle descriptor (must not be {@code null})
     */
    static Const of(MethodHandleDesc desc) {
        return ConstImpl.of(desc);
    }

    /**
     * {@return a method handle constant from the given information}
     *
     * @param kind the invocation kind (must not be {@code null})
     * @param desc the method's descriptor (must not be {@code null})
     */
    static Const ofMethodHandle(InvokeKind kind, MethodDesc desc) {
        return ConstImpl.ofMethodHandle(kind, desc);
    }

    /**
     * {@return a method handle constant from the given constructor descriptor}
     *
     * @param desc the constructor descriptor (must not be {@code null})
     */
    static Const ofConstructorMethodHandle(ConstructorDesc desc) {
        return ConstImpl.ofConstructorMethodHandle(desc);
    }

    /**
     * {@return a method handle for an instance field setter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofFieldSetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofFieldSetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for an instance field getter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofFieldGetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofFieldGetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for a static field setter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofStaticFieldSetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofStaticFieldSetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for a static field getter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofStaticFieldGetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofStaticFieldGetterMethodHandle(desc);
    }

    /**
     * {@return a method type constant from the given descriptor}
     *
     * @param desc the method type descriptor (must not be {@code null})
     */
    static Const of(MethodTypeDesc desc) {
        return ConstImpl.of(desc);
    }

    /**
     * {@return a list constant containing the given items}
     * The maximum number of items is around 254 (depending on the constant type); however,
     * this method should only be used for relatively short lists to avoid overfilling the constant pool.
     * Note that the JDK immutable collection types forbid {@code null}, so
     * {@linkplain Const#ofNull(ClassDesc) <code>null</code> values} should not be used.
     *
     * @param items the list of items, which must be {@code Const}, {@code Constable}, or {@code ConstantDesc} subtypes
     *        (must not be {@code null})
     */
    static Const of(List<?> items) {
        items = List.copyOf(items);
        if (items.size() > 254) {
            throw new IllegalArgumentException(
                    "List is too big (%d elements, max is 254)".formatted(Integer.valueOf(items.size())));
        }
        ClassDesc[] paramDescs = items.size() <= 10 ? Collections.nCopies(items.size(), CD_Object).toArray(ClassDesc[]::new)
                : new ClassDesc[] { CD_Object.arrayType() };
        return ofInvoke(
                ofMethodHandle(InvokeKind.STATIC, InterfaceMethodDesc.of(
                        CD_List,
                        "of",
                        MethodTypeDesc.of(CD_List, paramDescs))),
                consts(items));
    }

    /**
     * {@return a set constant containing the given items}
     * The maximum number of items is around 254 (depending on the constant type); however,
     * this method should only be used for relatively small sets to avoid overfilling the constant pool.
     * Note that the JDK immutable collection types forbid {@code null}, so
     * {@linkplain Const#ofNull(ClassDesc) <code>null</code> values} should not be used.
     *
     * @param items the set of items, which must be {@code Const}, {@code Constable}, or {@code ConstantDesc} subtypes
     *        (must not be {@code null})
     */
    static Const of(Set<?> items) {
        items = Set.copyOf(items);
        if (items.size() > 254) {
            throw new IllegalArgumentException(
                    "Set is too big (%d elements, max is 254)".formatted(Integer.valueOf(items.size())));
        }
        ClassDesc[] paramDescs = items.size() <= 10 ? Collections.nCopies(items.size(), CD_Object).toArray(ClassDesc[]::new)
                : new ClassDesc[] { CD_Object.arrayType() };
        return ofInvoke(
                ofMethodHandle(InvokeKind.STATIC, InterfaceMethodDesc.of(
                        CD_Set,
                        "of",
                        MethodTypeDesc.of(CD_Set, paramDescs))),
                consts(items));
    }

    /**
     * {@return a map constant containing the given items}
     * The maximum number of items is around 254 (depending on the constant type); however,
     * this method should only be used for relatively small maps to avoid overfilling the constant pool.
     * Note that the JDK immutable collection types forbid {@code null}, so
     * {@linkplain Const#ofNull(ClassDesc) <code>null</code> values} should not be used.
     *
     * @param items the set of items, which must be {@code Const}, {@code Constable}, or {@code ConstantDesc} subtypes
     *        (must not be {@code null})
     */
    static Const of(Map<?, ?> items) {
        items = Map.copyOf(items);
        if (items.size() > 254) {
            throw new IllegalArgumentException(
                    "Map is too big (%d elements, max is 254)".formatted(Integer.valueOf(items.size())));
        }
        if (items.size() <= 10) {
            // use the simple factory
            ClassDesc[] paramDescs = Collections.nCopies(items.size() * 2, CD_Object).toArray(ClassDesc[]::new);
            Const[] args = items.entrySet().stream().flatMap(e -> Stream.of(e.getKey(), e.getValue())).map(Const::of)
                    .toArray(Const[]::new);
            return ofInvoke(
                    ofMethodHandle(InvokeKind.STATIC, InterfaceMethodDesc.of(
                            CD_Map,
                            "of",
                            MethodTypeDesc.of(CD_Map, paramDescs))),
                    args);
        } else {
            // create map entry constants
            Const[] args = items.entrySet().stream().map(Const::of).toArray(Const[]::new);
            return ofInvoke(
                    ofMethodHandle(InvokeKind.STATIC, InterfaceMethodDesc.of(
                            CD_Map,
                            "ofEntries",
                            MethodTypeDesc.of(CD_Map, Util.classDesc(Map.Entry.class).arrayType()))),
                    args);
        }
    }

    /**
     * {@return a map entry constant with the key and value of the given entry}
     *
     * @param entry the map entry containing a constant key and value,
     *        which must be {@code Const}, {@code Constable}, or {@code ConstantDesc} subtypes
     *        (must not be {@code null})
     */
    static Const of(Map.Entry<?, ?> entry) {
        return ofInvoke(
                ofMethodHandle(InvokeKind.STATIC, InterfaceMethodDesc.of(
                        CD_Map,
                        "entry",
                        MethodTypeDesc.of(Util.classDesc(Map.Entry.class), CD_Object, CD_Object))),
                of(entry.getKey()),
                of(entry.getValue()));
    }

    // Private to avoid ambiguous overload behavior
    private static Const of(Object any) {
        if (any instanceof ConstantDesc cd) {
            return of(cd);
        } else if (any instanceof Constable c) {
            return of(c);
        } else {
            throw wrongType(any);
        }
    }

    private static Const[] consts(final Collection<?> items) {
        return items.stream().map(Const::of).toArray(Const[]::new);
    }

    private static IllegalArgumentException wrongType(Object object) {
        return new IllegalArgumentException("Given object %s is not a valid constant".formatted(object));
    }
}
