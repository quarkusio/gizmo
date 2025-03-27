package io.quarkus.gizmo2.impl.constant;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

abstract class IntBasedConstant extends ConstantImpl {
    IntBasedConstant(ClassDesc type) {
        super(type);
    }

    abstract int intValue();

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        int i = intValue();
        switch (i) {
            case -5 -> {
                cb.iconst_5();
                cb.ineg();
            }
            case -4 -> {
                cb.iconst_4();
                cb.ineg();
            }
            case -3 -> {
                cb.iconst_3();
                cb.ineg();
            }
            case -2 -> {
                cb.iconst_2();
                cb.ineg();
            }
            default -> cb.loadConstant(i);
        }
    }

    public boolean isZero() {
        return intValue() == 0;
    }

    public boolean isNonZero() {
        return intValue() != 0;
    }

    public StringBuilder toShortString(final StringBuilder b) {
        int i = intValue();
        return b.append(i).append(" (0x").append(Integer.toHexString(i)).append(')');
    }
}
