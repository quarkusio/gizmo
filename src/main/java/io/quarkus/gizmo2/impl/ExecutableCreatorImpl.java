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
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ExecutableCreator;
import io.quarkus.gizmo2.creator.ParamCreator;

public sealed abstract class ExecutableCreatorImpl extends AnnotatableCreatorImpl implements ExecutableCreator permits ConstructorCreatorImpl, MethodCreatorImpl {
    protected final TypeCreatorImpl owner;
    protected int flags;
    protected List<ParamVarImpl> params = new ArrayList<>(4);

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
        mb.with(MethodParametersAttribute.of(params.stream().map(pv -> MethodParameterInfo.ofParameter(Optional.of(pv.name()), pv.flags())).toList()));
        // find parameter annotations, if any
        if (params.stream().anyMatch(pvi -> ! pvi.visible.isEmpty())) {
            mb.with(RuntimeVisibleParameterAnnotationsAttribute.of(params.stream().map(
                pvi -> pvi.visible
            ).toList()));
        }
        if (params.stream().anyMatch(pvi -> ! pvi.invisible.isEmpty())) {
            mb.with(RuntimeInvisibleParameterAnnotationsAttribute.of(params.stream().map(
                pvi -> pvi.invisible
            ).toList()));
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
            throw new IllegalStateException("Outermost block of an executable member must not fall out (return or throw instead)");
        }
    }

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

    int firstSlot() {
        return 1;
    }

    public ClassDesc owner() {
        return owner.type();
    }
}
