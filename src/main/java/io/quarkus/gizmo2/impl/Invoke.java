package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

final class Invoke extends Item {
    private final ClassDesc owner;
    private final String name;
    private final MethodTypeDesc type;
    private final Item instance;
    private final List<Item> args;
    private final Opcode opcode;
    private final boolean isInterface;
    private final boolean construct;

    Invoke(final Opcode opcode, final MethodDesc desc, Expr instance, List<Expr> args) {
        this(desc.owner(), desc.name(), desc.type(), opcode, desc instanceof InterfaceMethodDesc, false, (Item) instance, Util.reinterpretCast(args));
    }

    Invoke(final ConstructorDesc desc, Expr instance, boolean construct, List<Expr> args) {
        this(desc.owner(), "<init>", desc.type(), Opcode.INVOKESPECIAL, false, construct, (Item) instance, Util.reinterpretCast(args));
    }

    private Invoke(final ClassDesc owner, final String name, final MethodTypeDesc type, final Opcode opcode, final boolean isInterface, final boolean construct, Item instance, final List<Item> args) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.opcode = opcode;
        this.isInterface = isInterface;
        this.construct = construct;
        this.instance = instance;
        this.args = args;
    }

    public ClassDesc type() {
        return construct ? owner : type.returnType();
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        node = node.prev();
        int size = args.size();
        for (int i = size - 1; i >= 0; i --) {
            node = args.get(i).process(node, op);
        }
        if (instance != null) {
            node = instance.process(node, op);
        }
        return node;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invoke(opcode, owner, name, type, isInterface);
    }
}
