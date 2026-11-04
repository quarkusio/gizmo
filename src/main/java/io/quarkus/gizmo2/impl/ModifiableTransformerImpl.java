package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.transformer.ModifiableTransformer;
import io.smallrye.classfile.Annotation;
import io.smallrye.classfile.ClassFileElement;
import io.smallrye.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import io.smallrye.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import io.smallrye.common.constraint.Assert;

public abstract sealed class ModifiableTransformerImpl implements ModifiableTransformer
        permits ClassTransformerImpl, FieldTransformerImpl, MethodTransformerImpl {
    private int addMask;
    private int removeMask;
    private AccessLevel access;
    private final Set<ClassDesc> removedAnnotations = new HashSet<>();

    public void addFlag(final ModifierFlag flag) {
        Assert.checkNotNullParam("flag", flag);
        flag.forEachExclusive(this::removeFlag);
        addMask |= flag.mask();
        removeMask &= ~flag.mask();
    }

    public void removeFlag(final ModifierFlag flag) {
        Assert.checkNotNullParam("flag", flag);
        removeMask |= flag.mask();
        addMask &= ~flag.mask();
    }

    public void access(final AccessLevel access) {
        this.access = Assert.checkNotNullParam("access", access);
    }

    public void removeAnnotation(final ClassDesc annotationType) {
        removedAnnotations.add(Assert.checkNotNullParam("annotationType", annotationType));
    }

    /**
     * {@return the given flags with the recorded changes applied}
     */
    int applyFlags(int flags) {
        flags = flags & ~removeMask | addMask;
        if (access != null) {
            flags = flags & ~AccessLevel.fullMask() | access.mask();
        }
        return flags;
    }

    /**
     * {@return the element to emit in place of the given one, or {@code null} to drop it}
     * Annotation attributes have the removed annotations filtered out; other elements are returned as-is.
     */
    @SuppressWarnings("unchecked")
    <E extends ClassFileElement> E filterElement(E element) {
        if (element instanceof RuntimeVisibleAnnotationsAttribute a) {
            return (E) filter(a);
        } else if (element instanceof RuntimeInvisibleAnnotationsAttribute a) {
            return (E) filter(a);
        } else {
            return element;
        }
    }

    /**
     * {@return the attribute with the removed annotations filtered out, or {@code null} to drop it entirely}
     */
    RuntimeVisibleAnnotationsAttribute filter(RuntimeVisibleAnnotationsAttribute attr) {
        List<Annotation> kept = filter(attr.annotations());
        return kept == null ? attr : kept.isEmpty() ? null : RuntimeVisibleAnnotationsAttribute.of(kept);
    }

    /**
     * {@return the attribute with the removed annotations filtered out, or {@code null} to drop it entirely}
     */
    RuntimeInvisibleAnnotationsAttribute filter(RuntimeInvisibleAnnotationsAttribute attr) {
        List<Annotation> kept = filter(attr.annotations());
        return kept == null ? attr : kept.isEmpty() ? null : RuntimeInvisibleAnnotationsAttribute.of(kept);
    }

    /**
     * {@return the kept annotations, or {@code null} if nothing was removed}
     */
    private List<Annotation> filter(List<Annotation> annotations) {
        if (removedAnnotations.isEmpty()) {
            return null;
        }
        List<Annotation> kept = new ArrayList<>(annotations.size());
        for (Annotation annotation : annotations) {
            if (!removedAnnotations.contains(annotation.classSymbol())) {
                kept.add(annotation);
            }
        }
        return kept.size() == annotations.size() ? null : kept;
    }
}
