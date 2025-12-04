package io.quarkus.gizmo2.impl;

import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

public class NewArrayResult extends Item {
    private final NewEmptyArray newEmptyArray;
    private final List<ArrayStore> elements;

    NewArrayResult(NewEmptyArray newEmptyArray, List<ArrayStore> elements) {
        super(newEmptyArray.type());
        this.newEmptyArray = newEmptyArray;
        this.elements = elements;
    }

    @Override
    public String itemName() {
        return "NewArrayResult:" + newEmptyArray.type().displayName();
    }

    @Override
    protected void forEachDependency(ListIterator<Item> itr, BiConsumer<Item, ListIterator<Item>> op) {
        int size = elements.size();
        for (int i = size - 1; i >= 0; i--) {
            // processes the `Dup`, index, element and `ArrayStore`
            elements.get(i).process(itr, op);
        }
        newEmptyArray.process(itr, op);
    }

    public void pop(ListIterator<Item> itr) {
        remove(itr);
        int size = elements.size();
        for (int i = size - 1; i >= 0; i--) {
            // delete the array store and pop the things being stored
            elements.get(i).revoke(itr);
        }
        newEmptyArray.pop(itr);
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block, final StackMapBuilder smb) {
        // nothing
    }
}
