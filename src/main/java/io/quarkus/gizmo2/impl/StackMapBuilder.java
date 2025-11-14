package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.attribute.StackMapFrameInfo;
import io.smallrye.common.constraint.Assert;

/**
 * A builder for conservative stack maps.
 */
public final class StackMapBuilder {
    private final ArrayList<StackMapFrameInfo.VerificationTypeInfo> stack = new ArrayList<>();
    private final ArrayList<StackMapFrameInfo.VerificationTypeInfo> locals = new ArrayList<>();
    private final ArrayList<StackMapFrameInfo> frameInfos = new ArrayList<>();
    private boolean wroteCode;
    private final HashMap<List<StackMapFrameInfo.VerificationTypeInfo>, List<StackMapFrameInfo.VerificationTypeInfo>> lists = new HashMap<>();
    private final HashMap<String, StackMapFrameInfo.ObjectVerificationTypeInfo> vtiCache = new HashMap<>();

    /**
     * Construct a new instance.
     */
    public StackMapBuilder() {
    }

    /**
     * {@return a saved state snapshot (not {@code null})}
     */
    public Saved save() {
        return new Saved(stack.toArray(StackMapFrameInfo.VerificationTypeInfo[]::new),
                locals.toArray(StackMapFrameInfo.VerificationTypeInfo[]::new));
    }

    /**
     * Restore a saved state.
     *
     * @param state the saved state (must not be {@code null})
     */
    public void restore(Saved state) {
        stack.clear();
        Collections.addAll(stack, state.stack);
        locals.clear();
        Collections.addAll(locals, state.locals);
    }

    /**
     * Push a value type onto the logical stack.
     * "Class 2" values ({@code long} and {@code double}) will occupy two stack slots per the JVMS.
     * Call after generating an instruction that pushes a value on to the stack.
     *
     * @param vti the type information to push (must not be {@code null})
     */
    public void push(StackMapFrameInfo.VerificationTypeInfo vti) {
        if (isClass2(vti)) {
            stack.add(StackMapFrameInfo.SimpleVerificationTypeInfo.TOP);
        }
        stack.add(vti);
    }

    /**
     * Push a value type onto the logical stack.
     * "Class 2" values ({@code long} and {@code double}) will occupy two stack slots per the JVMS.
     * Call after generating an instruction that pushes a value on to the stack.
     *
     * @param type the type to push (must not be {@code null})
     */
    public void push(final ClassDesc type) {
        push(verificationTypeOf(type));
    }

    /**
     * Clear the stack.
     */
    public void clearStack() {
        stack.clear();
    }

    /**
     * Pop a value type from the logical stack.
     * "Class 2" values ({@code long} and {@code double}) will occupy two stack slots per the JVMS.
     * Call after generating an instruction that pops a value from the stack.
     *
     * @return the type information that was popped (not {@code null})
     */
    public StackMapFrameInfo.VerificationTypeInfo pop() {
        try {
            StackMapFrameInfo.VerificationTypeInfo result = stack.remove(stack.size() - 1);
            if (isClass2(result)) {
                StackMapFrameInfo.VerificationTypeInfo top = stack.remove(stack.size() - 1);
                assert top == StackMapFrameInfo.SimpleVerificationTypeInfo.TOP;
            }
            return result;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("Stack underflow");
        }
    }

    /**
     * {@return the current verification type information for the given variable index}
     */
    public StackMapFrameInfo.VerificationTypeInfo load(int localIdx) {
        return locals.get(localIdx);
    }

    /**
     * Set the value type for a local variable.
     *
     * @param localIdx the local variable index
     * @param vti the type information (must not be {@code null})
     */
    public void store(int localIdx, StackMapFrameInfo.VerificationTypeInfo vti) {
        if (isClass2(vti)) {
            growLocals(localIdx + 1);
            locals.set(localIdx, vti);
            locals.set(localIdx + 1, StackMapFrameInfo.SimpleVerificationTypeInfo.TOP);
        } else {
            growLocals(localIdx);
            locals.set(localIdx, vti);
        }
    }

    /**
     * Set the value type for a local variable.
     *
     * @param localIdx the local variable index
     * @param type the type (must not be {@code null})
     */
    public void store(final int localIdx, final ClassDesc type) {
        store(localIdx, verificationTypeOf(type));
    }

