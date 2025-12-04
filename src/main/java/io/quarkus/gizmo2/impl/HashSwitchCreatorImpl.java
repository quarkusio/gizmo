package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;
import io.smallrye.classfile.TypeKind;
import io.smallrye.classfile.instruction.SwitchCase;

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

    abstract void equaller(final CodeBuilder cb, C value, Label ifEq, StackMapBuilder smb);

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        Label fallOut, nonMatching;
        ClassDesc type = type();
        if (default_ == null) {
            fallOut = nonMatching = block.newLabel();
        } else {
            fallOut = default_.endLabel();
            nonMatching = default_.startLabel();
            if (fallThrough) {
                default_.breakTarget();
            }
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
        smb.store(idx, switchVal.type());
        hash(cb);
        smb.pop(); // switch value
        if ((double) casesByConstant.size() / ((double) (max - min)) >= TABLESWITCH_DENSITY) {
            cb.tableswitch(min, max, nonMatching, switchCases);
        } else {
            cb.lookupswitch(nonMatching, switchCases);
        }
        smb.wroteCode();

        if (!casesByConstant.isEmpty()) {
            // now write the cases
            Iterator<Map.Entry<C, CaseCreatorImpl>> iterator = sortedCases.iterator();
            assert iterator.hasNext();
            Map.Entry<C, CaseCreatorImpl> entry = iterator.next();
            int hashIdx = 0;
            int hash = staticHash(entry.getKey());
            assert hash == hashes[hashIdx];
            // start the initial series
            Label caseLabel = caseLabels[hashIdx++];
            cb.labelBinding(caseLabel);
            smb.wroteCode();
            smb.addFrameInfo(cb);
            cb.loadLocal(tk, idx);
            smb.push(switchVal.type());
            equaller(cb, entry.getKey(), entry.getValue().body.startLabel(), smb);
            while (iterator.hasNext()) {
                entry = iterator.next();
                int nextHash = staticHash(entry.getKey());
                if (hash != nextHash) {
                    // end the current series
                    cb.goto_(nonMatching);
                    // start the new series
                    caseLabel = caseLabels[hashIdx++];
                    cb.labelBinding(caseLabel);
                    smb.wroteCode();
                    smb.addFrameInfo(cb);
                    hash = nextHash;
                }

                cb.loadLocal(tk, idx);
                smb.push(switchVal.type());
                equaller(cb, entry.getKey(), entry.getValue().body.startLabel(), smb);
            }
            // end the final series
            cb.goto_(nonMatching);
            smb.wroteCode();
            StackMapBuilder.Saved saved = smb.save();

            // now the case blocks themselves
            for (CaseCreatorImpl case_ : cases) {
                case_.body.branchTarget();
                case_.body.writeCode(cb, block, smb);
                if (case_.body.mayFallThrough()) {
                    cb.goto_(fallOut);
                }
                smb.restore(saved);
            }
        }

        // finally, the default block
        if (default_ == null) {
            // `fallOut` and `nonMatching` refer to the same object, so we need to bind it just once
            cb.labelBinding(fallOut);
            if (fallThrough) {
                smb.addFrameInfo(cb);
            }
        } else {
            default_.writeCode(cb, block, smb);
        }
        if (!Util.isVoid(type)) {
            smb.push(type());
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
