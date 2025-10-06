package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.convert;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
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

    Invoke(final Opcode opcode, final MethodDesc desc, Expr instance, List<? extends Expr> args,
            final GenericType genericType) {
        this(desc.owner(), desc.name(), desc.type(), opcode, desc instanceof InterfaceMethodDesc, (Item) instance,
                Util.reinterpretCast(args), genericType);
    }

    Invoke(final ConstructorDesc desc, Expr instance, List<? extends Expr> args, final GenericType genericType) {
        this(desc.owner(), "<init>", desc.type(), Opcode.INVOKESPECIAL, false, (Item) instance, Util.reinterpretCast(args),
                genericType);
    }

    private Invoke(final ClassDesc owner, final String name, final MethodTypeDesc type, final Opcode opcode,
            final boolean isInterface, Item instance, final List<Item> args, final GenericType genericType) {
        super(type.returnType(), genericType);
        if (type.parameterCount() != args.size()) {
            String paramsStr = type.parameterCount() == 1 ? "1 parameter" : type.parameterCount() + " parameters";
            String argsStr = args.size() == 1 ? "1 argument was" : args.size() + " arguments were";
            throw new IllegalArgumentException("Method " + owner.displayName() + "." + name + "() takes "
                    + paramsStr + ", but " + argsStr + " passed");
        }
        if (instance != null) {
            instance = convert(instance, owner);
        }
        List<Item> newArgs = new ArrayList<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            try {
                newArgs.add(convert(args.get(i), type.parameterType(i)));
            } catch (IllegalArgumentException e) {
                // slightly better error message
                throw new IllegalArgumentException("Parameter " + i + " of method " + owner.displayName() + "." + name
                        + "() is of type '" + type.parameterType(i).displayName() + "', but given argument is '"
                        + args.get(i).type().displayName() + "'");
            }
        }
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.opcode = opcode;
        this.isInterface = isInterface;
        this.instance = instance;
        this.args = newArgs;
    }

    @Override
    public String itemName() {
        return "Invoke:" + owner.displayName() + "." + name;
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
