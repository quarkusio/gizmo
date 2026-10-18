package io.quarkus.gizmo2.impl;

import io.quarkus.gizmo2.transformer.MethodTransformer;
import io.smallrye.classfile.AccessFlags;
import io.smallrye.classfile.ClassBuilder;
import io.smallrye.classfile.MethodElement;
import io.smallrye.classfile.MethodModel;
import io.smallrye.common.constraint.Assert;

public final class MethodTransformerImpl extends ModifiableTransformerImpl implements MethodTransformer {
    private final boolean constructor;
    private String newName;

    MethodTransformerImpl(final boolean constructor) {
        this.constructor = constructor;
    }

    public void rename(final String newName) {
        Assert.checkNotNullParam("newName", newName);
        if (constructor) {
            throw new IllegalArgumentException("Constructors cannot be renamed");
        }
        if (newName.isEmpty() || newName.equals("<init>") || newName.equals("<clinit>")) {
            throw new IllegalArgumentException("Invalid method name: " + newName);
        }
        this.newName = newName;
    }

    void apply(ClassBuilder cb, MethodModel mm) {
        String name = newName == null ? mm.methodName().stringValue() : newName;
        cb.withMethod(name, mm.methodTypeSymbol(), applyFlags(mm.flags().flagsMask()), mb -> {
            for (MethodElement me : mm) {
                if (me instanceof AccessFlags) {
                    // replaced above
                    continue;
                }
                MethodElement kept = filterElement(me);
                if (kept != null) {
                    mb.with(kept);
                }
            }
        });
    }
}
