package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.StackMapBuilder;
import io.smallrye.classfile.CodeBuilder;

public abstract class IntBasedConst extends ConstImpl {
    IntBasedConst(ClassDesc type) {
        super(type);
    }

    public abstract int intValue();

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
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
        smb.push(type());
        smb.wroteCode();
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
