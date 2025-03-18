package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.BlockCreatorImpl.cleanStack;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

final class Return extends Item {
    static final Return RETURN_VOID = new Return(ConstantImpl.ofVoid());

    private final Item val;

    Return(final Expr val) {
        this.val = (Item) val;
    }

    protected Node insert(final Node node) {
        Node res = super.insert(node);
        cleanStack(node);
        return res;
    }

    public boolean mayFallThrough() {
        return false;
    }

    public boolean mayReturn() {
        return true;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        if (val != null && val.typeKind() != TypeKind.VOID) {
            return val.process(node.prev(), op);
        } else {
            return node.prev();
        }
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (val != null) {
            cb.return_(TypeKind.from(val.type()));
        } else {
            cb.return_();
        }
    }
}
