package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

public final class StaticFieldVarImpl extends LValueExprImpl implements StaticFieldVar {
    private final FieldDesc desc;

    StaticFieldVarImpl(FieldDesc desc) {
        this.desc = desc;
    }

    public FieldDesc desc() {
        return desc;
    }

    public ClassDesc type() {
        return desc.type();
    }

    public boolean bound() {
        return false;
    }

    ExprImpl emitGet(final BlockCreatorImpl block, final AccessMode mode) {
        if (mode == AccessMode.AsDeclared) {
            return asBound();
        }
        return new ExprImpl() {
            public ClassDesc type() {
                return StaticFieldVarImpl.this.type();
            }

            protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                ConstantImpl.ofFieldVarHandle(desc).processDependencies(iter, op);
            }

            public boolean bound() {
                return true;
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.invokevirtual(CD_VarHandle, switch (mode) {
                    case Plain -> "get";
                    case Opaque -> "getOpaque";
                    case Acquire -> "getAcquire";
                    case Volatile -> "getVolatile";
                    default -> throw new IllegalStateException();
                }, MethodTypeDesc.of(
                    type()
                ));
            }
        };
    }

    Item emitSet(final BlockCreatorImpl block, final ExprImpl value, final AccessMode mode) {
        return new Item() {
            protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                value.process(iter, op);
                if (mode != AccessMode.AsDeclared) {
                    ConstantImpl.ofStaticFieldVarHandle(desc).process(iter, op);
                }
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                switch (mode) {
                    case AsDeclared -> {
                        cb.putstatic(owner(), name(), type());
                    }
                    default -> {
                        cb.invokevirtual(CD_VarHandle, switch (mode) {
                            case Plain -> "set";
                            case Opaque -> "setOpaque";
                            case Release -> "setRelease";
                            case Volatile -> "setVolatile";
                            default -> throw new IllegalStateException();
                        }, MethodTypeDesc.of(
                            CD_void,
                            type()
                        ));
                    }
                }
            }
        };
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.getstatic(owner(), name(), type());
    }
}
