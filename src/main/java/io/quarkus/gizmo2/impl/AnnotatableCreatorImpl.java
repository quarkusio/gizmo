package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import io.quarkus.gizmo2.Annotatable;

public abstract sealed class AnnotatableCreatorImpl implements Annotatable
        permits ExecutableCreatorImpl, FieldCreatorImpl, ParamCreatorImpl, TypeCreatorImpl {
    List<Annotation> invisible = List.of();
    List<Annotation> visible = List.of();

    private List<Annotation> visible() {
        List<Annotation> list = visible;
        if (list.isEmpty()) {
            list = visible = new ArrayList<>(4);
        }
        return list;
    }

    private List<Annotation> invisible() {
        List<Annotation> list = invisible;
        if (list.isEmpty()) {
            list = invisible = new ArrayList<>(4);
        }
        return list;
    }

    public void withAnnotation(final RetentionPolicy retention, final Annotation annotation) {
        // ignore SOURCE for now (though we could record it for posterity, maybe put it in a custom attribute)
        switch (retention) {
            case CLASS -> invisible().add(annotation);
            case RUNTIME -> visible().add(annotation);
        }
    }

    void addInvisible(Consumer<? super RuntimeInvisibleAnnotationsAttribute> consumer) {
        if (!invisible.isEmpty()) {
            consumer.accept(RuntimeInvisibleAnnotationsAttribute.of(visible));
        }
    }

    void addVisible(Consumer<? super RuntimeVisibleAnnotationsAttribute> consumer) {
        if (!visible.isEmpty()) {
            consumer.accept(RuntimeVisibleAnnotationsAttribute.of(visible));
        }
    }
}
