package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

import java.lang.constant.ClassDesc;

public class Preconditions {
    private Preconditions() {
    }

    public static void requireSameType(final Expr a, final Expr b) {
        if (!a.type().equals(b.type())) {
            throw new IllegalArgumentException("Type mismatch between " + a.type().displayName() + " and " + b.type().displayName());
        }
    }

    public static void requireSameTypeKind(final Expr a, final Expr b) {
        if (a.typeKind() != b.typeKind()) {
            throw new IllegalArgumentException("Type mismatch between " + a.type().displayName() + " and " + b.type().displayName());
        }
    }

    public static void requireSameTypeKind(final ClassDesc a, final ClassDesc b) {
        if (TypeKind.from(a) != TypeKind.from(b)) {
            throw new IllegalArgumentException("Type mismatch between " + a.displayName() + " and " + b.displayName());
        }
    }

    public static void requireSameLoadableTypeKind(final Expr a, final Expr b) {
        if (a.typeKind().asLoadable() != b.typeKind().asLoadable()) {
            throw new IllegalArgumentException("Type mismatch between " + a.type().displayName() + " and " + b.type().displayName());
        }
    }

    public static void requireSameLoadableTypeKind(final ClassDesc a, final ClassDesc b) {
        if (TypeKind.from(a).asLoadable() != TypeKind.from(b).asLoadable()) {
            throw new IllegalArgumentException("Type mismatch between " + a.displayName() + " and " + b.displayName());
        }
    }

    public static void requireArray(Expr expr) {
        if (!expr.type().isArray()) {
            throw new IllegalArgumentException("Array expected: " + expr.type().displayName());
        }
    }
}
