package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Comparator;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.instruction.SwitchCase;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

sealed abstract class PerfectHashSwitchCreatorImpl<C extends ConstImpl> extends SwitchCreatorImpl<C>
        permits EnumOrdinalSwitchCreatorImpl, IntSwitchCreatorImpl {

    PerfectHashSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type,
            final Class<C> constantType) {
        super(enclosing, switchVal, type, constantType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Label fallOut, nonMatching;
        if (default_ == null) {
            fallOut = nonMatching = block.newLabel();
        } else {
            fallOut = default_.endLabel();
            nonMatching = default_.startLabel();
        }

        List<SwitchCase> switchCases = casesByConstant.entrySet().stream()
                .map(c -> SwitchCase.of(staticHash(c.getKey()), c.getValue().body.startLabel()))
                .sorted(Comparator.comparingInt(SwitchCase::caseValue))
                .toList();

        hash(cb);
        if ((double) casesByConstant.size() / ((double) (max - min)) >= TABLESWITCH_DENSITY) {
            cb.tableswitch(min, max, nonMatching, switchCases);
        } else {
            cb.lookupswitch(nonMatching, switchCases);
        }

        // now write the cases
        for (CaseCreatorImpl case_ : casesByConstant.values()) {
            // write body
            case_.body.writeCode(cb, block);
            if (case_.body.mayFallThrough()) {
                cb.goto_(fallOut);
            }
        }
        // finally, the default block
        if (default_ == null) {
            cb.labelBinding(fallOut);
            cb.labelBinding(nonMatching);
        } else {
            default_.writeCode(cb, block);
        }
    }
}
