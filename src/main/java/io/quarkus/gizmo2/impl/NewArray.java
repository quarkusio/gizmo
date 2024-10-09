package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

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

    protected Node insert(Node node) {
        // we have our own way of doing this
        // first insert our node and move backwards
        node = super.insert(node).prev();
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
            node = stores.get(i).insert(node);
            // Then the value (already in the list, just skip over them):
            node = values.get(i).insertIfUnbound(node);
            // Then the index:
            node = ConstantImpl.of(i).insert(node);
            // And last, the dup of the original array:
            node = dups.get(i).insert(node);
        }
        // Finally, the array allocation itself:
        node = nea.insert(node);
        // and our length
        return ConstantImpl.of(size).insert(node);
        // we're now positioned before this instruction
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        int size = values.size();
        node = node.prev();
        for (int i = size - 1; i >= 0; i --) {
            node = stores.get(i).process(node, op);
            node = values.get(i).process(node, op);
            node = ConstantImpl.of(i).process(node, op);
            node = dups.get(i).process(node, op);
        }
        return nea.process(node, op);
    }

    public ClassDesc type() {
        return arrayType;
    }

    public IntConstant length() {
        return ConstantImpl.of(values.size());
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // we put everything into dependency nodes
    }
}