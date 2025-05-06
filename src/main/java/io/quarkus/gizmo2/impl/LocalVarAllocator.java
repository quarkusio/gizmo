package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.GenericType;

final class LocalVarAllocator extends Item {
    private final LocalVarImpl localVar;
    private Label startScope;
    private Label endScope;

    LocalVarAllocator(final LocalVarImpl localVar) {
        this.localVar = localVar;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        int slot = cb.allocateLocal(Util.actualKindOf(localVar.typeKind()));
        // we reserve the slot for the full remainder of the block to avoid control-flow analysis
        startScope = cb.newBoundLabel();
        endScope = block.endLabel();
        cb.localVariable(slot, localVar.name(), localVar.type(), startScope, endScope);
        GenericType gt = localVar.genericType();
        if (gt instanceof GenericType.OfClass oc && !oc.typeArguments().isEmpty()) {
            cb.localVariableType(slot, localVar.name(), Util.signatureOf(gt), startScope, endScope);
        }
        int lvSlot = localVar.slot;
        if (lvSlot != -1 && slot != lvSlot) {
            throw new IllegalStateException("Local variable reallocated into a different slot");
        }
        localVar.slot = slot;
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (localVar.genericType().hasAnnotations(retention)) {
            Util.computeAnnotations(localVar.genericType(), retention, TypeAnnotation.TargetInfo.ofLocalVariable(
                    List.of(
                            TypeAnnotation.LocalVarTargetInfo.of(startScope, endScope, localVar.slot))),
                    annotations, new ArrayDeque<>());
        }
    }
}
