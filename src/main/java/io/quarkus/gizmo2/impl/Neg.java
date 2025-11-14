package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.convert;
import static io.quarkus.gizmo2.impl.Conversions.unboxingConversion;
import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;

final class Neg extends Item {
    private final Item a;

    Neg(Expr a) {
        a = convert(a, unboxingConversion(a.type()).orElse(a.type()));
        this.a = (Item) a;
        initType(a.type());
        TypeKind typeKind = a.typeKind();
        if (typeKind == TypeKind.REFERENCE || typeKind == TypeKind.BOOLEAN || typeKind == TypeKind.VOID) {
            throw new IllegalArgumentException("Cannot negate non-numeric expression: " + a);
        }
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        a.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        switch (typeKind().asLoadable()) {
            case INT -> cb.ineg();
            case LONG -> cb.lneg();
            case FLOAT -> cb.fneg();
            case DOUBLE -> cb.dneg();
            default -> throw impossibleSwitchCase(typeKind().asLoadable());
        }
        smb.wroteCode();
    }
}
