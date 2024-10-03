package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_int;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.Objects;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.LValueExpr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

non-sealed public abstract class ExprImpl extends Item implements Expr {

    void requireSameType(final Expr a, final Expr b) {
        if (! a.type().equals(b.type())) {
            throw new IllegalArgumentException("Type mismatch between " + a.type() + " and " + b.type());
        }
    }

    public LValueExpr elem(final Expr index) {
        if (! type().isArray()) {
            throw new IllegalArgumentException("Value type is not array");
        }
        final ClassDesc type = type().componentType();
        return new ArrayDeref(type, index);
    }

    public Expr length() {
        if (! type().isArray()) {
            throw new IllegalArgumentException("Length is only allowed on arrays (expression type is actually " + type() + ")");
        }
        return null;
    }

    public InstanceFieldVar field(final FieldDesc desc) {
        return new FieldDeref(Objects.requireNonNull(desc, "desc"));
    }

    ExprImpl asBound() {
        return bound() ? this : new ExprImpl() {
            public ClassDesc type() {
                return ExprImpl.this.type();
            }

            public boolean bound() {
                return true;
            }

            protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                ExprImpl.this.processDependencies(iter, op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                ExprImpl.this.writeCode(cb, block);
            }
        };
    }

    public final class FieldDeref extends LValueExprImpl implements InstanceFieldVar {
        private final FieldDesc desc;

        private FieldDeref(final FieldDesc desc) {
            this.desc = desc;
        }

        protected void processDependencies(final ListIterator<Item> iter, final Op op) {
            ExprImpl.this.process(iter, op);
        }

        public FieldDesc desc() {
            return desc;
        }

        ExprImpl emitGet(final BlockCreatorImpl block, final AccessMode mode) {
            return switch (mode) {
                case AsDeclared -> asBound();
                default -> new ExprImpl() {
                    public ClassDesc type() {
                        return FieldDeref.this.type();
                    }

                    public boolean bound() {
                        return true;
                    }

                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        ExprImpl.this.process(iter, op);
                        ConstantImpl.ofFieldVarHandle(desc).process(iter, op);
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
            };
        }

        Item emitSet(final BlockCreatorImpl block, final ExprImpl value, final AccessMode mode) {
            return switch (mode) {
                case AsDeclared -> new Item() {
                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        value.process(iter, op);
                        ExprImpl.this.process(iter, op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.putfield(owner(), name(), type());
                    }
                };
                default -> new Item() {
                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        value.process(iter, op);
                        ExprImpl.this.process(iter, op);
                        ConstantImpl.ofFieldVarHandle(desc).process(iter, op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, switch (mode) {
                            case Plain -> "set";
                            case Opaque -> "setOpaque";
                            case Release -> "setRelease";
                            case Volatile -> "setVolatile";
                            default -> throw new IllegalStateException();
                        }, MethodTypeDesc.of(
                            type()
                        ));
                    }
                };
            };
        }

        public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
            cb.getfield(owner(), name(), type());
        }
    }

    final class ArrayDeref extends LValueExprImpl {
        private final ClassDesc type;
        private final ExprImpl index;

        public ArrayDeref(final ClassDesc type, final Expr index) {
            this.type = type;
            this.index = (ExprImpl) index;
        }

        protected void processDependencies(final ListIterator<Item> iter, final Op op) {
            index.process(iter, op);
            ExprImpl.this.process(iter, op);
        }

        ExprImpl emitGet(final BlockCreatorImpl block, final AccessMode mode) {
            if (! mode.validForReads()) {
                throw new IllegalArgumentException("Invalid mode " + mode);
            }
            return switch (mode) {
                case AsDeclared, Plain -> asBound();
                default -> new ExprImpl() {
                    public String itemName() {
                        return "ArrayDeref$Get" + super.itemName();
                    }

                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        index.processDependencies(iter, op);
                        ExprImpl.this.processDependencies(iter, op);
                        ConstantImpl.ofArrayVarHandle(ExprImpl.this.type()).processDependencies(iter, op);
                    }

                    public ClassDesc type() {
                        return type;
                    }

                    public boolean bound() {
                        return true;
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, switch (mode) {
                            case Opaque -> "getOpaque";
                            case Acquire -> "getAcquire";
                            case Volatile -> "getVolatile";
                            default -> throw new IllegalStateException();
                        }, MethodTypeDesc.of(
                            type(),
                            ExprImpl.this.type(),
                            CD_int
                        ));
                    }
                };
            };
        }

        Item emitSet(final BlockCreatorImpl block, final ExprImpl value, final AccessMode mode) {
            return switch (mode) {
                case AsDeclared, Plain -> new ArrayStore(ExprImpl.this, index, value);
                default -> new Item() {
                    public String itemName() {
                        return "ArrayDeref$SetVolatile" + super.itemName();
                    }

                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        value.process(iter, op);
                        index.process(iter, op);
                        ExprImpl.this.process(iter, op);
                        ConstantImpl.ofArrayVarHandle(ExprImpl.this.type()).process(iter, op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, "setVolatile", MethodTypeDesc.of(
                            type(),
                            ExprImpl.this.type(),
                            CD_int
                        ));
                    }
                };
            };
        }

        public ClassDesc type() {
            return type;
        }

        public boolean bound() {
            return false;
        }

        public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
            cb.arrayLoad(typeKind());
        }
    }
}
