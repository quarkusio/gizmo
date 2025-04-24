package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.github.dmlloyd.classfile.TypeKind;
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

    Invoke(final Opcode opcode, final MethodDesc desc, Expr instance, List<? extends Expr> args) {
        this(desc.owner(), desc.name(), desc.type(), opcode, desc instanceof InterfaceMethodDesc, (Item) instance,
                Util.reinterpretCast(args));
    }

    Invoke(final ConstructorDesc desc, Expr instance, List<? extends Expr> args) {
        this(desc.owner(), "<init>", desc.type(), Opcode.INVOKESPECIAL, false, (Item) instance, Util.reinterpretCast(args));
    }

    private Invoke(final ClassDesc owner, final String name, final MethodTypeDesc type, final Opcode opcode,
            final boolean isInterface, Item instance, final List<Item> args) {
        if (type.parameterCount() != args.size()) {
            String paramsStr = type.parameterCount() == 1 ? "1 parameter" : type.parameterCount() + " parameters";
            String argsStr = args.size() == 1 ? "1 argument was" : args.size() + " arguments were";
            throw new IllegalArgumentException("Method " + owner.displayName() + "." + name + "() takes "
                    + paramsStr + ", but " + argsStr + " passed");
        }
        for (int i = 0; i < type.parameterCount(); i++) {
            ClassDesc parameterType = type.parameterType(i);
            ClassDesc argumentType = args.get(i).type();
            if (TypeKind.from(parameterType).asLoadable() != TypeKind.from(argumentType).asLoadable()) {
                throw new IllegalArgumentException("Parameter " + i + " of method " + owner.displayName() + "." + name
                        + "() is of type '" + parameterType.displayName() + "', but given argument is '"
                        + argumentType.displayName() + "'");
            }
        }
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.opcode = opcode;
        this.isInterface = isInterface;
        this.instance = instance;
        this.args = args;
    }

    public ClassDesc type() {
        return type.returnType();
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        node = node.prev();
        int size = args.size();
        for (int i = size - 1; i >= 0; i--) {
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
