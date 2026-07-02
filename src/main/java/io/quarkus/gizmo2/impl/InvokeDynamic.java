package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.smallrye.classfile.CodeBuilder;

final class InvokeDynamic extends Item {
    private final List<? extends Expr> args;
    private final DynamicCallSiteDesc callSiteDesc;

    InvokeDynamic(final List<? extends Expr> args, final DynamicCallSiteDesc callSiteDesc) {
        super(callSiteDesc.invocationType().returnType());
        this.args = args;
        this.callSiteDesc = callSiteDesc;
    }

    /**
     * {@return the dynamic call site descriptor}
     */
    DynamicCallSiteDesc callSiteDesc() {
        return callSiteDesc;
    }

    /**
     * {@return the invocation argument items}
     */
    @SuppressWarnings("unchecked")
    List<Item> invocationArgs() {
        return (List<Item>) (List<?>) args;
    }

    protected void forEachDependency(ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        for (int i = args.size() - 1; i >= 0; i--) {
            ((Item) args.get(i)).process(itr, op);
        }
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokedynamic(callSiteDesc);
        MethodTypeDesc type = callSiteDesc.invocationType();
        for (ClassDesc classDesc : type.parameterList()) {
            smb.pop(); // argument
        }
        if (!Util.isVoid(type.returnType())) {
            smb.push(type.returnType()); // result
        }
        smb.wroteCode();
    }

    /** {@inheritDoc} */
    @Override
    protected StringBuilder appendSourceExpr(StringBuilder buf, SourceBuilder sb) {
        return SourceGenerator.invokeDynamicExpr(buf, this, sb);
    }
}
