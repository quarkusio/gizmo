package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.desc.Descs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.TypeParameter;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.constant.ArrayVarHandleConst;
import io.quarkus.gizmo2.impl.constant.CharConst;
import io.quarkus.gizmo2.impl.constant.ClassConst;
import io.quarkus.gizmo2.impl.constant.DynamicConst;
import io.quarkus.gizmo2.impl.constant.EnumConst;
import io.quarkus.gizmo2.impl.constant.FieldVarHandleConst;
import io.quarkus.gizmo2.impl.constant.IntBasedConst;
import io.quarkus.gizmo2.impl.constant.InvokeConst;
import io.quarkus.gizmo2.impl.constant.MethodHandleConst;
import io.quarkus.gizmo2.impl.constant.MethodTypeConst;
import io.quarkus.gizmo2.impl.constant.StaticFieldVarHandleConst;
import io.quarkus.gizmo2.impl.constant.StaticFinalFieldConst;
import io.smallrye.classfile.Annotation;

/**
 * Centralized pseudo-Java source code generator for the Gizmo IR.
 * <p>
 * This class renders {@link Item} nodes as pseudo-Java source text using
 * {@code instanceof} pattern matching over item types. Expressions are
 * rendered inline by {@link #expr(StringBuilder, Item, SourceBuilder)},
 * which appends directly to a shared {@link StringBuilder} to avoid
 * intermediate string allocations. Statements emit lines via
 * {@link #statement(Item, SourceBuilder)}.
 * <p>
 * The generated source is not necessarily valid Java, but aims to be as
 * close as possible, using consistent pseudo-syntax for constructs that
 * have no direct Java representation.
 */
public final class SourceGenerator {

    private SourceGenerator() {
    }

    // region Type name utilities

    /**
     * {@return a readable type name for the given class descriptor}
     * Uses fully-qualified dot-separated names for reference types;
     * uses Java keyword names for primitives.
     *
     * @param desc the class descriptor (must not be {@code null})
     */
    public static String typeName(ClassDesc desc) {
        if (desc.isPrimitive()) {
            return switch (desc.descriptorString()) {
                case "V" -> "void";
                case "Z" -> "boolean";
                case "B" -> "byte";
                case "C" -> "char";
                case "S" -> "short";
                case "I" -> "int";
                case "J" -> "long";
                case "F" -> "float";
                case "D" -> "double";
                default -> desc.displayName();
            };
        }
        if (desc.isArray()) {
            return typeName(desc.componentType()) + "[]";
        }
        // reference type: convert internal form to dot-separated
        String internal = Util.internalName(desc);
        return internal.replace('/', '.');
    }

    /**
     * {@return a readable type name for the given class descriptor, potentially simplified via import tracking}
     * When a non-null {@code sb} is provided, the type is registered with the import tracker.
     * For reference types, the returned name may be a simple name if the type successfully
     * claimed its simple name, or a fully-qualified name if there is a conflict.
     *
     * @param desc the class descriptor (must not be {@code null})
     * @param sb the source builder for import tracking, or {@code null} for FQN-only behavior
     */
    public static String typeName(ClassDesc desc, SourceBuilder sb) {
        if (desc.isPrimitive()) {
            return typeName(desc);
        }
        if (desc.isArray()) {
            return typeName(desc.componentType(), sb) + "[]";
        }
        if (sb == null) {
            return typeName(desc);
        }
        return sb.importTracker().resolveName(desc, typeName(desc));
    }

    /**
     * {@return a function that resolves type names through the given source builder's import tracker}
     *
     * @param sb the source builder for import tracking (must not be {@code null})
     */
    public static Function<ClassDesc, String> typeNameFn(SourceBuilder sb) {
        return desc -> typeName(desc, sb);
    }

    // endregion

    // region Expression rendering

    /**
     * Appends an inline expression for the given item to the buffer.
     * Items that have no natural Java expression form are rendered
     * using a consistent pseudo-syntax.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param item the item to render (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder expr(StringBuilder buf, Item item, SourceBuilder sb) {
        return item.appendSourceExpr(buf, sb);
    }

    /**
     * Appends a character literal representation for the given code point to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param cp the code point value
     * @return the buffer, for chaining
     */
    public static StringBuilder charLiteral(StringBuilder buf, int cp) {
        switch (cp) {
            case '\'' -> buf.append("'\\''");
            case '\\' -> buf.append("'\\\\'");
            case '\n' -> buf.append("'\\n'");
            case '\r' -> buf.append("'\\r'");
            case '\t' -> buf.append("'\\t'");
            case '\b' -> buf.append("'\\b'");
            case '\f' -> buf.append("'\\f'");
            default -> {
                if (cp >= 32 && cp < 127) {
                    buf.append('\'').append((char) cp).append('\'');
                } else {
                    buf.append("'\\u").append(String.format("%04x", cp)).append('\'');
                }
            }
        }
        return buf;
    }

    /**
     * Maximum length for string literals in bootstrap args before truncation.
     */
    private static final int MAX_BSM_STRING_LENGTH = 512;

    /**
     * Appends a quoted and escaped string literal to the buffer,
     * truncating values longer than the given maximum.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param s the string value (must not be {@code null})
     * @param maxLen the maximum number of characters to include before truncating
     * @return the buffer, for chaining
     */
    public static StringBuilder stringLiteral(StringBuilder buf, String s, int maxLen) {
        buf.append('"');
        int limit = Math.min(s.length(), maxLen);
        int cp;
        for (int i = 0; i < limit; i += Character.charCount(cp)) {
            cp = s.codePointAt(i);
            switch (cp) {
                case '\b' -> buf.append("\\b");
                case '\f' -> buf.append("\\f");
                case '\n' -> buf.append("\\n");
                case '\r' -> buf.append("\\r");
                case '\t' -> buf.append("\\t");
                case '"', '\\' -> buf.append('\\').appendCodePoint(cp);
                default -> {
                    if (Character.isISOControl(cp)) {
                        buf.append("\\u").append(String.format("%04x", cp));
                    } else {
                        buf.appendCodePoint(cp);
                    }
                }
            }
        }
        if (s.length() > maxLen) {
            buf.append("...");
        }
        return buf.append('"');
    }

    /**
     * Class descriptor for {@code java.lang.invoke.MethodHandle}, used in constant rendering.
     */
    private static final ClassDesc CD_MethodHandle = ClassDesc.of("java.lang.invoke.MethodHandle");

    /**
     * Class descriptor for {@code java.lang.invoke.MethodType}, used in constant rendering.
     */
    private static final ClassDesc CD_MethodType = ClassDesc.of("java.lang.invoke.MethodType");

    /**
     * Class descriptor for {@code java.lang.invoke.VarHandle}, used in constant rendering.
     */
    private static final ClassDesc CD_VarHandle = ClassDesc.of("java.lang.invoke.VarHandle");

