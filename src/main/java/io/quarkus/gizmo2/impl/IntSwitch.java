package io.quarkus.gizmo2.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.instruction.SwitchCase;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.impl.constant.IntConstant;

final class IntSwitch extends SwitchCreatorImpl<IntConstant> {
    private final List<Case<IntConstant>> cases = new ArrayList<>();
    private BlockCreatorImpl default_;

    IntSwitch(final BlockCreatorImpl enclosing, final Expr switchVal) {
        super(enclosing, switchVal);
    }

    Case<IntConstant> findCase(final Constant val) {
        for (Case<IntConstant> case_ : cases) {
            if (val.equals(case_.value)) {
                return case_;
            }
        }
        return null;
    }

    BlockCreatorImpl findDefault() {
        return default_;
    }

    public void case_(final Constant val, final Consumer<BlockCreator> body) {
        cases.add(new Case<>(enclosing, (IntConstant) val));
    }

    public void default_(final Consumer<BlockCreator> body) {
        if (default_ != null) {
            throw new IllegalStateException("Default block already created");
        }
        default_ = new BlockCreatorImpl(enclosing);
        default_.accept(body);
    }

    void accept(Consumer<? super IntSwitch> builder) {
        builder.accept(this);
        if (default_ == null) {
            throw new IllegalArgumentException("No default branch for switch");
        }
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Iterator<Case<IntConstant>> iterator = cases.iterator();
        int min, max;
        if (iterator.hasNext()) {
            Case<IntConstant> case_ = iterator.next();
            min = max = case_.value.intValue();
            while (iterator.hasNext()) {
                case_ = iterator.next();
                if (case_.value.intValue() < min) {
                    min = case_.value.intValue();
                }
                if (case_.value.intValue() > max) {
                    max = case_.value.intValue();
                }
            }
            List<SwitchCase> switchCases = cases.stream().sorted(Comparator.comparingInt(c -> c.value().intValue())).map(c -> SwitchCase.of(c.value.intValue(), c.startLabel())).toList();
            double range = max - min;
            double count = cases.size();
            if (count / range >= TABLESWITCH_DENSITY) {
                cb.tableswitch(min, max, default_.startLabel(), switchCases);
            } else {
                cb.lookupswitch(default_.startLabel(), switchCases);
            }
            // now write the cases
            for (Case<IntConstant> body : cases) {
                // write body
                body.writeCode(cb, block);
                if (body.mayFallThrough()) {
                    cb.goto_(default_.endLabel());
                }
            }
        }
        // finally, the default block
        default_.writeCode(cb, block);
    }
}
