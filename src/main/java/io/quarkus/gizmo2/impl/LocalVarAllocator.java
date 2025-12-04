package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.gizmo2.GenericType;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;
import io.smallrye.classfile.TypeAnnotation;
import io.smallrye.classfile.instruction.LocalVariable;
import io.smallrye.classfile.instruction.LocalVariableType;

final class LocalVarAllocator extends Item {
    private final LocalVarImpl localVar;
    private Label startScope;
    private Label endScope;

    LocalVarAllocator(final LocalVarImpl localVar) {
        this.localVar = localVar;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        int slot = cb.allocateLocal(Util.actualKindOf(localVar.typeKind()));
        // we reserve the slot for the full remainder of the block to avoid control-flow analysis
        startScope = cb.newBoundLabel();
        endScope = block.endLabel();
        cb.with(LocalVariable.of(slot, localVar.name(), localVar.type(), startScope, endScope));
        if (localVar.hasGenericType()) {
            GenericType gt = localVar.genericType();
            if (!gt.isRaw()) {
                cb.with(LocalVariableType.of(slot, localVar.name(), Util.signatureOf(gt), startScope, endScope));
            }
        }
        localVar.slot = slot;
        smb.store(slot, localVar.type());
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (localVar.hasGenericType() && localVar.genericType().hasAnnotations(retention)) {
            Util.computeAnnotations(localVar.genericType(), retention, TypeAnnotation.TargetInfo.ofLocalVariable(
                    List.of(
                            TypeAnnotation.LocalVarTargetInfo.of(startScope, endScope, localVar.slot))),
                    annotations, new ArrayDeque<>());
        }
    }
}
