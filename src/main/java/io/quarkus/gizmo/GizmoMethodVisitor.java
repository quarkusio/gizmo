package io.quarkus.gizmo;

import java.util.IdentityHashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public final class GizmoMethodVisitor extends MethodVisitor {
    private final GizmoClassVisitor cv;
    private final IdentityHashMap<Label, String> labelNames = new IdentityHashMap<>();
    int labelCnt = 1;

    public GizmoMethodVisitor(final int api, final MethodVisitor delegate, final GizmoClassVisitor cv) {
        super(api, delegate);
        this.cv = cv;
    }

    public boolean nameLabel(Label label, String name) {
        return labelNames.putIfAbsent(label, name) == null;
    }

    String getNameOf(Label label) {
        String str = labelNames.get(label);
        if (str == null) {
            str = "label" + (labelCnt ++);
            labelNames.put(label, str);
        }
        return str;
    }

    public void visitGizmoNode(StackTraceElement element, String opName) {
        checkMethod();
        cv.append("// ").append(opName).newLine();
        if (element != null) cv.append("// at ").append(element.toString()).newLine();
    }

    public void visitInsn(final int opcode) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitInsn(opcode);
    }

    public void visitIntInsn(final int opcode, final int operand) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).append(' ').append(operand).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitIntInsn(opcode, operand);
    }

    public void visitVarInsn(final int opcode, final int varNum) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).append(' ').append(varNum).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitVarInsn(opcode, varNum);
    }

    public void visitTypeInsn(final int opcode, final String type) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).append(' ').append(type).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitTypeInsn(opcode, type);
    }

    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        checkMethod();
        cv.append("// Field descriptor: ").append(descriptor).newLine();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).append(' ').append(owner).append('#').append(name).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor) {
        checkMethod();
        cv.append("// Method descriptor: ").append(descriptor).newLine();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).append(' ').append(owner).append('#').append(name).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitMethodInsn(opcode, owner, name, descriptor);
    }

    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        checkMethod();
        cv.append("// Method descriptor: ").append(descriptor).newLine();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).append(' ').append(owner).append('#').append(name).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    public void visitJumpInsn(final int opcode, final Label target) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(opcode)).append(' ').append(getNameOf(target)).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitJumpInsn(opcode, target);
    }

    public void visitLabel(final Label label) {
        cv.append("** ").append(getNameOf(label)).newLine();
        super.visitLabel(label);
    }

    public void visitLdcInsn(final Object value) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(Opcodes.LDC)).append(" (").append(value.getClass().getSimpleName()).append(") ");
        if (value instanceof String) {
            cv.append('"').append(value).append('"');
        } else {
            cv.append(value);
        }
        cv.newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitLdcInsn(value);
    }

    public void visitIincInsn(final int var, final int increment) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(Opcodes.IINC)).append(' ').append(var).append(' ').append(increment > 0 ? '+' : '-').append(increment).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitIincInsn(var, increment);
    }

    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(Opcodes.TABLESWITCH)).newLine();
        for (int i = 0; i < max - min; i++) {
            cv.append("  [").append(min + i).append("]: goto ").append(getNameOf(labels[i])).newLine();
        }
        cv.append("  default: goto ").append(getNameOf(dflt)).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        checkMethod();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(Opcodes.TABLESWITCH)).newLine();
        for (int i = 0; i < labels.length; i++) {
            cv.append("  [").append(keys[i]).append("]: goto ").append(getNameOf(labels[i])).newLine();
        }
        cv.append("  default: goto ").append(getNameOf(dflt)).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
        checkMethod();
        cv.append("// Array descriptor: ").append(descriptor).newLine();
        final int lineNumber = cv.getLineNumber();
        cv.append(getOpString(Opcodes.MULTIANEWARRAY)).append(' ').append(numDimensions).newLine();
        final Label label = new Label();
        super.visitLabel(label);
        super.visitLineNumber(lineNumber, label);
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        checkMethod();
        cv.append("// Try from ").append(getNameOf(start)).append(" to ").append(getNameOf(end)).newLine();
        cv.append("// Catch ").append(type).append(" by going to ").append(getNameOf(handler)).newLine();
        super.visitTryCatchBlock(start, end, handler, type);
    }

    public void visitEnd() {
        cv.methodVisitEnd();
        super.visitEnd();
    }

    void checkMethod() {
        if (cv.getCurrentMethod() != this) {
            throw new IllegalStateException("Wrong method is active");
        }
    }

    private static String getOpString(final int opcode) {
        return Printer.OPCODES[opcode];
    }
}
