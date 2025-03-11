package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.MethodBuilder;
import io.github.dmlloyd.classfile.attribute.MethodParameterInfo;
import io.github.dmlloyd.classfile.attribute.MethodParametersAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ExecutableCreator;
import io.quarkus.gizmo2.creator.ParamCreator;

public sealed abstract class ExecutableCreatorImpl extends AnnotatableCreatorImpl implements ExecutableCreator
        permits ConstructorCreatorImpl, MethodCreatorImpl {
    final TypeCreatorImpl owner;
    int flags;
    List<ParamVarImpl> params = new ArrayList<>(4);
    ThisExpr this_;

    ExecutableCreatorImpl(final TypeCreatorImpl owner, final int flags) {
        this.owner = owner;
        this.flags = flags;
    }

    void doBody(final Consumer<BlockCreator> builder, MethodBuilder mb) {
        mb.withFlags(flags);
        addVisible(mb);
        addInvisible(mb);
        // lock parameters
        List<ParamVarImpl> params = this.params = List.copyOf(this.params);
        mb.with(MethodParametersAttribute
                .of(params.stream().map(pv -> MethodParameterInfo.ofParameter(Optional.of(pv.name()), pv.flags())).toList()));
        // find parameter annotations, if any
        if (params.stream().anyMatch(pvi -> !pvi.visible.isEmpty())) {
            mb.with(RuntimeVisibleParameterAnnotationsAttribute.of(params.stream().map(
                    pvi -> pvi.visible).toList()));
        }
        if (params.stream().anyMatch(pvi -> !pvi.invisible.isEmpty())) {
            mb.with(RuntimeInvisibleParameterAnnotationsAttribute.of(params.stream().map(
                    pvi -> pvi.invisible).toList()));
        }
        mb.withCode(cb -> {
            doCode(builder, cb, params);
        });
    }

    void doCode(final Consumer<BlockCreator> builder, final CodeBuilder cb, final List<ParamVarImpl> params) {
        BlockCreatorImpl bc = new BlockCreatorImpl(owner, cb);
        for (ParamVarImpl param : params) {
            cb.localVariable(param.slot(), param.name(), param.type(), bc.startLabel(), bc.endLabel());
        }
        bc.accept(builder);
        bc.writeCode(cb, bc);
        if (bc.mayFallThrough()) {
            throw new IllegalStateException(
                    "Outermost block of an executable member must not fall out (return or throw instead)");
        }
    }

    abstract String name();

    void body(final Consumer<BlockCreator> builder) {
        owner.zb.withMethod(name(), type(), flags, mb -> {
            doBody(builder, mb);
        });
    }

    public ParamVar parameter(final String name, final Consumer<ParamCreator> builder) {
        int size = params.size();
        int slot;
        if (size == 0) {
            slot = firstSlot();
        } else {
            ParamVarImpl last = params.get(size - 1);
            slot = last.slot() + last.typeKind().slotSize();
        }
        ParamVarImpl pv = new ParamCreatorImpl().apply(builder, name, size, slot);
        params.add(pv);
        return pv;
    }

    Var this_() {
        ThisExpr this_ = this.this_;
        if (this_ == null) {
            this_ = this.this_ = new ThisExpr(owner());
        }
        return this_;
    }

    int firstSlot() {
        return 1;
    }

    public ClassDesc owner() {
        return owner.type();
    }

    public void public_() {
        withFlag(AccessFlag.PUBLIC);
        withoutFlags(AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    public void packagePrivate() {
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    public void private_() {
        withFlag(AccessFlag.PRIVATE);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PROTECTED);
    }

    public void protected_() {
        withFlag(AccessFlag.PROTECTED);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE);
    }

    public void final_() {
        withFlag(AccessFlag.FINAL);
    }

    abstract void withFlag(AccessFlag flag);

    void withoutFlag(AccessFlag flag) {
        flags &= ~flag.mask();
    }

    void withoutFlags(AccessFlag... flags) {
        for (AccessFlag flag : flags) {
            withoutFlag(flag);
        }
    }
}
