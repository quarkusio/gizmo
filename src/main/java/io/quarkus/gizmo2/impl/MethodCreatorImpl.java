package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.attribute.MethodParameterInfo;
import io.github.dmlloyd.classfile.attribute.MethodParametersAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleParameterAnnotationsAttribute;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ParamCreator;

public abstract sealed class MethodCreatorImpl extends AnnotatableCreatorImpl permits InstanceMethodCreatorImpl, StaticMethodCreatorImpl {
    protected final TypeCreatorImpl owner;
    protected final String name;
    protected int flags;
    private List<ParamVarImpl> params = new ArrayList<>(4);
    private ClassDesc returnType = ConstantDescs.CD_void;
    private MethodDesc desc;

    MethodCreatorImpl(final TypeCreatorImpl owner, final String name, final int flags) {
        this.owner = owner;
        this.name = name;
        this.flags = flags;
    }

    public MethodDesc desc() {
        MethodDesc desc = this.desc;
        if (desc == null) {
            MethodTypeDesc mtd = MethodTypeDesc.of(returnType, params.stream().map(ParamVarImpl::type).toArray(ClassDesc[]::new));
            this.desc = desc = owner instanceof InterfaceCreatorImpl ? InterfaceMethodDesc.of(owner.type(), name, mtd) : ClassMethodDesc.of(owner.type(), name, mtd);
        }
        return desc;
    }

    public void returning(final ClassDesc type) {
        this.desc = null;
        returnType = type;
    }

    public void returning(final Class<?> type) {
        returning(Util.classDesc(type));
    }

    public void body(final Consumer<BlockCreator> builder) {
        owner.zb.withMethod(name, type(), AccessFlag.STATIC.mask(), mb -> {
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
                BlockCreatorImpl bc = new BlockCreatorImpl(owner, cb);
                for (ParamVarImpl param : params) {
                    cb.localVariable(param.slot(), param.name(), param.type(), bc.startLabel(), bc.endLabel());
                }
                bc.accept(builder);
                bc.writeCode(cb, bc);
                if (bc.fallsOut()) {
                    throw new IllegalStateException("Outermost block of an executable member must not fall out (return or throw instead)");
                }
            });
        });
    }

    public MethodTypeDesc type() {
        return desc().type();
    }

    public ParamVar parameter(final String name, final Consumer<ParamCreator> builder) {
        int size = params.size();
        int slot;
        if (size == 0) {
            slot = 0;
        } else {
            ParamVarImpl last = params.get(size - 1);
            slot = last.slot() + last.typeKind().slotSize();
        }
        ParamVarImpl pv = new ParamCreatorImpl().apply(builder, name, size, slot);
        params.add(pv);
        return pv;
    }

    public ClassDesc owner() {
        return owner.type();
    }

    public String name() {
        return name;
    }
}
