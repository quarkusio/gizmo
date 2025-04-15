package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassFile;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;

public final class GizmoImpl implements Gizmo {
    private final ClassOutput outputHandler;

    public GizmoImpl(final ClassOutput outputHandler) {
        this.outputHandler = outputHandler;
    }

    public Gizmo withOutput(final ClassOutput outputHandler) {
        return new GizmoImpl(outputHandler);
    }

    public ClassDesc class_(final ClassDesc desc, final Consumer<ClassCreator> builder) {
        if (!desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Descriptor must describe a valid class");
        }

        ClassFile cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        byte[] bytes = cf.build(desc, zb -> {
            ClassCreatorImpl tc = new ClassCreatorImpl(desc, outputHandler, zb);
            tc.preAccept();
            builder.accept(tc);
            tc.postAccept();
        });
        outputHandler.write(desc, bytes);
        return desc;
    }

    public ClassDesc interface_(final ClassDesc desc, final Consumer<InterfaceCreator> builder) {
        if (!desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Descriptor must describe a valid class");
        }
        ClassFile cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        byte[] bytes = cf.build(desc, zb -> {
            InterfaceCreatorImpl tc = new InterfaceCreatorImpl(desc, outputHandler, zb);
            tc.accept(builder);
        });
        outputHandler.write(desc, bytes);
        return desc;
    }
}
