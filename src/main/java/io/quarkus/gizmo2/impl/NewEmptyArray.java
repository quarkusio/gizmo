package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.convert;
import static java.lang.constant.ConstantDescs.CD_int;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.IntConst;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.TypeKind;

final class NewEmptyArray extends Item {
    private final Item size;

    NewEmptyArray(final ClassDesc componentType, final Item size) {
        super(componentType.arrayType());
        this.size = convert(size, CD_int);
    }

    @Override
    public String itemName() {
        return "NewEmptyArray:" + type().displayName();
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        size.process(itr, op);
    }

    public Expr length() {
        return size instanceof IntConst ? size : super.length();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        ClassDesc componentType = type().componentType();
        TypeKind typeKind = TypeKind.from(componentType);
        if (typeKind == TypeKind.REFERENCE) {
            cb.anewarray(componentType);
        } else {
            cb.newarray(typeKind);
        }
        smb.pop(); // size
        smb.push(type());
        smb.wroteCode();
    }
}
