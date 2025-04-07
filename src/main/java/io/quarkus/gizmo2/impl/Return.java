package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Objects;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

final class Return extends Item {
    static final Return RETURN_VOID = new Return(ConstantImpl.ofVoid());

    private final Item returnValue;

    Return(final Expr returnValue) {
        this.returnValue = (Item) returnValue;
    }

    public boolean mayFallThrough() {
        return false;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return returnValue.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        ClassDesc returnType = from.returnType();
        if (tryFinally != null) {
            cb.goto_(tryFinally.cleanup(new ReturnKey(returnType)));
        } else {
            cb.return_(TypeKind.from(returnType));
        }
    }

    static final class ReturnKey extends TryFinally.CleanupKey {
        private final ClassDesc returnType;

        ReturnKey(final ClassDesc returnType) {
            this.returnType = returnType;
        }

        ClassDesc type() {
            return returnType;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.return_(input);
        }

        public boolean equals(final Object obj) {
            return obj instanceof ReturnKey rk && equals(rk);
        }

        public boolean equals(final ReturnKey other) {
            return this == other || other != null && returnType.equals(other.returnType);
        }

        public int hashCode() {
            return Objects.hash(ReturnKey.class, returnType);
        }
    }
}
