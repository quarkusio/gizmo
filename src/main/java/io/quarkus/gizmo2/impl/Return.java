package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.TypeKind;

final class Return extends Item {
    static final Return RETURN_VOID = new Return(ConstImpl.ofVoid());

    private final Item returnValue;

    Return(final Expr returnValue) {
        this.returnValue = (Item) returnValue;
    }

    public boolean mayFallThrough() {
        return false;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        returnValue.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl from, final StackMapBuilder smb) {
        TryFinally tryFinally = from.tryFinally();
        ClassDesc returnType = from.returnType();
        if (tryFinally != null) {
            cb.goto_(tryFinally.cleanup(new ReturnKey(returnType, smb.save())));
            smb.wroteCode();
        } else {
            cb.return_(TypeKind.from(returnType));
            smb.wroteCode();
        }
        if (!Util.isVoid(returnType)) {
            smb.pop();
        }
    }

    static final class ReturnKey extends TryFinally.CleanupKey {
        private final ClassDesc returnType;

        ReturnKey(final ClassDesc returnType, final StackMapBuilder.Saved saved) {
            super(saved);
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
            return this == other || other != null && Util.equals(returnType, other.returnType);
        }

        public int hashCode() {
            return Objects.hash(ReturnKey.class, returnType);
        }
    }
}
