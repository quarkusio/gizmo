package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeKind;
import io.github.dmlloyd.classfile.instruction.SwitchCase;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

/**
 * A hashing switch implementation.
 */
abstract sealed class HashSwitchCreatorImpl<C extends ConstImpl> extends SwitchCreatorImpl<C>
        permits ClassSwitchCreatorImpl, EnumSwitchCreatorImpl, LongSwitchCreatorImpl, StringSwitchCreatorImpl {
    HashSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type,
            final Class<C> constantType) {
        super(enclosing, switchVal, type, constantType);
    }

    abstract boolean staticEquals(C a, C b);

    abstract void equaller(final CodeBuilder cb, C value, Label ifEq);

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Label fallOut, nonMatching;
        if (default_ == null) {
            fallOut = nonMatching = block.newLabel();
        } else {
            fallOut = default_.endLabel();
            nonMatching = default_.startLabel();
        }

        // `cases` is a linked map, so this sort is stable
        List<Map.Entry<C, CaseCreatorImpl>> sortedCases = casesByConstant.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> staticHash(e.getKey())))
                .toList();

        int[] hashes = casesByConstant.keySet().stream()
                .mapToInt(this::staticHash)
                .sorted()
                .distinct()
                .toArray();

        Label[] caseLabels = IntStream.of(hashes)
                .mapToObj(val -> cb.newLabel())
                .toArray(Label[]::new);

        List<SwitchCase> switchCases = IntStream.range(0, hashes.length)
                .mapToObj(i -> SwitchCase.of(hashes[i], caseLabels[i]))
                .toList();

        TypeKind tk = Util.actualKindOf(switchVal.typeKind());
        // todo: improved alloc scheme
        int idx = cb.allocateLocal(tk);
        doDup(cb);
        cb.storeLocal(tk, idx);
        hash(cb);
        if ((double) casesByConstant.size() / ((double) (max - min)) >= TABLESWITCH_DENSITY) {
            cb.tableswitch(min, max, nonMatching, switchCases);
        } else {
            cb.lookupswitch(nonMatching, switchCases);
        }

        if (!casesByConstant.isEmpty()) {
            // now write the cases
            Iterator<Map.Entry<C, CaseCreatorImpl>> iterator = sortedCases.iterator();
            assert iterator.hasNext();
            Map.Entry<C, CaseCreatorImpl> entry = iterator.next();
            int hashIdx = 0;
            int hash = staticHash(entry.getKey());
            assert hash == hashes[hashIdx];
            // start the initial series
            cb.labelBinding(caseLabels[hashIdx++]);
            cb.loadLocal(tk, idx);
            equaller(cb, entry.getKey(), entry.getValue().body.startLabel());
            while (iterator.hasNext()) {
                entry = iterator.next();
                int nextHash = staticHash(entry.getKey());
                if (hash != nextHash) {
                    // end the current series
                    cb.goto_(nonMatching);
                    // start the new series
                    cb.labelBinding(caseLabels[hashIdx++]);
                    hash = nextHash;
                }

                cb.loadLocal(tk, idx);
                equaller(cb, entry.getKey(), entry.getValue().body.startLabel());
            }
            // end the final series
            cb.goto_(nonMatching);

            // now the case blocks themselves
            for (CaseCreatorImpl case_ : cases) {
                case_.body.writeCode(cb, block);
                if (case_.body.mayFallThrough()) {
                    cb.goto_(fallOut);
                }
            }
        }

        // finally, the default block
        if (default_ == null) {
            // `fallOut` and `nonMatching` refer to the same object, so we need to bind it just once
            cb.labelBinding(fallOut);
        } else {
            default_.writeCode(cb, block);
        }
    }

    private void doDup(CodeBuilder cb) {
        if (switchVal.typeKind().slotSize() == 2) {
            cb.dup2();
        } else {
            cb.dup();
        }
    }

    private void doPop(CodeBuilder cb) {
        if (switchVal.typeKind().slotSize() == 2) {
            cb.pop2();
        } else {
            cb.pop();
        }
    }
}
