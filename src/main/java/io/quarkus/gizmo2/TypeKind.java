package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

import io.smallrye.common.constraint.Assert;

/**
 * The kind of a type, which can be one of the primitive types, a reference type, or {@code void}.
 */
public enum TypeKind {
    BOOLEAN(io.github.dmlloyd.classfile.TypeKind.BOOLEAN),
    BYTE(io.github.dmlloyd.classfile.TypeKind.BYTE),
    CHAR(io.github.dmlloyd.classfile.TypeKind.CHAR),
    SHORT(io.github.dmlloyd.classfile.TypeKind.SHORT),
    INT(io.github.dmlloyd.classfile.TypeKind.INT),
    LONG(io.github.dmlloyd.classfile.TypeKind.LONG),
    FLOAT(io.github.dmlloyd.classfile.TypeKind.FLOAT),
    DOUBLE(io.github.dmlloyd.classfile.TypeKind.DOUBLE),
    REFERENCE(io.github.dmlloyd.classfile.TypeKind.REFERENCE),
    VOID(io.github.dmlloyd.classfile.TypeKind.VOID),
    ;

    private final io.github.dmlloyd.classfile.TypeKind actualKind;

    TypeKind(final io.github.dmlloyd.classfile.TypeKind actualKind) {
        this.actualKind = actualKind;
    }

    /**
     * {@return the most specific class descriptor for this type kind}
     */
    public ClassDesc upperBound() {
        return actualKind.upperBound();
    }

    /**
     * {@return the number of variable or stack slots required by values of this type}
     */
    public int slotSize() {
        return actualKind.slotSize();
    }

    /**
     * {@return the loadable type kind for this type kind}
     */
    public TypeKind asLoadable() {
        return of(actualKind.asLoadable());
    }

    static TypeKind of(io.github.dmlloyd.classfile.TypeKind actualKind) {
        return switch (actualKind) {
            case BOOLEAN -> BOOLEAN;
            case BYTE -> BYTE;
            case CHAR -> CHAR;
            case SHORT -> SHORT;
            case INT -> INT;
            case LONG -> LONG;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
            case REFERENCE -> REFERENCE;
            case VOID -> VOID;
        };
    }

    /**
     * {@return the type kind for the given descriptor string}
     *
     * @param descriptor the descriptor string (must not be {@code null})
     */
    public static TypeKind from(String descriptor) {
        char ch = descriptor.charAt(0);
        return switch (ch) {
            case 'Z' -> TypeKind.BOOLEAN;
            case 'B' -> TypeKind.BYTE;
            case 'C' -> TypeKind.CHAR;
            case 'S' -> TypeKind.SHORT;
            case 'I' -> TypeKind.INT;
            case 'J' -> TypeKind.LONG;
            case 'F' -> TypeKind.FLOAT;
            case 'D' -> TypeKind.DOUBLE;
            case 'V' -> TypeKind.VOID;
            case '[', 'L' -> TypeKind.REFERENCE;
            default -> throw Assert.impossibleSwitchCase(ch);
        };
    }

    /**
     * {@return the type kind for the given descriptor}
     *
     * @param descriptor the descriptor (must not be {@code null})
     */
    public static TypeKind from(ClassDesc descriptor) {
        return from(descriptor.descriptorString());
    }

    /**
     * {@return the type kind for the given class}
     *
     * @param clazz the class (must not be {@code null})
     */
    public static TypeKind from(Class<?> clazz) {
        return clazz.isPrimitive() ? from(clazz.descriptorString()) : REFERENCE;
    }
}