    /**
     * Appends a method type signature in {@code (T1,T2):R} syntax to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param mt the method type descriptor (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder methodTypeSig(StringBuilder buf, MethodTypeDesc mt, SourceBuilder sb) {
        buf.append('(');
        for (int i = 0; i < mt.parameterCount(); i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(typeName(mt.parameterType(i), sb));
        }
        buf.append("):");
        return buf.append(typeName(mt.returnType(), sb));
    }

    /**
     * Appends a VarHandle instance-field target expression ({@code (&instance.fieldName)}) to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param instance the instance expression (must not be {@code null})
     * @param fieldName the field name (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder varHandleFieldTarget(StringBuilder buf, Item instance, String fieldName,
            SourceBuilder sb) {
        buf.append("(&");
        expr(buf, instance, sb).append('.').append(fieldName);
        return buf.append(')');
    }

    /**
     * Appends a VarHandle static-field target expression ({@code (&Owner.fieldName)}) to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param owner the owning class descriptor (must not be {@code null})
     * @param fieldName the field name (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder varHandleStaticTarget(StringBuilder buf, ClassDesc owner, String fieldName,
            SourceBuilder sb) {
        buf.append("(&");
        buf.append(typeName(owner, sb)).append('.').append(fieldName);
        return buf.append(')');
    }

    /**
     * Appends a VarHandle array-element target expression ({@code (&array[index])}) to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param array the array expression (must not be {@code null})
     * @param index the index expression (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder varHandleArrayTarget(StringBuilder buf, Item array, Item index, SourceBuilder sb) {
        buf.append("(&");
        expr(buf, array, sb).append('[');
        expr(buf, index, sb).append(']');
        return buf.append(')');
    }

    /**
     * {@return the mode suffix for VarHandle get/set operations}
     *
     * @param mode the memory order (must not be {@code null})
     */
    public static String getSetModeSuffix(MemoryOrder mode) {
        return switch (mode) {
            case Plain -> "Plain";
            case Opaque -> "Opaque";
            case Acquire -> "Acquire";
            case Release -> "Release";
            case Volatile -> "Volatile";
            case AsDeclared -> "Default";
        };
    }

    /**
     * {@return the VarHandle RMW method name for the given operation and mode}
     *
     * @param op the operation name (e.g., "Set", "Add") (must not be {@code null})
     * @param mode the memory order (must not be {@code null})
     */
    public static String rmwMethodName(String op, MemoryOrder mode) {
        return switch (mode) {
            case Volatile -> "getAnd" + op;
            case Acquire -> "getAnd" + op + "Acquire";
            case Release -> "getAnd" + op + "Release";
            default -> "getAnd" + op + mode.name();
        };
    }

    /**
     * {@return the VarHandle compare-and-set method name for the given weak flag and mode}
     *
     * @param weak whether this is a weak CAS
     * @param mode the memory order (must not be {@code null})
     */
    public static String casMethodName(boolean weak, MemoryOrder mode) {
        return switch (mode) {
            case Volatile -> weak ? "weakCompareAndSet" : "compareAndSet";
            case Acquire -> "weakCompareAndSetAcquire";
            case Release -> "weakCompareAndSetRelease";
            case Plain -> "weakCompareAndSetPlain";
            default -> (weak ? "weakCompareAndSet" : "compareAndSet") + mode.name();
        };
    }

    /**
     * {@return the VarHandle compare-and-exchange method name for the given mode}
     *
     * @param mode the memory order (must not be {@code null})
     */
    public static String caeMethodName(MemoryOrder mode) {
        return switch (mode) {
            case Volatile -> "compareAndExchange";
            case Acquire -> "compareAndExchangeAcquire";
            case Release -> "compareAndExchangeRelease";
            default -> "compareAndExchange" + mode.name();
        };
    }

    /**
     * Appends a pseudo-source representation of a {@link ConstantDesc} value to the buffer.
     * This is used for rendering bootstrap arguments in invokedynamic expressions.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param cd the constant descriptor to render (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder constDescExpr(StringBuilder buf, ConstantDesc cd, SourceBuilder sb) {
        if (cd instanceof String s) {
            return stringLiteral(buf, s, MAX_BSM_STRING_LENGTH);
        } else if (cd instanceof Integer i) {
            return buf.append(i);
        } else if (cd instanceof Long l) {
            return buf.append(l).append('L');
        } else if (cd instanceof Float f) {
            return buf.append(f).append('f');
        } else if (cd instanceof Double d) {
            return buf.append(d).append('d');
        } else if (cd instanceof ClassDesc classDesc) {
            return buf.append(typeName(classDesc, sb)).append(".class");
        } else if (cd instanceof MethodTypeDesc mt) {
            return methodTypeSig(buf, mt, sb);
        } else if (cd instanceof DirectMethodHandleDesc dmh) {
            return buf.append(typeName(dmh.owner(), sb)).append("::").append(dmh.methodName());
        } else if (cd instanceof DynamicConstantDesc<?> dcd) {
            return buf.append("const dynamic ").append(dcd.constantName());
        } else {
            return buf.append(cd);
        }
    }

    /**
     * {@return the comparison operator symbol for the given if-kind}
     *
     * @param kind the comparison kind (must not be {@code null})
     */
    public static String relSymbol(If.Kind kind) {
        return switch (kind) {
            case EQ -> "==";
            case NE -> "!=";
            case LT -> "<";
            case GE -> ">=";
            case LE -> "<=";
            case GT -> ">";
        };
    }

