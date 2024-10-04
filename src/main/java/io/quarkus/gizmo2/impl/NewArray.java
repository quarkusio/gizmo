package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;
import io.quarkus.gizmo2.impl.constant.IntConstant;

final class NewArray extends Item {
    private final ClassDesc arrayType;
    private final List<Item> values;
    private final List<ArrayStore> stores;
    private final List<Dup> dups;
    private final NewEmptyArray nea;

    NewArray(final ClassDesc elemType, final List<Expr> storeVals) {
        arrayType = elemType.arrayType();
        int size = storeVals.size();
        List<Item> values = new ArrayList<>(size);
        List<ArrayStore> stores = new ArrayList<>(size);
        List<Dup> dups = new ArrayList<>(size);
        NewEmptyArray nea = new NewEmptyArray(elemType, ConstantImpl.of(size));
        Item dupIn = nea;
        for (int i = 0; i < size; i++) {
            final Item value = (Item) storeVals.get(i);
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

    protected void insert(final ListIterator<Item> iter) {
        // we have our own way of doing this
        // first insert our node and move backwards
        super.insert(iter);
        // now insert all of the stuff we need in order to function correctly
        int size = values.size();
        for (int i = size - 1; i >= 0; i --) {
            // we need to insert the following sequence:
            //   DUP
            //   LDC index
            //   <push value>
            //   AASTORE
            // ...except in the reverse order (because we're walking up the list, not down it).
            // So, first the store, which expects `<array> <index> <value>`:
            stores.get(i).insert(iter);
            // Then the value (already in the list, just skip over them):
            values.get(i).process(iter, Op.VERIFY);
            // Then the index:
            ConstantImpl.of(i).insert(iter);
            // And last, the dup of the original array:
            dups.get(i).insert(iter);
        }
        // Finally, the array allocation itself:
        nea.insert(iter);
        // we're now positioned before this instruction
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        int size = values.size();
        for (int i = size - 1; i >= 0; i --) {
            stores.get(i).process(iter, op);
        }
        nea.process(iter, op);
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