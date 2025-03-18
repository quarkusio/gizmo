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
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.CaseCreator;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;
import io.quarkus.gizmo2.impl.constant.VoidConstant;

public sealed abstract class SwitchCreatorImpl<C extends ConstantImpl> extends Item implements SwitchCreator permits HashSwitchCreatorImpl, PerfectHashSwitchCreatorImpl {

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

    static final int FL_FALL_THROUGH = 0b001;
    static final int FL_BREAK        = 0b010;
    static final int FL_RETURN       = 0b100;

    boolean done;
    int flags = 0;

    SwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type, final Class<C> constantType) {
        this.enclosing = enclosing;
        this.switchVal = (Item) switchVal;
        this.type = type;
        this.constantType = constantType;
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
        return (flags & FL_FALL_THROUGH) != 0;
    }

    public boolean mayBreak() {
        return (flags & FL_BREAK) != 0;
    }

    public boolean mayReturn() {
        return (flags & FL_RETURN) != 0;
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
                    flags |= FL_FALL_THROUGH;
                } else {
                    throw new IllegalStateException("Missing default branch on switch expression");
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
        default_ = new BlockCreatorImpl(enclosing, VoidConstant.INSTANCE, type());
        body.accept(default_);
        int mask = 0;
        if (default_.mayFallThrough()) {
            mask |= FL_FALL_THROUGH;
        }
        if (default_.mayReturn()) {
            mask |= FL_RETURN;
        }
        if (default_.mayBreak()) {
            mask |= FL_BREAK;
        }
        flags |= mask;
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

    CaseCreatorImpl findCase(final Constant val) {
        return constantType.isInstance(val) ? casesByConstant.get(constantType.cast(val)) : null;
    }

    BlockCreatorImpl findDefault() {
        return default_;
    }

    public final class CaseCreatorImpl implements CaseCreator {
        private static final int ST_INITIAL = 0;
        private static final int ST_CASE_VALS = 1;
        private static final int ST_BODY = 2;
        private static final int ST_DONE = 3;

        final BlockCreatorImpl body;

        int state = ST_INITIAL;

        CaseCreatorImpl(BlockCreatorImpl parent, ClassDesc outputType) {
            body = new BlockCreatorImpl(parent, ConstantImpl.ofVoid(), outputType);
        }

        public void of(final Constant val) {
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
                int mask = 0;
                if (body.mayFallThrough()) {
                    mask |= FL_FALL_THROUGH;
                }
                if (body.mayReturn()) {
                    mask |= FL_RETURN;
                }
                if (body.mayBreak()) {
                    mask |= FL_BREAK;
                }
                flags |= mask;
            } finally {
                state = ST_DONE;
            }
        }
    }
}