    /**
     * Appends the comparison-against-zero/null expression suffix to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param r the rel-zero item (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder relZeroExpr(StringBuilder buf, RelZero r) {
        if (r.input().typeKind() == io.quarkus.gizmo2.TypeKind.REFERENCE) {
            return switch (r.kind()) {
                case EQ -> buf.append("== null");
                case NE -> buf.append("!= null");
                default -> buf.append(relSymbol(r.kind())).append(" null");
            };
        }
        return buf.append(relSymbol(r.kind())).append(" 0");
    }

    /**
     * Appends source text for a dynamic constant expression to the buffer.
     * <p>
     * Renders using the {@code const dynamic} pseudo-keyword with the bootstrap
     * method and its arguments shown in brackets, followed by the constant name
     * and type:
     * {@code const dynamic[Bootstrap::method(bsmArgs)] name: Type}.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param c the dynamic constant (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder dynamicConstExpr(StringBuilder buf, DynamicConst c, SourceBuilder sb) {
        DynamicConstantDesc<?> desc = c.desc();
        buf.append("const dynamic");
        DirectMethodHandleDesc bsm = desc.bootstrapMethod();
        buf.append('[');
        buf.append(typeName(bsm.owner(), sb)).append("::").append(bsm.methodName());
        ConstantDesc[] bsmArgs = desc.bootstrapArgs();
        if (bsmArgs.length > 0) {
            buf.append('(');
            for (int i = 0; i < bsmArgs.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                constDescExpr(buf, bsmArgs[i], sb);
            }
            buf.append(')');
        }
        buf.append(']');
        buf.append(' ').append(desc.constantName());
        buf.append(": ").append(typeName(desc.constantType(), sb));
        return buf;
    }

    /**
     * Appends source text for a new-object expression to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param nr the new-result item (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder newExpr(StringBuilder buf, NewResult nr, SourceBuilder sb) {
        Invoke inv = nr.invoke();
        buf.append("new ").append(typeName(nr.type(), sb)).append('(');
        List<Item> args = inv.args();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            expr(buf, args.get(i), sb);
        }
        return buf.append(')');
    }

    /**
     * Appends source text for a method invocation expression to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param inv the invocation item (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder invokeExpr(StringBuilder buf, Invoke inv, SourceBuilder sb) {
        if (inv.instance() != null) {
            if (inv.name().equals("<init>")) {
                // constructor/super call
                if (inv.instance() instanceof ThisExpr) {
                    buf.append("super");
                } else {
                    expr(buf, inv.instance(), sb);
                }
            } else {
                expr(buf, inv.instance(), sb).append('.').append(inv.name());
            }
        } else {
            // static call
            buf.append(typeName(inv.owner(), sb)).append('.').append(inv.name());
        }
        buf.append('(');
        List<Item> args = inv.args();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            expr(buf, args.get(i), sb);
        }
        return buf.append(')');
    }

    /**
     * Appends source text for an invokedynamic expression to the buffer.
     * <p>
     * Lambda call sites are detected and rendered as method references
     * (e.g., {@code Outer::lambda$main$0}). Other invokedynamic call sites
     * are rendered using the {@code invokedynamic} pseudo-keyword with the
     * bootstrap method and its arguments shown in brackets:
     * {@code invokedynamic[Bootstrap::method(bsmArgs)] name(invocationArgs)}.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param id the invokedynamic item (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder invokeDynamicExpr(StringBuilder buf, InvokeDynamic id, SourceBuilder sb) {
        DynamicCallSiteDesc desc = id.callSiteDesc();
        if (desc.bootstrapMethod() instanceof DirectMethodHandleDesc bootstrap) {
            String bsmName = bootstrap.methodName();
            // classic lambda via LambdaMetafactory
            if ((bsmName.equals("metafactory") || bsmName.equals("altMetafactory"))
                    && Util.equals(bootstrap.owner(), CD_LambdaMetafactory)) {
                ConstantDesc[] bsmArgs = desc.bootstrapArgs();
                if (bsmArgs.length >= 2 && bsmArgs[1] instanceof DirectMethodHandleDesc impl) {
                    return buf.append(typeName(impl.owner(), sb))
                            .append("::")
                            .append(impl.methodName());
                }
            }
            // hidden-class lambda via defineLambdaCallSite
            if (bsmName.equals("defineLambdaCallSite")) {
                return buf.append(typeName(bootstrap.owner(), sb))
                        .append("$lambda");
            }
        }
        // fallback: generic invokedynamic rendering
        // format: invokedynamic[Bootstrap::method(bsmArgs)] name(invocationArgs)
        buf.append("invokedynamic");
        if (desc.bootstrapMethod() instanceof DirectMethodHandleDesc bsm) {
            buf.append('[');
            buf.append(typeName(bsm.owner(), sb)).append("::").append(bsm.methodName());
            ConstantDesc[] bsmArgs = desc.bootstrapArgs();
            if (bsmArgs.length > 0) {
                buf.append('(');
                for (int i = 0; i < bsmArgs.length; i++) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    constDescExpr(buf, bsmArgs[i], sb);
                }
                buf.append(')');
            }
            buf.append(']');
        }
        buf.append(' ').append(desc.invocationName()).append('(');
        List<Item> args = id.invocationArgs();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            expr(buf, args.get(i), sb);
        }
        return buf.append(')');
    }

    /**
     * Appends a multi-line if-expression pseudo-syntax to the buffer.
     * Used for value-producing {@code If} nodes with non-trivial branches.
     * <p>
     * The rendered form is:
     *
     * <pre>{@code
     * if (cond) {
     *     stmt;
     *     yield result1;
     * } else {
     *     stmt;
     *     yield result2;
     * }
     * }</pre>
     *
     * The caller is expected to have already called {@link SourceBuilder#startLine()};
     * this method finishes the current line, renders the branches, and starts
     * the final {@code }} line so the caller can append to it (e.g., {@code ;}).
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param ifItem the value-producing if item (must not be {@code null})
     * @param sb the source builder for line and indent management (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder ifExprExpr(StringBuilder buf, If ifItem, SourceBuilder sb) {
        buf.append("if (");
        ifCondition(buf, ifItem, sb);
        buf.append(") {");
        sb.endLine();
        sb.indent();
        generateBlock(ifItem.whenTrue, sb);
        sb.dedent();
        sb.line("} else {");
        sb.indent();
        generateBlock(ifItem.whenFalse, sb);
        sb.dedent();
        sb.startLine();
        return buf.append('}');
    }

    // region Expression dispatch methods

    /**
     * Appends a character constant expression.
     *
     * @param c the character constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprCharConst(CharConst c, StringBuilder buf, SourceBuilder sb) {
        return charLiteral(buf, c.intValue());
    }

    /**
     * Appends a class constant expression ({@code Type.class}).
     *
     * @param c the class constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprClassConst(ClassConst c, StringBuilder buf, SourceBuilder sb) {
        return buf.append(typeName(c.desc(), sb)).append(".class");
    }

    /**
     * Appends an enum constant expression.
     *
     * @param c the enum constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprEnumConst(EnumConst c, StringBuilder buf, SourceBuilder sb) {
        return buf.append(typeName(c.desc().constantType(), sb)).append('.').append(c.name());
    }

    /**
     * Appends a method handle constant expression.
     *
     * @param mhc the method handle constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprMethodHandleConst(MethodHandleConst mhc, StringBuilder buf, SourceBuilder sb) {
        buf.append("const ").append(typeName(CD_MethodHandle, sb)).append('[');
        if (mhc.desc() instanceof DirectMethodHandleDesc dmh) {
            buf.append(typeName(dmh.owner(), sb)).append("::").append(dmh.methodName());
            MethodTypeDesc mt = dmh.invocationType();
            int refKind = dmh.refKind();
            // skip receiver param for instance-kind handles
            int start = (refKind == 1 || refKind == 3 || refKind == 5 || refKind == 7 || refKind == 9) ? 1 : 0;
            buf.append('(');
            for (int i = start; i < mt.parameterCount(); i++) {
                if (i > start) {
                    buf.append(',');
                }
                buf.append(typeName(mt.parameterType(i), sb));
            }
            buf.append("):").append(typeName(mt.returnType(), sb));
        } else {
            buf.append(mhc.desc());
        }
        return buf.append(']');
    }

    /**
     * Appends a method type constant expression.
     *
     * @param mtc the method type constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprMethodTypeConst(MethodTypeConst mtc, StringBuilder buf, SourceBuilder sb) {
        buf.append("const ").append(typeName(CD_MethodType, sb)).append('[');
        methodTypeSig(buf, mtc.methodTypeDesc(), sb);
        return buf.append(']');
    }

    /**
     * Appends an invoke constant expression.
     *
     * @param ic the invoke constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprInvokeConst(InvokeConst ic, StringBuilder buf, SourceBuilder sb) {
        buf.append("const ");
        MethodHandleConst hc = ic.handleConstant();
        if (hc.desc() instanceof DirectMethodHandleDesc dmh) {
            buf.append(typeName(dmh.owner(), sb)).append('.').append(dmh.methodName());
        } else {
            hc.toShortString(buf);
        }
        buf.append('(');
        var args = ic.args();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            expr(buf, args.get(i), sb);
        }
        return buf.append(')');
    }

    /**
     * Appends a static final field constant expression.
     *
     * @param sffc the static final field constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFinalFieldConst(StaticFinalFieldConst sffc, StringBuilder buf,
            SourceBuilder sb) {
        FieldDesc fd = sffc.fieldDesc();
        return buf.append("const ").append(typeName(fd.owner(), sb)).append('.').append(fd.name());
    }

    /**
     * Appends a field VarHandle constant expression.
     *
     * @param fvhc the field VarHandle constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprFieldVarHandleConst(FieldVarHandleConst fvhc, StringBuilder buf,
            SourceBuilder sb) {
        FieldDesc f = fvhc.field();
        return buf.append("const ").append(typeName(CD_VarHandle, sb)).append('[')
                .append(typeName(f.owner(), sb)).append('.').append(f.name()).append(']');
    }

    /**
     * Appends a static field VarHandle constant expression.
     *
     * @param svhc the static field VarHandle constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFieldVarHandleConst(StaticFieldVarHandleConst svhc, StringBuilder buf,
            SourceBuilder sb) {
        FieldDesc f = svhc.field();
        return buf.append("const ").append(typeName(CD_VarHandle, sb)).append("[static ")
                .append(typeName(f.owner(), sb)).append('.').append(f.name()).append(']');
    }

    /**
     * Appends an array VarHandle constant expression.
     *
     * @param avhc the array VarHandle constant (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayVarHandleConst(ArrayVarHandleConst avhc, StringBuilder buf,
            SourceBuilder sb) {
        return buf.append("const ").append(typeName(CD_VarHandle, sb)).append('[')
                .append(typeName(avhc.arrayType(), sb)).append(']');
    }

    /**
     * Appends a binary operation expression.
     *
     * @param b the binary operation (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprBinOp(BinOp b, StringBuilder buf, SourceBuilder sb) {
        buf.append('(');
        expr(buf, b.left(), sb).append(' ').append(b.kind().symbol).append(' ');
        return expr(buf, b.right(), sb).append(')');
    }

    /**
     * Appends a relational comparison expression.
     *
     * @param r the relational comparison (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprRel(Rel r, StringBuilder buf, SourceBuilder sb) {
        buf.append('(');
        expr(buf, r.left(), sb).append(' ').append(relSymbol(r.kind())).append(' ');
        return expr(buf, r.right(), sb).append(')');
    }

    /**
     * Appends a compare-against-zero/null expression.
     *
     * @param r the rel-zero item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprRelZero(RelZero r, StringBuilder buf, SourceBuilder sb) {
        buf.append('(');
        expr(buf, r.input(), sb).append(' ');
        return relZeroExpr(buf, r).append(')');
    }

    /**
     * Appends a negation expression.
     *
     * @param n the negation (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprNeg(Neg n, StringBuilder buf, SourceBuilder sb) {
        buf.append("(-");
        return expr(buf, n.input(), sb).append(')');
    }

    /**
     * Appends an instanceof expression.
     *
     * @param io the instanceof item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprInstanceOf(InstanceOf io, StringBuilder buf, SourceBuilder sb) {
        buf.append('(');
        expr(buf, io.input(), sb).append(" instanceof ").append(typeName(io.checkType(), sb));
        return buf.append(')');
    }

    /**
     * Appends a checked cast expression.
     *
     * @param c the checked cast (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprCheckCast(CheckCast c, StringBuilder buf, SourceBuilder sb) {
        buf.append("((").append(typeName(c.type(), sb)).append(") ");
        return expr(buf, c.a, sb).append(')');
    }

    /**
     * Appends a primitive cast expression.
     *
     * @param c the primitive cast (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprPrimitiveCast(PrimitiveCast c, StringBuilder buf, SourceBuilder sb) {
        buf.append("((").append(typeName(c.type(), sb)).append(") ");
        return expr(buf, c.a, sb).append(')');
    }

    /**
     * Appends an unchecked cast expression (transparent — renders the inner expression only).
     *
     * @param c the unchecked cast (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprUncheckedCast(UncheckedCast c, StringBuilder buf, SourceBuilder sb) {
        return expr(buf, c.a, sb);
    }

    /**
     * Appends a boxing expression (transparent — renders the inner expression only).
     *
     * @param b the box operation (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprBox(Box b, StringBuilder buf, SourceBuilder sb) {
        return expr(buf, b.a, sb);
    }

    /**
     * Appends an unboxing expression (transparent — renders the inner expression only).
     *
     * @param u the unbox operation (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprUnbox(Unbox u, StringBuilder buf, SourceBuilder sb) {
        return expr(buf, u.a, sb);
    }

    /**
     * Appends a field dereference expression.
     *
     * @param f the field dereference (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprFieldDeref(FieldDeref f, StringBuilder buf, SourceBuilder sb) {
        return expr(buf, f.instance(), sb).append('.').append(f.desc().name());
    }

    /**
     * Appends a static field variable expression.
     *
     * @param sf the static field variable (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFieldVar(StaticFieldVarImpl sf, StringBuilder buf, SourceBuilder sb) {
        return buf.append(typeName(sf.desc().owner(), sb)).append('.').append(sf.desc().name());
    }

    /**
     * Appends an array dereference expression.
     *
     * @param a the array dereference (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayDeref(ArrayDeref a, StringBuilder buf, SourceBuilder sb) {
        expr(buf, a.array(), sb).append('[');
        return expr(buf, a.index(), sb).append(']');
    }

    /**
     * Appends an array length expression.
     *
     * @param a the array length item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayLength(ArrayLength a, StringBuilder buf, SourceBuilder sb) {
        return expr(buf, a.arrayItem(), sb).append(".length");
    }

    /**
     * Appends a new empty array expression.
     *
     * @param na the new empty array item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprNewEmptyArray(NewEmptyArray na, StringBuilder buf, SourceBuilder sb) {
        buf.append("new ").append(typeName(na.type().componentType(), sb)).append('[');
        return expr(buf, na.sizeItem(), sb).append(']');
    }

    /**
     * Appends a dup expression (transparent — renders the inner expression only).
     *
     * @param d the dup item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprDup(Dup d, StringBuilder buf, SourceBuilder sb) {
        return expr(buf, d.inputItem(), sb);
    }

    /**
     * Appends a bound item expression (transparent — renders the wrapped item).
     *
     * @param bi the bound item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprBoundItem(BoundItem bi, StringBuilder buf, SourceBuilder sb) {
        return expr(buf, bi.item(), sb);
    }

    /**
     * Appends a VarHandle field get expression.
     *
     * @param fgvh the field get via handle item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprFieldGetViaHandle(FieldGetViaHandle fgvh, StringBuilder buf, SourceBuilder sb) {
        FieldDeref fd = fgvh.fieldDeref();
        varHandleFieldTarget(buf, fd.instance(), fd.desc().name(), sb);
        return buf.append(".get").append(getSetModeSuffix(fgvh.mode())).append("()");
    }

    /**
     * Appends a VarHandle static field get expression.
     *
     * @param sfgvh the static field get via handle item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFieldGetViaHandle(StaticFieldGetViaHandle sfgvh, StringBuilder buf,
            SourceBuilder sb) {
        StaticFieldVarImpl sfv = sfgvh.staticFieldVar();
        varHandleStaticTarget(buf, sfv.desc().owner(), sfv.desc().name(), sb);
        return buf.append(".get").append(getSetModeSuffix(sfgvh.mode())).append("()");
    }

    /**
     * Appends a VarHandle array load expression.
     *
     * @param alvh the array load via handle item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayLoadViaHandle(ArrayLoadViaHandle alvh, StringBuilder buf, SourceBuilder sb) {
        ArrayDeref ad = alvh.arrayDeref();
        varHandleArrayTarget(buf, ad.array(), ad.index(), sb);
        return buf.append(".get").append(getSetModeSuffix(alvh.mode())).append("()");
    }

    /**
     * Appends a VarHandle field set expression.
     *
     * @param fsvh the field set via handle item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprFieldSetViaHandle(FieldSetViaHandle fsvh, StringBuilder buf, SourceBuilder sb) {
        FieldDeref fd = fsvh.fieldDeref();
        varHandleFieldTarget(buf, fd.instance(), fd.desc().name(), sb);
        buf.append(".set").append(getSetModeSuffix(fsvh.mode())).append('(');
        return expr(buf, fsvh.value(), sb).append(')');
    }

    /**
     * Appends a VarHandle static field set expression.
     *
     * @param ssvh the static field set via handle item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFieldSetViaHandle(StaticFieldSetViaHandle ssvh, StringBuilder buf,
            SourceBuilder sb) {
        StaticFieldVarImpl sfv = ssvh.staticFieldVar();
        varHandleStaticTarget(buf, sfv.desc().owner(), sfv.desc().name(), sb);
        buf.append(".set").append(getSetModeSuffix(ssvh.mode())).append('(');
        return expr(buf, ssvh.value(), sb).append(')');
    }

    /**
     * Appends a VarHandle array store expression.
     *
     * @param asvh the array store via handle item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayStoreViaHandle(ArrayStoreViaHandle asvh, StringBuilder buf,
            SourceBuilder sb) {
        ArrayDeref ad = asvh.arrayDeref();
        varHandleArrayTarget(buf, ad.array(), ad.index(), sb);
        buf.append(".set").append(getSetModeSuffix(asvh.mode())).append('(');
        return expr(buf, asvh.value(), sb).append(')');
    }

    /**
     * Appends a VarHandle field read-modify-write expression.
     *
     * @param frmw the field RMW item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprFieldReadModifyWrite(FieldReadModifyWrite frmw, StringBuilder buf,
            SourceBuilder sb) {
        FieldDesc fd = ((FieldVarHandleConst) frmw.handle()).field();
        varHandleFieldTarget(buf, frmw.instance(), fd.name(), sb);
        buf.append('.').append(rmwMethodName(frmw.op(), frmw.mode())).append('(');
        return expr(buf, frmw.value(), sb).append(')');
    }

    /**
     * Appends a VarHandle static field read-modify-write expression.
     *
     * @param srmw the static field RMW item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFieldReadModifyWrite(StaticFieldReadModifyWrite srmw, StringBuilder buf,
            SourceBuilder sb) {
        FieldDesc fd = ((StaticFieldVarHandleConst) srmw.handle()).field();
        varHandleStaticTarget(buf, fd.owner(), fd.name(), sb);
        buf.append('.').append(rmwMethodName(srmw.op(), srmw.mode())).append('(');
        return expr(buf, srmw.value(), sb).append(')');
    }

    /**
     * Appends a VarHandle array read-modify-write expression.
     *
     * @param armw the array RMW item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayReadModifyWrite(ArrayReadModifyWrite armw, StringBuilder buf,
            SourceBuilder sb) {
        varHandleArrayTarget(buf, armw.array(), armw.index(), sb);
        buf.append('.').append(rmwMethodName(armw.op(), armw.mode())).append('(');
        return expr(buf, armw.value(), sb).append(')');
    }

    /**
     * Appends a VarHandle field compare-and-set expression.
     *
     * @param fcas the field CAS item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprFieldCompareAndSet(FieldCompareAndSet fcas, StringBuilder buf, SourceBuilder sb) {
        FieldDesc fd = ((FieldVarHandleConst) fcas.handle()).field();
        varHandleFieldTarget(buf, fcas.instance(), fd.name(), sb);
        buf.append('.').append(casMethodName(fcas.weak(), fcas.mode())).append('(');
        expr(buf, fcas.expect(), sb).append(", ");
        return expr(buf, fcas.update(), sb).append(')');
    }

    /**
     * Appends a VarHandle static field compare-and-set expression.
     *
     * @param scas the static field CAS item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFieldCompareAndSet(StaticFieldCompareAndSet scas, StringBuilder buf,
            SourceBuilder sb) {
        FieldDesc fd = ((StaticFieldVarHandleConst) scas.handle()).field();
        varHandleStaticTarget(buf, fd.owner(), fd.name(), sb);
        buf.append('.').append(casMethodName(scas.weak(), scas.mode())).append('(');
        expr(buf, scas.expect(), sb).append(", ");
        return expr(buf, scas.update(), sb).append(')');
    }

    /**
     * Appends a VarHandle array compare-and-set expression.
     *
     * @param acas the array CAS item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayCompareAndSet(ArrayCompareAndSet acas, StringBuilder buf, SourceBuilder sb) {
        varHandleArrayTarget(buf, acas.array(), acas.index(), sb);
        buf.append('.').append(casMethodName(acas.weak(), acas.mode())).append('(');
        expr(buf, acas.expect(), sb).append(", ");
        return expr(buf, acas.update(), sb).append(')');
    }

    /**
     * Appends a VarHandle field compare-and-exchange expression.
     *
     * @param fcae the field CAE item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprFieldCompareAndExchange(FieldCompareAndExchange fcae, StringBuilder buf,
            SourceBuilder sb) {
        FieldDesc fd = ((FieldVarHandleConst) fcae.handle()).field();
        varHandleFieldTarget(buf, fcae.instance(), fd.name(), sb);
        buf.append('.').append(caeMethodName(fcae.mode())).append('(');
        expr(buf, fcae.expect(), sb).append(", ");
        return expr(buf, fcae.update(), sb).append(')');
    }

    /**
     * Appends a VarHandle static field compare-and-exchange expression.
     *
     * @param scae the static field CAE item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprStaticFieldCompareAndExchange(StaticFieldCompareAndExchange scae,
            StringBuilder buf, SourceBuilder sb) {
        FieldDesc fd = ((StaticFieldVarHandleConst) scae.handle()).field();
        varHandleStaticTarget(buf, fd.owner(), fd.name(), sb);
        buf.append('.').append(caeMethodName(scae.mode())).append('(');
        expr(buf, scae.expect(), sb).append(", ");
        return expr(buf, scae.update(), sb).append(')');
    }

    /**
     * Appends a VarHandle array compare-and-exchange expression.
     *
     * @param acae the array CAE item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprArrayCompareAndExchange(ArrayCompareAndExchange acae, StringBuilder buf,
            SourceBuilder sb) {
        varHandleArrayTarget(buf, acae.array(), acae.index(), sb);
        buf.append('.').append(caeMethodName(acae.mode())).append('(');
        expr(buf, acae.expect(), sb).append(", ");
        return expr(buf, acae.update(), sb).append(')');
    }

    /**
     * Appends a new array with initializer expression.
     *
     * @param nar the new array result item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprNewArrayResult(NewArrayResult nar, StringBuilder buf, SourceBuilder sb) {
        buf.append("new ").append(typeName(nar.newEmptyArray().type().componentType(), sb)).append("[] { ");
        var elements = nar.elements();
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            expr(buf, elements.get(i).value(), sb);
        }
        return buf.append(" }");
    }

    /**
     * Appends a three-way comparison expression.
     *
     * @param cmp the comparison item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprCmp(Cmp cmp, StringBuilder buf, SourceBuilder sb) {
        buf.append('(');
        expr(buf, cmp.a(), sb);
        buf.append(switch (cmp.kind()) {
            case CMP -> " <=> ";
            case CMPG -> " <=>> ";
            case CMPL -> " <<=> ";
        });
        return expr(buf, cmp.b(), sb).append(')');
    }

    /**
     * Appends a value-producing if expression (ternary or multi-line if-expression).
     *
     * @param ifItem the value-producing if item (must not be {@code null})
     * @param buf the string builder to append to (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder exprIfValue(If ifItem, StringBuilder buf, SourceBuilder sb) {
        if (isTrivialExprBlock(ifItem.whenTrue) && isTrivialExprBlock(ifItem.whenFalse)) {
            // trivial branches: render as ternary
            buf.append('(');
            ifCondition(buf, ifItem, sb);
            buf.append(") ? ");
            expr(buf, yieldValue(ifItem.whenTrue), sb);
            buf.append(" : ");
            return expr(buf, yieldValue(ifItem.whenFalse), sb);
        } else {
            // non-trivial branches: render as if-expression pseudo-syntax
            return ifExprExpr(buf, ifItem, sb);
        }
    }

    // endregion

    // region Statement dispatch methods

    /**
     * Emits a local variable set statement ({@code Type name = value;}).
     *
     * @param lvs the local variable set item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtLocalVarSet(LocalVarSet lvs, SourceBuilder sb) {
        lvs.sourceLine = sb.startLine();
        sb.body().append(typeName(lvs.localVar().type(), sb)).append(' ')
                .append(lvs.localVar().name()).append(" = ");
        expr(sb.body(), lvs.value(), sb).append(';');
        sb.endLine();
        sb.trackItem(lvs);
    }

    /**
     * Emits a parameter set statement ({@code name = value;}).
     *
     * @param ps the parameter set item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtParamSet(ParamSet ps, SourceBuilder sb) {
        ps.sourceLine = sb.startLine();
        expr(sb.body().append(ps.paramVar().name()).append(" = "), ps.value(), sb).append(';');
        sb.endLine();
        sb.trackItem(ps);
    }

    /**
     * Emits a field set statement ({@code instance.field = value;}).
     *
     * @param fs the field set item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtFieldSet(FieldSet fs, SourceBuilder sb) {
        fs.sourceLine = sb.startLine();
        expr(sb.body(), fs.fieldDeref().instance(), sb).append('.').append(fs.fieldDeref().desc().name())
                .append(" = ");
        expr(sb.body(), fs.value(), sb).append(';');
        sb.endLine();
        sb.trackItem(fs);
    }

    /**
     * Emits a static field set statement ({@code Owner.field = value;}).
     *
     * @param sfs the static field set item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtStaticFieldSet(StaticFieldSet sfs, SourceBuilder sb) {
        sfs.sourceLine = sb.startLine();
        sb.body().append(typeName(sfs.staticFieldVar().desc().owner(), sb)).append('.')
                .append(sfs.staticFieldVar().desc().name()).append(" = ");
        expr(sb.body(), sfs.value(), sb).append(';');
        sb.endLine();
        sb.trackItem(sfs);
    }

    /**
     * Emits a return statement.
     *
     * @param ret the return item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtReturn(Return ret, SourceBuilder sb) {
        if (ret.returnValue().isVoid()) {
            ret.sourceLine = sb.line("return;");
        } else {
            ret.sourceLine = sb.startLine();
            expr(sb.body().append("return "), ret.returnValue(), sb).append(';');
            sb.endLine();
        }
        sb.trackItem(ret);
    }

    /**
     * Emits a throw statement.
     *
     * @param t the throw item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtThrow(Throw t, SourceBuilder sb) {
        t.sourceLine = sb.startLine();
        expr(sb.body().append("throw "), t.thrown, sb).append(';');
        sb.endLine();
        sb.trackItem(t);
    }

    /**
     * Emits a pop statement (only if the popped expression is non-void).
     *
     * @param p the pop item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtPop(Pop p, SourceBuilder sb) {
        if (!p.expr().isVoid()) {
            p.sourceLine = sb.startLine();
            expr(sb.body(), p.expr(), sb).append(';');
            sb.endLine();
            sb.trackItem(p);
        }
    }

    /**
     * Emits an array store statement ({@code array[index] = value;}).
     *
     * @param as the array store item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtArrayStore(ArrayStore as, SourceBuilder sb) {
        as.sourceLine = sb.startLine();
        expr(sb.body(), as.arrayExpr(), sb).append('[');
        expr(sb.body(), as.index(), sb).append("] = ");
        expr(sb.body(), as.value(), sb).append(';');
        sb.endLine();
        sb.trackItem(as);
    }

    /**
     * Emits a monitor enter statement.
     *
     * @param me the monitor enter item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtMonitorEnter(MonitorEnter me, SourceBuilder sb) {
        me.sourceLine = sb.startLine();
        expr(sb.body().append("monitorenter("), me.monitor(), sb).append(");");
        sb.endLine();
        sb.trackItem(me);
    }

    /**
     * Emits a monitor exit statement.
     *
     * @param me the monitor exit item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtMonitorExit(MonitorExit me, SourceBuilder sb) {
        me.sourceLine = sb.startLine();
        expr(sb.body().append("monitorexit("), me.monitor(), sb).append(");");
        sb.endLine();
        sb.trackItem(me);
    }

    /**
     * Emits a yield statement (only if the yielded value is non-void).
     *
     * @param y the yield item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtYield(Yield y, SourceBuilder sb) {
        if (!y.value().isVoid()) {
            y.sourceLine = sb.startLine();
            expr(sb.body().append("yield "), (Item) y.value(), sb).append(';');
            sb.endLine();
            sb.trackItem(y);
        }
    }

    /**
     * Emits a local variable increment statement ({@code name += amount;}).
     *
     * @param lvi the local variable increment item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtLocalVarIncrement(LocalVarIncrement lvi, SourceBuilder sb) {
        lvi.sourceLine = sb.startLine();
        sb.body().append(lvi.localVar().name()).append(" += ")
                .append(((IntBasedConst) lvi.amount()).intValue()).append(';');
        sb.endLine();
        sb.trackItem(lvi);
    }

    /**
     * Emits a local variable decrement statement ({@code name -= amount;}).
     *
     * @param lvd the local variable decrement item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void stmtLocalVarDecrement(LocalVarDecrement lvd, SourceBuilder sb) {
        lvd.sourceLine = sb.startLine();
        sb.body().append(lvd.localVar().name()).append(" -= ")
                .append(((IntBasedConst) lvd.amount()).intValue()).append(';');
        sb.endLine();
        sb.trackItem(lvd);
    }

    // endregion

    // endregion

    // region Statement rendering

    /**
     * Emits a source line for a statement item and sets its {@code sourceLine}.
     *
     * @param item the statement item (must not be {@code null})
     * @param sb the source builder to emit lines into (must not be {@code null})
     */
    public static void statement(Item item, SourceBuilder sb) {
        item.appendSourceStatement(sb);
    }

