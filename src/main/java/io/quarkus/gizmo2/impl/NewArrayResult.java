package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

public class NewArrayResult extends Item {
    private final NewEmptyArray newEmptyArray;
    private final List<ArrayStore> elements;

    NewArrayResult(NewEmptyArray newEmptyArray, List<ArrayStore> elements) {
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

    public Node pop(Node ourNode) {
        Node node = ourNode.prev();
        remove(ourNode);
        int size = elements.size();
        for (int i = size - 1; i >= 0; i--) {
            // delete the array store and pop the things being stored
            node = elements.get(i).revoke(node);
        }
        node = newEmptyArray.pop(node);
        return node;
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block) {
        // nothing
    }
}
