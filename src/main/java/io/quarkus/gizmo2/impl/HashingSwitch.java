package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Object;

import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.instruction.SwitchCase;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * A hashing switch implementation.
 */
final class HashingSwitch<C extends Constant> extends SwitchCreatorImpl<C> {
    private final Class<C> clazz;
    private final Consumer<CodeBuilder> hasher;
    private final Consumer<CodeBuilder> equaller;
    private final ToIntFunction<C> staticHasher;
    private final BiPredicate<C, Constant> staticEqualler;
    private final List<Case<C>> cases = new ArrayList<>();
    private BlockCreatorImpl default_;
    private boolean mayReturn;
    private boolean mayBreak;
    private boolean mayThrow;
    private boolean mayFallThrough;

    HashingSwitch(final BlockCreatorImpl enclosing, final Expr switchVal, final Class<C> clazz, final Consumer<CodeBuilder> hasher, final Consumer<CodeBuilder> equaller, final ToIntFunction<C> staticHasher, final BiPredicate<C, Constant> staticEqualler) {
        super(enclosing, switchVal);
        this.clazz = clazz;
        this.hasher = hasher;
        this.equaller = equaller;
        this.staticHasher = staticHasher;
        this.staticEqualler = staticEqualler;
    }

    HashingSwitch(final BlockCreatorImpl enclosing, final Expr switchVal, final Class<C> clazz, final Consumer<CodeBuilder> hasher, final ToIntFunction<C> staticHasher) {
        this(enclosing, switchVal, clazz, hasher, cb -> cb.invokevirtual(CD_Object, "equals", MethodTypeDesc.ofDescriptor("()Z")), staticHasher, Object::equals);
    }

    HashingSwitch(final BlockCreatorImpl enclosing, final Expr switchVal, final Class<C> clazz) {
        this(enclosing, switchVal, clazz, cb -> cb.invokevirtual(CD_Object, "hashCode", MethodTypeDesc.ofDescriptor("()I")), Object::hashCode);
    }

    Case<C> findCase(final Constant val) {
        for (Case<C> case_ : cases) {
            if (staticEqualler.test(case_.value, val)) {
                return case_;
            }
        }
        return null;
    }

    public void case_(final Constant val, final Consumer<BlockCreator> body) {
        clazz.cast(val);
        int idx = Util.binarySearch(0, cases.size(), c -> staticHasher.applyAsInt(cases.get(c).value()) >= staticHasher.applyAsInt(clazz.cast(val)));
        int nextIdx = Util.binarySearch(idx, cases.size(), c -> staticHasher.applyAsInt(cases.get(c).value()) > staticHasher.applyAsInt(clazz.cast(val)));
        if (idx < cases.size()) {
            for (int i = idx; i < nextIdx; i ++) {
                if (staticEqualler.test(clazz.cast(val), cases.get(i).value())) {
                    throw new IllegalStateException("Duplicate case for " + val);
                }
            }
        }
        Case<C> case_ = new Case<>(enclosing, clazz.cast(val));
        cases.add(idx, case_);
        case_.accept(body);
        if (case_.mayReturn()) mayReturn = true;
        if (case_.mayBreak()) mayBreak = true;
        if (case_.mayThrow()) mayThrow = true;
        if (case_.mayFallThrough()) mayFallThrough = true;
    }

    public void default_(final Consumer<BlockCreator> body) {
        if (default_ != null) {
            throw new IllegalStateException("Default block exists");
        }
        default_ = new BlockCreatorImpl(enclosing);
        default_.accept(body);
        if (default_.mayReturn()) mayReturn = true;
        if (default_.mayBreak()) mayBreak = true;
        if (default_.mayThrow()) mayThrow = true;
        if (default_.mayFallThrough()) mayFallThrough = true;
    }

    BlockCreatorImpl findDefault() {
        return default_;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // cases are sorted by hash code at this point
        Label fallOut, nonMatching;
        if (default_ == null) {
            fallOut = nonMatching = block.newLabel();
        } else {
            fallOut = default_.endLabel();
            nonMatching = default_.startLabel();
        }
        Iterator<Case<C>> iterator = cases.listIterator();
        int min, max;
        if (iterator.hasNext()) {
            Case<C> case_ = iterator.next();
            int prevHash = min = max = staticHasher.applyAsInt(case_.value());
            List<SwitchCase> switchCases = new ArrayList<>(cases.size());
            while (iterator.hasNext()) {
                case_ = iterator.next();
                int hash = staticHasher.applyAsInt(case_.value());
                if (hash < min) {
                    min = hash;
                }
                if (hash > max) {
                    max = hash;
                }
                if (hash != prevHash) {
                    // add the hash
                    switchCases.add(SwitchCase.of(hash, cb.newLabel()));
                }
            }
            // insert the switch instruction
            cb.dup();
            hasher.accept(cb);
            double range = max - min;
            double count = cases.size();
            if (count / range >= TABLESWITCH_DENSITY) {
                cb.tableswitch(min, max, nonMatching, switchCases);
            } else {
                cb.lookupswitch(nonMatching, switchCases);
            }
            // now write the cases
            iterator = cases.iterator();
            case_ = iterator.next();
            Label next = cb.newLabel();
            for (SwitchCase switchCase : switchCases) {
                if (staticHasher.applyAsInt(case_.value()) != switchCase.caseValue()) {
                    // all tests have failed
                    cb.goto_(default_.startLabel());
                    // we know there is a valid next
                    case_ = iterator.next();
                }
                cb.dup();
                equaller.accept(cb);
                cb.if_icmpne(next);
                // matched
                cb.pop();
                case_.writeCode(cb, block);
                if (case_.mayFallThrough()) {
                    cb.goto_(default_.endLabel());
                }
                cb.labelBinding(next);
                next = cb.newLabel();
            }
            // finally, the default block
            cb.labelBinding(nonMatching);
            cb.pop();
        }
        default_.writeCode(cb, block);
        cb.labelBinding(fallOut);
    }

    public boolean mayReturn() {
        return mayReturn;
    }

    public boolean mayBreak() {
        return mayBreak;
    }

    public boolean mayThrow() {
        return mayThrow;
    }

    public boolean mayFallThrough() {
        return mayFallThrough;
    }
}
