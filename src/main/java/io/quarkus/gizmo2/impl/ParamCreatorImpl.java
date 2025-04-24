package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.ParamCreator;

public final class ParamCreatorImpl extends AnnotatableCreatorImpl implements ParamCreator {
    int flags = 0;
    boolean typeEstablished;
    ClassDesc type;

    public ParamCreatorImpl() {
    }

    public ParamCreatorImpl(final ClassDesc type) {
        this.type = type;
        typeEstablished = true;
    }

    ParamVarImpl apply(final Consumer<ParamCreator> builder, final String name, final int index, final int slot) {
        builder.accept(this);
        if (type == null) {
            throw new IllegalStateException("Parameter type was not set");
        }
        typeEstablished = true;
        return new ParamVarImpl(type, name, index, slot, flags, List.copyOf(invisible.values()), List.copyOf(visible.values()));
    }

    public void withFlag(final AccessFlag flag) {
        if (flag.locations().contains(AccessFlag.Location.METHOD_PARAMETER)) {
            flags |= flag.mask();
        } else {
            throw new IllegalArgumentException("Invalid flag for parameter: " + flag);
        }
    }

    public void withType(final ClassDesc type) {
        checkNotNullParam("type", type);
        if (type.equals(ConstantDescs.CD_void)) {
            throw new IllegalArgumentException("Bad type for parameter: " + type);
        }
        if (typeEstablished && !type.equals(this.type)) {
            throw new IllegalArgumentException("Given type " + type + " differs from established type " + this.type);
        }
        this.type = type;
    }

    void establishType(ClassDesc type) {
        if (typeEstablished) {
            if (!type.equals(this.type)) {
                throw new IllegalArgumentException("Established type " + type + " differs from existing type " + this.type);
            }
        } else {
            this.type = type;
            typeEstablished = true;
        }
    }

    public ClassDesc type() {
        return type;
    }

    ElementType annotationTargetType() {
        return ElementType.PARAMETER;
    }
}
