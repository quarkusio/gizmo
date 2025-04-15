package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

public final class StaticFieldVarImpl extends AssignableImpl implements StaticFieldVar {
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

    Item emitGet(final BlockCreatorImpl block, final AccessMode mode) {
        if (mode == AccessMode.AsDeclared) {
            return asBound();
        }
        return new Item() {
            public ClassDesc type() {
                return StaticFieldVarImpl.this.type();
            }

            protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                return ConstantImpl.ofFieldVarHandle(desc).forEachDependency(node.prev(), op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.invokevirtual(CD_VarHandle, switch (mode) {
                    case Plain -> "get";
                    case Opaque -> "getOpaque";
                    case Acquire -> "getAcquire";
                    case Volatile -> "getVolatile";
                    default -> throw new IllegalStateException();
                }, MethodTypeDesc.of(
                        type()));
            }

            public String itemName() {
                return "StaticFieldVar:get";
            }
        };
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final AccessMode mode) {
        return new Item() {
            protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
                if (mode != AccessMode.AsDeclared) {
                    node = ConstantImpl.ofStaticFieldVarHandle(desc).process(node.prev(), op);
                } else {
                    node = value.process(node.prev(), op);
                }
                return node;
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                switch (mode) {
                    case AsDeclared -> {
                        cb.putstatic(owner(), name(), desc().type());
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
                                type()));
                    }
                }
            }

            public String itemName() {
                return "StaticFieldVar:set";
            }
        };
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.getstatic(owner(), name(), type());
    }
}
