package io.quarkus.gizmo;

import java.io.IOException;
import java.io.Writer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 *
 */
public final class GizmoClassVisitor extends ClassVisitor {
    private String name;
    private LineNumberWriter writer;
    private GizmoMethodVisitor currentMethod;
    private int indent = 0;

    public GizmoClassVisitor(final int api, final ClassVisitor classVisitor, final Writer zigWriter) {
        super(api, classVisitor);
        this.writer = new LineNumberWriter(zigWriter);
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name;
        append("// Class: ").append(name).newLine();
        if (superName != null) append("//     Extends: ").append(superName).newLine();
        if (interfaces != null && interfaces.length > 0) {
            append("//     Implements:").newLine();
            for (String iName : interfaces) {
                append("//         ").append(iName).newLine();
            }
        }
        newLine();
        append("// DO NOT MODIFY.  This is not actually a source file; it is a textual representation of generated code.");
        newLine();
        append("// Use only for debugging purposes.");
        newLine().newLine();
    }

    public void visitSource(final String source, final String debug) {
        final int idx = name.lastIndexOf('/');
        final String fileName;
        if (idx == -1) {
            fileName = name + ".zig";
        } else {
            fileName = name.substring(idx + 1) + ".zig";
        }
        super.visitSource(fileName, null);
    }

    public GizmoMethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        if (currentMethod != null) {
            throw new IllegalStateException("Multiple active method visitors");
        }
        final MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
        final GizmoMethodVisitor zigMethodVisitor = new GizmoMethodVisitor(api, delegate, this);
        currentMethod = zigMethodVisitor;
        final Type returnType = Type.getReturnType(descriptor);
        append("Method ").append(name).append(" : ").append(returnType);
        if (exceptions != null && exceptions.length > 0) {
            addIndent().newLine().append("throws ");
            append(exceptions[0]);
            for (int i = 1; i < exceptions.length; i++) {
                append(", ").append(exceptions[i]);
            }
            removeIndent();
        }
        newLine().append("(").addIndent().newLine();
        final Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        if (argumentTypes.length > 0) {
            int base = ((access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
            append("arg ").append(base).append(" = ").append(argumentTypes[0]);
            for (int i = 1; i < argumentTypes.length; i ++) {
                append(',').newLine();
                append("arg ").append(base + i).append(" = ").append(argumentTypes[i]);
            }
        } else {
            append("// (no arguments)");
        }
        removeIndent().newLine().append(") {").addIndent().newLine();
        return zigMethodVisitor;
    }

    public void visitEnd() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        super.visitEnd();
    }

    public LineNumberWriter getWriter() {
        return writer;
    }

    int getLineNumber() {
        return writer.getLineNumber();
    }

    GizmoClassVisitor append(int arg) {
        return append(Integer.toString(arg));
    }

    GizmoClassVisitor append(long arg) {
        return append(Long.toString(arg));
    }

    GizmoClassVisitor append(char c) {
        try {
            writer.write(c);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    GizmoClassVisitor append(Object obj) {
        return append(String.valueOf(obj));
    }

    GizmoClassVisitor append(String str) {
        try {
            writer.write(str);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    GizmoClassVisitor newLine() {
        try {
            writer.write(System.lineSeparator());
            for (int i = 0; i < indent; i ++) {
                writer.write("    ");
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    GizmoClassVisitor addIndent() {
        indent++;
        return this;
    }

    GizmoClassVisitor removeIndent() {
        indent--;
        return this;
    }

    public void methodVisitEnd() {
        currentMethod = null;
        removeIndent().newLine().append('}').newLine().newLine();
    }

    GizmoMethodVisitor getCurrentMethod() {
        return currentMethod;
    }
}
