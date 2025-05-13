package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

public class NewArrayResult extends Item {
    private final Item newEmptyArray;
    private final List<Item> elements;

    NewArrayResult(Item newEmptyArray, List<Item> elements) {
        this.newEmptyArray = newEmptyArray;
        this.elements = elements;
    }

    @Override
    public String itemName() {
        return "NewArrayResult:" + newEmptyArray.type().displayName();
    }

    @Override
    public ClassDesc type() {
        return newEmptyArray.type();
    }

    @Override
    protected Node forEachDependency(Node node, BiFunction<Item, Node, Node> op) {
        node = node.prev();
        int size = elements.size();
        for (int i = size - 1; i >= 0; i--) {
            // processes the `Dup`, index, element and `ArrayStore`
            node = elements.get(i).process(node, op);
        }
        node = newEmptyArray.process(node, op);
        return node;
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block) {
        // nothing
    }
}
