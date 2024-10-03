package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.BlockCreatorImpl.cleanStack;

import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class Return extends Item {
    private final ExprImpl val;

    Return() {
        this(null);
    }

    Return(final Expr val) {
        this.val = (ExprImpl) val;
    }

    protected void insert(final ListIterator<Item> iter) {
        super.insert(iter);
        cleanStack(iter);
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        if (val != null && val.typeKind() != TypeKind.VOID) {
            val.process(iter, op);
        }
    }

    public boolean exitsAll() {
        return true;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        block.exitAll(cb);
        if (val != null) {
            cb.return_(TypeKind.from(val.type()));
        } else {
            cb.return_();
        }
    }
}
