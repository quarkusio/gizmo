package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassFile;
import io.github.dmlloyd.classfile.ClassModel;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;

public final class ClassOutputImpl implements ClassOutput {
    private final GizmoImpl gizmo;
    private final BiConsumer<ClassDesc, byte[]> outputHandler;

    ClassOutputImpl(final GizmoImpl gizmo, final BiConsumer<ClassDesc, byte[]> outputHandler) {
        this.gizmo = gizmo;
        this.outputHandler = outputHandler;
    }

    public BiConsumer<ClassDesc, byte[]> outputHandler() {
        return outputHandler;
    }

    public ClassDesc class_(final ClassDesc desc, final Consumer<ClassCreator> builder) {
        if (! desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Descriptor must describe a valid class");
        }

        ClassFile cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        byte[] bytes = cf.build(desc, zb -> {
            zb.withVersion(ClassFile.JAVA_17_VERSION, 0);
            ClassCreatorImpl tc = new ClassCreatorImpl(desc, this, zb);
            gizmo.do_(tc, cc -> {
                cc.preAccept();
                builder.accept(cc);
                cc.postAccept();
            });
        });
        ClassModel cm = cf.parse(bytes);
        List<VerifyError> result = cf.verify(cm);
        if (! result.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Class failed validation" + cm.toDebugString());
            result.forEach(e::addSuppressed);
            throw e;
        }
        outputHandler.accept(desc, bytes);
        return desc;
    }

    public ClassDesc interface_(final ClassDesc desc, final Consumer<InterfaceCreator> builder) {
        if (! desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Descriptor must describe a valid class");
        }
        ClassFile cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        byte[] bytes = cf.build(desc, zb -> {
            zb.withVersion(ClassFile.JAVA_17_VERSION, 0);
            InterfaceCreatorImpl tc = new InterfaceCreatorImpl(desc, this, zb);
            gizmo.do_(tc, ic -> {
                ic.accept(builder);
            });
        });
        List<VerifyError> result = cf.verify(bytes);
        if (! result.isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Class failed validation");
            result.forEach(e::addSuppressed);
            throw e;
        }
        outputHandler.accept(desc, bytes);
        return desc;
    }

}
