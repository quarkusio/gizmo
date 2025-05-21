package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.AnonymousClassCreator;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;

/**
 * A capturing class creator.
 */
public final class AnonymousClassCreatorImpl extends ClassCreatorImpl implements AnonymousClassCreator {
    private final ConstructorDesc superCtor;
    private final ArrayList<Consumer<ConstructorCreator>> ctorSetups = new ArrayList<>();
    private final ArrayList<Expr> captureExprs;
    private final ArrayList<Consumer<BlockCreator>> captures = new ArrayList<>();
    private final ArrayList<Expr> superArgs;
    private final ThisExpr this_;

    AnonymousClassCreatorImpl(final ClassDesc type, final ClassOutput output, final ClassBuilder zb,
            final ConstructorDesc superCtor, final ArrayList<Expr> captureExprs) {
        super(type, output, zb);
        modifiers |= ACC_FINAL;
        this.superCtor = superCtor;
        extends_(superCtor.owner());
        superArgs = new ArrayList<>(superCtor.type().parameterCount());
        this.captureExprs = captureExprs;
        ctorSetups.add(cc -> {
            MethodTypeDesc ctorType = superCtor.type();
            int cnt = ctorType.parameterCount();
            for (int i = 0; i < cnt; i++) {
                superArgs.add(cc.parameter("p" + i, ctorType.parameterType(i)));
            }
        });
        this_ = new ThisExpr(genericType());
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.ANONYMOUS_CLASS;
    }

    public Var capture(final String name, final Expr value) {
        ctorSetups.add(cc -> {
            // define additional parameters
            ParamVar param = cc.parameter(name, value.type());
            FieldDesc desc = field(name, ifc -> {
                ifc.withType(value.type());
                ifc.withAccess(AccessLevel.PRIVATE);
                ifc.withFlag(ModifierFlag.FINAL);
            });
            InstanceFieldVar fv = this_().field(desc);
            captures.add(b0 -> {
                b0.set(fv, param);
            });
        });
        captureExprs.add(value);
        return this_.field(FieldDesc.of(type(), name, value.type()));
    }

    void freezeCaptures() {
        ctorSetups.add(cc -> {
            cc.body(b0 -> {
                b0.invokeSpecial(superCtor, this_(), superArgs);
                for (Consumer<BlockCreator> capture : captures) {
                    capture.accept(b0);
                }
                b0.return_();
            });
        });
    }

    ArrayList<Consumer<ConstructorCreator>> ctorSetups() {
        return ctorSetups;
    }
}