    /**
     * Appends the condition expression for an if-statement to the buffer.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param ifItem the if item (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder ifCondition(StringBuilder buf, If ifItem, SourceBuilder sb) {
        if (ifItem instanceof IfZero iz) {
            Item a = iz.a;
            if (a.typeKind() == io.quarkus.gizmo2.TypeKind.REFERENCE) {
                return switch (iz.kind) {
                    case EQ -> expr(buf, a, sb).append(" == null");
                    case NE -> expr(buf, a, sb).append(" != null");
                    default -> expr(buf, a, sb).append(' ').append(relSymbol(iz.kind)).append(" null");
                };
            }
            if (iz.kind == If.Kind.NE) {
                return expr(buf, a, sb);
            }
            if (iz.kind == If.Kind.EQ) {
                buf.append('!');
                return expr(buf, a, sb);
            }
            return expr(buf, a, sb).append(' ').append(relSymbol(iz.kind)).append(" 0");
        }
        if (ifItem instanceof IfRel ir) {
            expr(buf, ir.a, sb).append(' ').append(relSymbol(ir.kind)).append(' ');
            return expr(buf, ir.b, sb);
        }
        return buf.append("/* unknown condition */");
    }

    /**
     * Emits an if/if-else statement structure.
     *
     * @param ifItem the if item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void emitIf(If ifItem, SourceBuilder sb) {
        if (ifItem.whenTrue != null && ifItem.whenFalse != null) {
            // if-else
            ifItem.sourceLine = sb.startLine();
            addBlockLabelSlots(sb, ifItem.whenTrue, ifItem.whenFalse);
            sb.body().append("if (");
            ifCondition(sb.body(), ifItem, sb);
            sb.body().append(") {");
            sb.endLine();
            sb.trackItem(ifItem);
            sb.indent();
            generateBlock(ifItem.whenTrue, sb);
            sb.dedent();
            sb.line("} else {");
            sb.indent();
            generateBlock(ifItem.whenFalse, sb);
            sb.dedent();
            sb.line("}");
        } else if (ifItem.whenTrue != null) {
            // if only
            ifItem.sourceLine = sb.startLine();
            addBlockLabelSlots(sb, ifItem.whenTrue);
            sb.body().append("if (");
            ifCondition(sb.body(), ifItem, sb);
            sb.body().append(") {");
            sb.endLine();
            sb.trackItem(ifItem);
            sb.indent();
            generateBlock(ifItem.whenTrue, sb);
            sb.dedent();
            sb.line("}");
        } else if (ifItem.whenFalse != null) {
            // if-not
            ifItem.sourceLine = sb.startLine();
            addBlockLabelSlots(sb, ifItem.whenFalse);
            sb.body().append("if (!(");
            ifCondition(sb.body(), ifItem, sb);
            sb.body().append(")) {");
            sb.endLine();
            sb.trackItem(ifItem);
            sb.indent();
            generateBlock(ifItem.whenFalse, sb);
            sb.dedent();
            sb.line("}");
        }
    }

    /**
     * Registers deferred label slots for blocks that are potential jump targets.
     *
     * @param sb the source builder
     * @param blocks the blocks to check
     */
    private static void addBlockLabelSlots(SourceBuilder sb, BlockCreatorImpl... blocks) {
        for (BlockCreatorImpl block : blocks) {
            if (block.isBreakTarget() || block.isBranchTarget()) {
                sb.addLabelSlot(block);
            }
        }
    }

    /**
     * Emits a try-catch statement structure.
     *
     * @param tc the try-catch item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void emitTryCatch(TryCatch tc, SourceBuilder sb) {
        tc.sourceLine = sb.startLine();
        addBlockLabelSlots(sb, tc.body());
        sb.body().append("try {");
        sb.endLine();
        sb.trackItem(tc);
        sb.indent();
        generateBlock(tc.body(), sb);
        sb.dedent();
        for (TryCatch.Catch c : tc.catches()) {
            // extract variable name from the first LocalVarSet in the catch body, if present
            String varName = "e";
            for (Item bodyItem : c.body().items()) {
                if (bodyItem instanceof LocalVarSet lvs) {
                    varName = lvs.localVar().name();
                    break;
                }
            }
            // build catch line with multi-catch type list directly into body
            sb.startLine();
            sb.body().append("} catch (");
            boolean first = true;
            for (ClassDesc type : c.types()) {
                if (!first) {
                    sb.body().append(" | ");
                }
                sb.body().append(typeName(type, sb));
                first = false;
            }
            sb.body().append(' ').append(varName).append(") {");
            sb.endLine();
            sb.indent();
            generateBlock(c.body(), sb);
            sb.dedent();
        }
        sb.line("}");
    }

    /**
     * Emits a try-finally statement structure.
     *
     * @param tf the try-finally item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void emitTryFinally(TryFinally tf, SourceBuilder sb) {
        tf.sourceLine = sb.startLine();
        addBlockLabelSlots(sb, tf.body());
        sb.body().append("try {");
        sb.endLine();
        sb.trackItem(tf);
        sb.indent();
        generateBlock(tf.body(), sb);
        sb.dedent();
        sb.line("} finally {");
        sb.indent();
        generateBlock(tf.cleanupTemplate, sb);
        sb.dedent();
        sb.line("}");
    }

    /**
     * Emits a switch statement or expression structure.
     *
     * @param sw the switch item (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void emitSwitch(SwitchCreatorImpl<?> sw, SourceBuilder sb) {
        sb.pushSwitch(sw);
        try {
            sw.sourceLine = sb.startLine();
            if (sw.jumpTarget) {
                sb.addLabelSlot(sw);
            }
            sb.body().append("switch (");
            expr(sb.body(), (Item) sw.switchVal(), sb);
            sb.body().append(") {");
            sb.endLine();
            sb.trackItem(sw);
            sb.indent();
            for (SwitchCreatorImpl<?>.CaseCreatorImpl c : sw.cases) {
                sb.startLine();
                sb.body().append("case ");
                boolean first = true;
                for (var entry : sw.casesByConstant.entrySet()) {
                    if (entry.getValue() == c) {
                        if (!first) {
                            sb.body().append(", ");
                        }
                        expr(sb.body(), entry.getKey(), sb);
                        first = false;
                    }
                }
                sb.body().append(" -> {");
                sb.endLine();
                sb.indent();
                generateBlock(c.body(), sb);
                sb.dedent();
                sb.line("}");
            }
            if (sw.default_ != null) {
                sb.startLine();
                sb.body().append("default -> {");
                sb.endLine();
                sb.indent();
                generateBlock(sw.default_, sb);
                sb.dedent();
                sb.line("}");
            }
            sb.dedent();
            sb.line("}");
        } finally {
            sb.popSwitch();
        }
    }

    // endregion

    // region Generic type rendering helpers

    /**
     * Appends a type parameter list (e.g., {@code <T, U extends Comparable<U>>}) to the buffer.
     * If the parameter list is empty, nothing is appended.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param params the type parameters (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     */
    public static void appendTypeParameters(StringBuilder buf, List<TypeParameter> params, SourceBuilder sb) {
        if (!params.isEmpty()) {
            Function<ClassDesc, String> fn = typeNameFn(sb);
            buf.append('<');
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                params.get(i).toString(buf, fn);
            }
            buf.append("> ");
        }
    }

    /**
     * Appends a generic type representation to the buffer, resolving class names through the import tracker.
     *
     * @param buf the string builder to append to (must not be {@code null})
     * @param type the generic type (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     */
    public static void appendGenericType(StringBuilder buf, GenericType type, SourceBuilder sb) {
        type.toString(buf, typeNameFn(sb));
    }

    // endregion

    // region Field declaration generation

    /**
     * Generates source lines for a field declaration, including any annotations.
     *
     * @param sb the source builder (must not be {@code null})
     * @param field the field creator (must not be {@code null})
     */
    public static void generateFieldDeclaration(SourceBuilder sb, FieldCreatorImpl field) {
        emitAnnotations(sb, field.visible(), field.invisible());
        sb.startLine();
        appendAccessFlags(sb.body(), field.modifiers);
        if (field.hasGenericType() && !field.genericType().isRaw()) {
            appendGenericType(sb.body(), field.genericType(), sb);
        } else {
            sb.body().append(typeName(field.type(), sb));
        }
        sb.body().append(' ').append(field.name());
        if (field instanceof StaticFieldCreatorImpl s) {
            Const initial = s.initial();
            if (initial != null) {
                sb.body().append(" = ");
                expr(sb.body(), (Item) initial, sb);
            }
        }
        sb.body().append(';');
        sb.endLine();
    }

    // endregion

    // region Block and method body generation

    /**
     * Generates source for all statement items in a block's item list.
     * After the forward pass, performs backward propagation of {@code sourceLine}
     * values from statement items to their preceding dependency items.
     *
     * @param block the block whose items to render (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    public static void generateBlock(BlockCreatorImpl block, SourceBuilder sb) {
        sb.pushBlock(block);
        try {
            ArrayList<Item> items = block.items();
            // forward pass: emit statements and render naked blocks
            for (Item item : items) {
                if (item.isSourceStatement()) {
                    statement(item, sb);
                } else if (item instanceof BlockCreatorImpl bci && bci.isVoid()) {
                    emitNakedBlock(bci, sb);
                }
            }
            // backward propagation: assign sourceLine from statements to their dependency items
            int lastLine = -1;
            for (int i = items.size() - 1; i >= 0; i--) {
                Item item = items.get(i);
                if (item.sourceLine >= 0) {
                    lastLine = item.sourceLine;
                } else if (lastLine >= 0 && !(item instanceof Nop) && !(item instanceof BlockHeader)
                        && !(item instanceof LocalVarAllocator)) {
                    item.sourceLine = lastLine;
                    sb.trackItem(item);
                }
            }
        } finally {
            sb.popBlock();
        }
    }

    /**
     * Emits a naked block (from {@code block()} or {@code loop()}) with a
     * deferred label slot if the block is a jump target.
     *
     * @param block the block to render (must not be {@code null})
     * @param sb the source builder (must not be {@code null})
     */
    private static void emitNakedBlock(BlockCreatorImpl block, SourceBuilder sb) {
        block.sourceLine = sb.startLine();
        if (block.isBreakTarget() || block.isBranchTarget()) {
            sb.addLabelSlot(block);
        }
        sb.body().append('{');
        sb.endLine();
        sb.trackItem(block);
        sb.indent();
        generateBlock(block, sb);
        sb.dedent();
        sb.line("}");
    }

    /**
     * Generates the source text for a method or constructor body, including
     * the method signature line.
     *
     * @param sb the source builder (must not be {@code null})
     * @param exec the executable creator (method/constructor) (must not be {@code null})
     * @param body the block containing the method body items (must not be {@code null})
     */
    public static void generateMethodBody(SourceBuilder sb, ExecutableCreatorImpl exec, BlockCreatorImpl body) {
        sb.resetLabels();
        sb.blankLine();
        emitAnnotations(sb, exec.visible(), exec.invisible());
        sb.startLine();
        methodSignature(sb.body(), exec, sb);
        sb.body().append(" {");
        sb.endLine();
        sb.indent();
        generateBlock(body, sb);
        sb.dedent();
        sb.line("}");
        sb.processLabelSlots();
    }

    /**
     * Generates the source text for a static initializer block ({@code static { ... }}).
     *
     * @param sb the source builder (must not be {@code null})
     * @param body the block containing the static initializer items (must not be {@code null})
     */
    public static void generateStaticInitializer(SourceBuilder sb, BlockCreatorImpl body) {
        sb.resetLabels();
        sb.blankLine();
        sb.line("static {");
        sb.indent();
        generateBlock(body, sb);
        sb.dedent();
        sb.line("}");
        sb.processLabelSlots();
    }

    /**
     * Appends the method/constructor signature to the given buffer.
     *
     * @param sig the string builder to append to (must not be {@code null})
     * @param exec the executable creator (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     * @return the buffer, for chaining
     */
    public static StringBuilder methodSignature(StringBuilder sig, ExecutableCreatorImpl exec, SourceBuilder sb) {
        // access modifiers
        appendAccessFlags(sig, exec.modifiers);
        // type parameters
        appendTypeParameters(sig, exec.typeParameters(), sb);
        if (exec instanceof MethodCreatorImpl mc) {
            // return type and method name
            if (mc.hasGenericReturnType() && !mc.genericReturnType().isRaw()) {
                appendGenericType(sig, mc.genericReturnType(), sb);
            } else {
                sig.append(typeName(mc.returnType(), sb));
            }
            sig.append(' ').append(mc.name());
        } else if (exec instanceof ConstructorCreatorImpl) {
            // constructor: use simple class name
            sig.append(simpleClassName(exec.typeCreator.type()));
        } else {
            sig.append("/* unknown */");
        }
        // parameters
        sig.append('(');
        List<ParamVarImpl> params = exec.params;
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sig.append(", ");
            }
            ParamVarImpl p = params.get(i);
            appendInlineAnnotations(sig, p.visible, p.invisible, sb);
            if (p.hasGenericType() && !p.genericType().isRaw()) {
                appendGenericType(sig, p.genericType(), sb);
            } else {
                sig.append(typeName(p.type(), sb));
            }
            sig.append(' ').append(p.name());
        }
        return sig.append(')');
    }

    /**
     * Appends Java access modifier keywords to a string builder.
     *
     * @param sb the string builder (must not be {@code null})
     * @param flags the access flags bitmask
     */
    public static void appendAccessFlags(StringBuilder sb, int flags) {
        if ((flags & 0x0001) != 0) {
            sb.append("public ");
        }
        if ((flags & 0x0002) != 0) {
            sb.append("private ");
        }
        if ((flags & 0x0004) != 0) {
            sb.append("protected ");
        }
        if ((flags & 0x0008) != 0) {
            sb.append("static ");
        }
        if ((flags & 0x0010) != 0) {
            sb.append("final ");
        }
        if ((flags & 0x0020) != 0) {
            sb.append("synchronized ");
        }
        if ((flags & 0x0100) != 0) {
            sb.append("native ");
        }
        if ((flags & 0x0400) != 0) {
            sb.append("abstract ");
        }
    }

    /**
     * Emits annotation lines to a source builder for the given visible and invisible annotation lists.
     * Each annotation is rendered on its own line at the current indentation level.
     * Type names within annotations are resolved through the source builder's import tracker.
     *
     * @param sb the source builder (must not be {@code null})
     * @param visible the runtime-visible annotations (must not be {@code null})
     * @param invisible the runtime-invisible annotations (must not be {@code null})
     */
    public static void emitAnnotations(SourceBuilder sb, List<Annotation> visible, List<Annotation> invisible) {
        Function<ClassDesc, String> fn = typeNameFn(sb);
        for (Annotation a : visible) {
            sb.startLine();
            Util.appendAnnotation(sb.body(), a, fn);
            sb.endLine();
        }
        for (Annotation a : invisible) {
            sb.startLine();
            Util.appendAnnotation(sb.body(), a, fn);
            sb.endLine();
        }
    }

    /**
     * Appends inline annotation text for a parameter to a string builder.
     * Annotations are separated by spaces and followed by a trailing space if any were rendered.
     * Type names within annotations are resolved through the source builder's import tracker.
     *
     * @param sig the string builder (must not be {@code null})
     * @param visible the runtime-visible annotations (must not be {@code null})
     * @param invisible the runtime-invisible annotations (must not be {@code null})
     * @param sb the source builder for import tracking (must not be {@code null})
     */
    public static void appendInlineAnnotations(StringBuilder sig, List<Annotation> visible,
            List<Annotation> invisible, SourceBuilder sb) {
        Function<ClassDesc, String> fn = typeNameFn(sb);
        for (Annotation a : visible) {
            Util.appendAnnotation(sig, a, fn);
            sig.append(' ');
        }
        for (Annotation a : invisible) {
            Util.appendAnnotation(sig, a, fn);
            sig.append(' ');
        }
    }

    /**
     * {@return the simple class name from a class descriptor}
     *
     * @param desc the class descriptor (must not be {@code null})
     */
    public static String simpleClassName(ClassDesc desc) {
        String full = typeName(desc);
        int dot = full.lastIndexOf('.');
        return dot >= 0 ? full.substring(dot + 1) : full;
    }

    /**
     * Determines whether a block is trivially expressible — that is,
     * the only statement-classified item in the block is the final {@link Yield}.
     * All other items are expression-only items that can be inlined by {@link #expr}.
     *
     * @param block the block to check (must not be {@code null})
     * @return {@code true} if the block is trivially expressible
     */
    public static boolean isTrivialExprBlock(BlockCreatorImpl block) {
        ArrayList<Item> items = block.items();
        int stmtCount = 0;
        for (Item item : items) {
            if (item.isSourceStatement()) {
                if (++stmtCount > 1) {
                    return false;
                }
            }
        }
        return stmtCount == 1;
    }

    /**
     * Extracts the yielded value from a block.
     * The block's last item must be a {@link Yield}.
     *
     * @param block the block to extract from (must not be {@code null})
     * @return the yielded value item (not {@code null})
     */
    public static Item yieldValue(BlockCreatorImpl block) {
        ArrayList<Item> items = block.items();
        Item last = items.get(items.size() - 1);
        return (Item) ((Yield) last).value();
    }

    // endregion
}