    /**
     * Add a stack map frame to the method.
     * If a stack map frame has already been added since the last bytecode was written,
     * then a duplicate will not be written.
     *
     * @param cb the code builder (must not be {@code null})
     */
    public void addFrameInfo(CodeBuilder cb) {
        if (wroteCode) {
            wroteCode = false;
            frameInfos.add(StackMapFrameInfo.of(cb.newBoundLabel(), snapshotLocals(), snapshotStack()));
        } else {
            if (!frameInfos.isEmpty()) {
                frameInfos.set(frameInfos.size() - 1,
                        StackMapFrameInfo.of(cb.newBoundLabel(), snapshotLocals(), snapshotStack()));
            }
        }
    }

    /**
     * Indicate that bytecodes were written.
     * If this method is not called after writing bytecodes,
     * then stack map entries may be missing from the generated method.
     */
    public void wroteCode() {
        wroteCode = true;
    }

    private static boolean isClass2(StackMapFrameInfo.VerificationTypeInfo vti) {
        return vti == StackMapFrameInfo.SimpleVerificationTypeInfo.LONG ||
                vti == StackMapFrameInfo.SimpleVerificationTypeInfo.DOUBLE;
    }

    private void growLocals(int idx) {
        locals.ensureCapacity(idx + 1);
        while (locals.size() <= idx) {
            locals.add(StackMapFrameInfo.SimpleVerificationTypeInfo.TOP);
        }
    }

    /**
     * {@return a snapshot of the current local variable types}
     * All trailing local variables of type {@code TOP} are left off of the snapshot.
     */
    private List<StackMapFrameInfo.VerificationTypeInfo> snapshotLocals() {
        int end = locals.size();
        while (end > 0 && locals.get(end - 1) == StackMapFrameInfo.SimpleVerificationTypeInfo.TOP) {
            end--;
        }
        if (end == 0) {
            return List.of();
        } else if (end == 1) {
            return cachedList(List.of(locals.get(0)));
        }
        ArrayList<StackMapFrameInfo.VerificationTypeInfo> result = new ArrayList<>(end);
        for (int i = 0; i < end; i++) {
            StackMapFrameInfo.VerificationTypeInfo vti = locals.get(i);
            result.add(vti);
            if (isClass2(vti)) {
                i++;
            }
        }
        return cachedList(result);
    }

    /**
     * {@return a snapshot of the current stack types}
     */
    private List<StackMapFrameInfo.VerificationTypeInfo> snapshotStack() {
        int sz = stack.size();
        if (sz == 0) {
            return List.of();
        }
        ArrayList<StackMapFrameInfo.VerificationTypeInfo> result = new ArrayList<>(sz);
        for (int i = sz - 1; i >= 0; i--) {
            StackMapFrameInfo.VerificationTypeInfo vti = stack.get(i);
            result.add(vti);
            if (isClass2(vti)) {
                i--;
            }
        }
        Collections.reverse(result);
        return cachedList(result);
    }

    private List<StackMapFrameInfo.VerificationTypeInfo> cachedList(final List<StackMapFrameInfo.VerificationTypeInfo> result) {
        List<StackMapFrameInfo.VerificationTypeInfo> list = lists.get(result);
        if (list == null) {
            list = List.copyOf(result);
            lists.put(list, list);
        }
        return list;
    }

    List<StackMapFrameInfo> frameInfos() {
        return frameInfos;
    }

    private StackMapFrameInfo.VerificationTypeInfo verificationTypeOf(final ClassDesc type) {
        String ds = type.descriptorString();
        return switch (ds.charAt(0)) {
            case 'I', 'Z', 'S', 'C', 'B' -> StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER;
            case 'J' -> StackMapFrameInfo.SimpleVerificationTypeInfo.LONG;
            case 'F' -> StackMapFrameInfo.SimpleVerificationTypeInfo.FLOAT;
            case 'D' -> StackMapFrameInfo.SimpleVerificationTypeInfo.DOUBLE;
            case 'L', '[' -> {
                StackMapFrameInfo.ObjectVerificationTypeInfo vti = vtiCache.get(ds);
                if (vti == null) {
                    vti = StackMapFrameInfo.ObjectVerificationTypeInfo.of(type);
                    vtiCache.put(ds, vti);
                }
                yield vti;
            }
            default -> throw Assert.impossibleSwitchCase(ds);
        };
    }

    /**
     * The saved stack map state.
     */
    @SuppressWarnings("ClassCanBeRecord")
    public static final class Saved {
        private final StackMapFrameInfo.VerificationTypeInfo[] stack;
        private final StackMapFrameInfo.VerificationTypeInfo[] locals;

        Saved(final StackMapFrameInfo.VerificationTypeInfo[] stack, final StackMapFrameInfo.VerificationTypeInfo[] locals) {
            this.stack = stack;
            this.locals = locals;
        }
    }
}
