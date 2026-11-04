package io.quarkus.gizmo2.impl;

import io.quarkus.gizmo2.transformer.FieldTransformer;
import io.smallrye.classfile.AccessFlags;
import io.smallrye.classfile.ClassBuilder;
import io.smallrye.classfile.FieldElement;
import io.smallrye.classfile.FieldModel;

public final class FieldTransformerImpl extends ModifiableTransformerImpl implements FieldTransformer {
    FieldTransformerImpl() {
    }

    void apply(ClassBuilder cb, FieldModel fm) {
        cb.withField(fm.fieldName(), fm.fieldType(), fb -> {
            fb.withFlags(applyFlags(fm.flags().flagsMask()));
            for (FieldElement fe : fm) {
                if (fe instanceof AccessFlags) {
                    // replaced above
                    continue;
                }
                FieldElement kept = filterElement(fe);
                if (kept != null) {
                    fb.with(kept);
                }
            }
        });
    }
}
