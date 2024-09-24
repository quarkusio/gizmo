package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;
import io.quarkus.gizmo2.impl.constant.IntConstant;

final class NewArray extends ExprImpl {
    private final ClassDesc arrayType;
    private final List<ExprImpl> values;
    private final List<ArrayStore> stores;
    private final List<Dup> dups;
    private final NewEmptyArray nea;

    NewArray(final ClassDesc elemType, final List<Expr> storeVals) {
        arrayType = elemType.arrayType();
        int size = storeVals.size();
        List<ExprImpl> values = new ArrayList<>(size);
        List<ArrayStore> stores = new ArrayList<>(size);
        List<Dup> dups = new ArrayList<>(size);
        NewEmptyArray nea = new NewEmptyArray(elemType, ConstantImpl.of(size));
        ExprImpl dupIn = nea;
        for (int i = 0; i < size; i++) {
            final ExprImpl value = (ExprImpl) storeVals.get(i);
            values.add(value);
            Dup dup = new Dup(dupIn);
            dups.add(dup);
            stores.add(new ArrayStore(dup, ConstantImpl.of(i), value));
            dupIn = dup;
        }
        this.values = values;
        this.stores = stores;
        this.dups = dups;
        this.nea = nea;
    }

    protected void insert(final BlockCreatorImpl block, final ListIterator<Item> iter) {
        // we have our own way of doing this
        iter.add(this);
        iter.previous();
        int size = values.size();
        for (int i = size - 1; i >= 0; i --) {
            iter.add(stores.get(i));
            iter.previous();
            values.get(i).process(block, iter, false);
            iter.add(ConstantImpl.of(i));
            iter.previous();
            // add but do not process the dep
            iter.add(dups.get(i));
            iter.previous();
        }
        nea.insert(block, iter);
        // we're now positioned before this instruction
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        int size = values.size();
        for (int i = size - 1; i >= 0; i --) {
            stores.get(i).process(block, iter, verifyOnly);
        }
        nea.process(block, iter, verifyOnly);
    }

    public ClassDesc type() {
        return arrayType;
    }

    public boolean bound() {
        return true;
    }

    public IntConstant length() {
        return ConstantImpl.of(values.size());
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // we put everything into dependency nodes
    }
}