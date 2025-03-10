package io.quarkus.gizmo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class MethodCheckerVisitor extends MethodVisitor {
    private boolean isReturningValue;
    private boolean isSuperOrThisCalled;

    public MethodCheckerVisitor() {
        this(Gizmo.ASM_API_VERSION);
    }
    public MethodCheckerVisitor(int api) {
        super(api);
        isReturningValue = false;
        isSuperOrThisCalled = false;
    }


    public void check(MethodCreatorImpl methodCreator) {
        visitLabel(methodCreator.getTop());
        for (BytecodeCreatorImpl.Operation op : methodCreator.operations) {
            op.writeBytecode(this);
        }
        visitLabel(methodCreator.getBottom());
    }

    @Override
    public void visitInsn(int opcode) {
        // if returnValue or ThrowException it is valid method.
        switch (opcode) {
            case Opcodes.RETURN:
            case Opcodes.IRETURN:
            case Opcodes.LRETURN:
            case Opcodes.FRETURN:
            case Opcodes.DRETURN:
            case Opcodes.ARETURN:
            case Opcodes.ATHROW:
                isReturningValue = true;
                break;
        }
    }

    @Override
    public void visitParameter(String name, int access) {
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new DummyVisitor();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new DummyVisitor();
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DummyVisitor();
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return new DummyVisitor();
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitCode() {
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
            isSuperOrThisCalled = true;
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>") && !isInterface) {
            isSuperOrThisCalled = true;
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        // if
    }

    @Override
    public void visitLabel(Label label) {
    }

    @Override
    public void visitLdcInsn(Object value) {
    }

    @Override
    public void visitIincInsn(int var, int increment) {
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DummyVisitor();
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DummyVisitor();
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        return new DummyVisitor();
    }

    @Override
    public void visitLineNumber(int line, Label start) {
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
    }

    @Override
    public void visitEnd() {
    }

    public boolean isReturningValue() {
        return isReturningValue;
    }

    public boolean isSuperOrThisCalled() {
        return isSuperOrThisCalled;
    }

    class DummyVisitor extends AnnotationVisitor {
        public DummyVisitor() {
            super(Gizmo.ASM_API_VERSION);
        }

        @Override
        public void visit(String name, Object value) {
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String descriptor) {
            return this;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return this;
        }

        @Override
        public void visitEnd() {
        }
    }
}
