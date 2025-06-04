package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.AnnotationElement;
import io.github.dmlloyd.classfile.AnnotationValue;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import io.quarkus.gizmo2.creator.AnnotatableCreator;
import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.smallrye.common.constraint.Assert;

public abstract sealed class AnnotatableCreatorImpl implements AnnotatableCreator
        permits ModifiableCreatorImpl, TypeAnnotatableCreatorImpl, TypeParameterCreatorImpl {
    protected final String creationSite = Util.trackCaller();

    Map<ClassDesc, Annotation> invisible;
    Map<ClassDesc, Annotation> visible;

    protected AnnotatableCreatorImpl() {
        invisible = Map.of();
        visible = Map.of();
    }

    protected AnnotatableCreatorImpl(List<Annotation> visible, List<Annotation> invisible) {
        this.invisible = invisible.isEmpty() ? Map.of()
                : invisible.stream().collect(Collectors.toMap(Annotation::classSymbol, Function.identity(), (a, b) -> {
                    throw new IllegalStateException();
                }, LinkedHashMap::new));
        this.visible = visible.isEmpty() ? Map.of()
                : visible.stream().collect(Collectors.toMap(Annotation::classSymbol, Function.identity(), (a, b) -> {
                    throw new IllegalStateException();
                }, LinkedHashMap::new));
    }

    private Map<ClassDesc, Annotation> visibleMap() {
        Map<ClassDesc, Annotation> map = visible;
        if (map.isEmpty()) {
            map = visible = new LinkedHashMap<>(4);
        }
        return map;
    }

    private Map<ClassDesc, Annotation> invisibleMap() {
        Map<ClassDesc, Annotation> map = invisible;
        if (map.isEmpty()) {
            map = invisible = new LinkedHashMap<>(4);
        }
        return map;
    }

    public abstract ElementType annotationTargetType();

    @Override
    public <A extends java.lang.annotation.Annotation> void addAnnotation(Class<A> annotationClass,
            Consumer<AnnotationCreator<A>> builder) {
        checkNotNullParam("annotationClass", annotationClass);
        checkNotNullParam("builder", builder);

        Target target = annotationClass.getAnnotation(Target.class);
        if (target != null) {
            Set<ElementType> elementTypes = Set.of(target.value());
            if (!elementTypes.contains(annotationTargetType())) {
                throw new IllegalArgumentException("Annotation %s is not allowed on element type `%s` (the allowed set is %s)"
                        .formatted(annotationClass, annotationTargetType(), elementTypes));
            }
        }
        Retention retention = annotationClass.getAnnotation(Retention.class);
        Annotation annotation = AnnotationCreatorImpl.makeAnnotation(annotationClass, builder);
        RetentionPolicy retentionPolicy = retention == null ? RetentionPolicy.RUNTIME : retention.value();
        if (retentionPolicy == RetentionPolicy.SOURCE) {
            // just exit without adding
            return;
        }
        Repeatable repeatable = annotationClass.getAnnotation(Repeatable.class);
        if (repeatable != null) {
            // special process: if it is not the first to be added, then add it to the existing list
            Map<ClassDesc, Annotation> map = getAnnotationMap(retentionPolicy);
            ClassDesc repeatableType = Util.classDesc(repeatable.value());
            if (map.containsKey(annotation.classSymbol())) {
                // pull out the singleton and replace it with the wrapped type
                Annotation old = map.remove(annotation.classSymbol());
                map.put(repeatableType, Annotation.of(repeatableType, List.of(
                        AnnotationElement.of("value", AnnotationValue.ofArray(
                                AnnotationValue.ofAnnotation(old),
                                AnnotationValue.ofAnnotation(annotation))))));
            } else if (map.containsKey(repeatableType)) {
                // pull out the old list and add the new annotation to it
                Annotation old = map.get(repeatableType);
                AnnotationValue.OfArray value = old.elements().stream().filter(ae -> ae.name().equalsString("value"))
                        .map(AnnotationElement::value)
                        .map(AnnotationValue.OfArray.class::cast).findFirst().orElseThrow();
                Annotation replacement = Annotation.of(repeatableType, List.of(
                        AnnotationElement.of("value", AnnotationValue.ofArray(
                                Stream.concat(
                                        value.values().stream(),
                                        Stream.of(AnnotationValue.ofAnnotation(annotation))).toList()))));
                map.replace(repeatableType, old, replacement);
            } else {
                // add singly
                registerAnnotation(retentionPolicy, annotation);
            }
        } else {
            // register singly
            registerAnnotation(retentionPolicy, annotation);
        }
    }

    private Map<ClassDesc, Annotation> getAnnotationMap(final RetentionPolicy retentionPolicy) {
        return switch (retentionPolicy) {
            case SOURCE -> throw Assert.impossibleSwitchCase(retentionPolicy);
            case CLASS -> invisibleMap();
            case RUNTIME -> visibleMap();
        };
    }

    @Override
    public void addAnnotation(ClassDesc annotationClass, RetentionPolicy retentionPolicy,
            Consumer<AnnotationCreator<java.lang.annotation.Annotation>> builder) {
        checkNotNullParam("annotationClass", annotationClass);
        checkNotNullParam("retentionPolicy", retentionPolicy);
        checkNotNullParam("builder", builder);

        Annotation annotation = AnnotationCreatorImpl.makeAnnotation(annotationClass, builder);
        if (retentionPolicy == RetentionPolicy.SOURCE) {
            // just exit without adding
            return;
        }
        registerAnnotation(retentionPolicy, annotation);
    }

    private void registerAnnotation(final RetentionPolicy retentionPolicy, final Annotation annotation) {
        Annotation existing = getAnnotationMap(retentionPolicy).putIfAbsent(annotation.classSymbol(), annotation);
        if (existing != null) {
            throw new IllegalArgumentException("Duplicate annotation %s".formatted(annotation.className().stringValue()));
        }
    }

    void addInvisible(Consumer<? super RuntimeInvisibleAnnotationsAttribute> consumer) {
        if (!invisible.isEmpty()) {
            consumer.accept(RuntimeInvisibleAnnotationsAttribute.of(List.copyOf(invisible.values())));
        }
    }

    void addVisible(Consumer<? super RuntimeVisibleAnnotationsAttribute> consumer) {
        if (!visible.isEmpty()) {
            consumer.accept(RuntimeVisibleAnnotationsAttribute.of(List.copyOf(visible.values())));
        }
    }

    public List<Annotation> invisible() {
        return List.copyOf(invisible.values());
    }

    public List<Annotation> visible() {
        return List.copyOf(visible.values());
    }
}
