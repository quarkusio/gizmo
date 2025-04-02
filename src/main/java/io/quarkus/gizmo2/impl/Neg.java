package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class Neg extends Item {
    private final Item a;

    Neg(final Expr a) {
        this.a = (Item) a;
        TypeKind typeKind = a.typeKind();
        if (typeKind == TypeKind.REFERENCE || typeKind == TypeKind.BOOLEAN || typeKind == TypeKind.VOID) {
            throw new IllegalArgumentException("Cannot negate non-numeric expression: " + a);
        }
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(node.prev(), op);
    }

    public ClassDesc type() {
        return a.type();
    }

    public boolean bound() {
        return a.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        switch (typeKind().asLoadable()) {
            case INT -> cb.ineg();
            case LONG -> cb.lneg();
            case FLOAT -> cb.fneg();
            case DOUBLE -> cb.dneg();
            default -> throw impossibleSwitchCase(typeKind().asLoadable());
        }
    }
}
