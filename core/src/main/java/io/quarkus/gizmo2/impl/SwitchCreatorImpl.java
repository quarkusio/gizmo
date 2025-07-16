package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.CaseCreator;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.quarkus.gizmo2.impl.constant.VoidConst;

public sealed abstract class SwitchCreatorImpl<C extends ConstImpl> extends Item implements SwitchCreator
        permits HashSwitchCreatorImpl, PerfectHashSwitchCreatorImpl {

    static final double TABLESWITCH_DENSITY = 0.9;

    /**
     * The enclosing block.
     */
    final BlockCreatorImpl enclosing;
    /**
     * The expression of the switch value.
     */
    final Item switchVal;
    /**
     * The result type of the switch expression ({@code void} for statements).
     */
    final ClassDesc type;
    /**
     * The type of switch constant.
     */
    final Class<C> constantType;
    /**
     * The cases indexed by key (order is significant for imperfect-hash switches).
     */
    final Map<C, CaseCreatorImpl> casesByConstant = new LinkedHashMap<>();
    /**
     * The cases in declaration order.
     */
    final List<CaseCreatorImpl> cases = new ArrayList<>();
    /**
     * The default case.
     */
    BlockCreatorImpl default_;
    /**
     * The minimum and maximum hash values.
     */
    int min, max;
    boolean fallThrough;
    boolean done;

    SwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type,
            final Class<C> constantType) {
        this.enclosing = enclosing;
        this.switchVal = (Item) switchVal;
        this.type = type;
        this.constantType = constantType;

        if (!type.equals(CD_void)) {
            // switch expressions may always fall through, even if all branches actually may not
            // this allows (and, in fact, requires) using them as actual expressions
            fallThrough = true;
        }
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return switchVal.process(node.prev(), op);
    }

    public BlockCreatorImpl enclosing() {
        return enclosing;
    }

    public Expr switchVal() {
        return switchVal;
    }

    public boolean mayFallThrough() {
        return fallThrough;
    }

    public final ClassDesc type() {
        return type;
    }

    abstract int staticHash(C val);

    abstract void hash(CodeBuilder cb);

    void accept(Consumer<? super SwitchCreatorImpl<C>> builder) {
        try {
            builder.accept(this);
            if (default_ == null) {
                if (type.equals(CD_void)) {
                    fallThrough = true;
                } else {
                    throw new IllegalStateException("Missing default branch on switch expression");
                }
                if (cases.isEmpty()) {
                    throw new IllegalStateException("No case branch and no default branch on switch");
                }
            }
        } finally {
            done = true;
        }
    }

    public void default_(final Consumer<BlockCreator> body) {
        if (done) {
            throw new IllegalStateException("Cannot add new cases");
        }
        if (default_ != null) {
            throw new IllegalStateException("Default case was already added");
        }
        default_ = new BlockCreatorImpl(enclosing, VoidConst.INSTANCE, type());
        body.accept(default_);
        if (default_.mayFallThrough()) {
            fallThrough = true;
        }
    }

    public void case_(final Consumer<CaseCreator> builder) {
        if (done) {
            throw new IllegalStateException("Cannot add new cases");
        }
        CaseCreatorImpl cci = new CaseCreatorImpl(enclosing, type);
        builder.accept(cci);
        if (cci.state < CaseCreatorImpl.ST_DONE) {
            throw new IllegalStateException("Case does not have a body");
        }
        cases.add(cci);
    }

    CaseCreatorImpl findCase(final Const val) {
        return constantType.isInstance(val) ? casesByConstant.get(constantType.cast(val)) : null;
    }

    BlockCreatorImpl findDefault() {
        return default_;
    }

    boolean contains(final BlockCreatorImpl block) {
        return (default_ != null && default_.contains(block))
                || cases.stream()
                        .map(CaseCreatorImpl.class::cast)
                        .map(CaseCreatorImpl::body)
                        .anyMatch(b -> b.contains(block));
    }

    public final class CaseCreatorImpl implements CaseCreator {
        private static final int ST_INITIAL = 0;
        private static final int ST_CASE_VALS = 1;
        private static final int ST_BODY = 2;
        private static final int ST_DONE = 3;

        final BlockCreatorImpl body;

        int state = ST_INITIAL;

        CaseCreatorImpl(BlockCreatorImpl parent, ClassDesc outputType) {
            body = new BlockCreatorImpl(parent, ConstImpl.ofVoid(), outputType);
        }

        public void of(final Const val) {
            C castVal = constantType.cast(val);
            if (state > ST_CASE_VALS) {
                throw new IllegalStateException("No more case values may be added");
            }
            state = ST_CASE_VALS;
            int hc = staticHash(castVal);
            if (casesByConstant.isEmpty()) {
                // the very first value
                min = max = hc;
            } else {
                if (hc < min) {
                    min = hc;
                }
                if (hc > max) {
                    max = hc;
                }
            }
            CaseCreatorImpl existing = casesByConstant.putIfAbsent(castVal, this);
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate case for constant " + val);
            }
        }

        BlockCreatorImpl body() {
            return body;
        }

        public void body(final Consumer<BlockCreator> builder) {
            if (state >= ST_BODY) {
                throw new IllegalStateException("Body already created");
            }
            if (state == ST_INITIAL) {
                throw new IllegalStateException("No values given for this switch case");
            }
            state = ST_BODY;
            try {
                body.accept(builder);
                if (body.mayFallThrough()) {
                    fallThrough = true;
                }
            } finally {
                state = ST_DONE;
            }
        }
    }
}
