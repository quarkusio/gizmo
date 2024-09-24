package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.quarkus.gizmo2.ConstructorDesc;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.InterfaceMethodDesc;
import io.quarkus.gizmo2.MethodDesc;

final class Invoke extends ExprImpl {
    private final ClassDesc owner;
    private final String name;
    private final MethodTypeDesc type;
    private final List<ExprImpl> args;
    private final Opcode opcode;
    private final boolean isInterface;
    private final boolean construct;

    Invoke(final Opcode opcode, final MethodDesc desc, Expr instance, List<Expr> args) {
        this(desc.owner(), desc.name(), desc.type(), opcode, desc instanceof InterfaceMethodDesc, false, Stream.concat(Stream.ofNullable(instance), args.stream()).map(ExprImpl.class::cast).toList());
    }

    Invoke(final ConstructorDesc desc, Expr instance, boolean construct, List<Expr> args) {
        this(desc.owner(), "<init>", desc.type(), Opcode.INVOKESPECIAL, false, construct, Stream.concat(Stream.of(instance), args.stream()).map(ExprImpl.class::cast).toList());
    }

    private Invoke(final ClassDesc owner, final String name, final MethodTypeDesc type, final Opcode opcode, final boolean isInterface, final boolean construct, final List<ExprImpl> args) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.opcode = opcode;
        this.isInterface = isInterface;
        this.construct = construct;
        this.args = args;
    }

    public ClassDesc type() {
        return construct ? owner : type.returnType();
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        int size = args.size();
        for (int i = size - 1; i >= 0; i --) {
            args.get(i).process(block, iter, verifyOnly);
        }
    }

    public boolean bound() {
        return true;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (construct) {
            cb.dup();
        }
        cb.invoke(opcode, owner, name, type, isInterface);
    }
}
