package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.TypeKind;

final class ArrayStore extends Item {
    private final Item arrayExpr;
    private final Item index;
    private final Item value;
    private final ClassDesc componentType;

    ArrayStore(final Item arrayExpr, final Item index, final Item value, final ClassDesc componentType) {
        this.arrayExpr = arrayExpr;
        this.index = convert(index, CD_int);
        this.value = convert(value, componentType);
        this.componentType = componentType;
    }

    Item arrayExpr() {
        return arrayExpr;
    }

    Item index() {
        return index;
    }

    Item value() {
        return value;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
        index.process(itr, op);
        arrayExpr.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.arrayStore(TypeKind.from(componentType));
        smb.pop(); // array
        smb.pop(); // index
        smb.pop(); // value
        smb.wroteCode();
    }
}
