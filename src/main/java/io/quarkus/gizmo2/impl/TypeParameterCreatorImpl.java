package io.quarkus.gizmo2.impl;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Optional;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.TypeParameter;
import io.quarkus.gizmo2.creator.TypeParameterCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.smallrye.classfile.Annotation;
import io.smallrye.common.constraint.Assert;

public final class TypeParameterCreatorImpl extends AnnotatableCreatorImpl implements TypeParameterCreator {
    private final String name;
    private GenericType.OfReference firstBound;
    private List<GenericType.OfReference> otherBounds = List.of();

    public TypeParameterCreatorImpl(final String name) {
        this.name = name;
    }

    public TypeParameterCreatorImpl(final List<Annotation> visible, final List<Annotation> invisible, final String name) {
        super(visible, invisible);
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public void setFirstBound(final GenericType.OfClass bound) {
        firstBound = Assert.checkNotNullParam("bound", bound);
    }

    @Override
    public void setFirstBound(final GenericType.OfTypeVariable bound) {
        if (!otherBounds.isEmpty()) {
            throw new IllegalArgumentException("Other bounds may not be present when the first bound is a type variable");
        }
        firstBound = Assert.checkNotNullParam("bound", bound);
    }

    @Override
    public void setOtherBounds(final List<GenericType.OfClass> bounds) {
        if (firstBound instanceof GenericType.OfTypeVariable) {
            throw new IllegalArgumentException("Other bounds may not be present when the first bound is a type variable");
        }
        otherBounds = List.copyOf(bounds);
    }

    public ElementType annotationTargetType() {
        return ElementType.TYPE_PARAMETER;
    }

    private static final MethodHandle ofType;
    private static final MethodHandle ofConstructor;
    private static final MethodHandle ofMethod;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(TypeParameter.class, MethodHandles.lookup());
            ofType = lookup.findConstructor(TypeParameter.OfType.class, MethodType.methodType(
                    void.class,
                    List.class, List.class, String.class, Optional.class, List.class, ClassDesc.class));
            ofConstructor = lookup.findConstructor(TypeParameter.OfConstructor.class, MethodType.methodType(
                    void.class,
                    List.class, List.class, String.class, Optional.class, List.class, ConstructorDesc.class));
            ofMethod = lookup.findConstructor(TypeParameter.OfMethod.class, MethodType.methodType(
                    void.class,
                    List.class, List.class, String.class, Optional.class, List.class, MethodDesc.class));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    TypeParameter.OfConstructor forConstructor(ConstructorDesc desc) {
        try {
            return (TypeParameter.OfConstructor) ofConstructor.invokeExact(
                    List.copyOf(visible.values()),
                    List.copyOf(invisible.values()),
                    name,
                    Optional.ofNullable(firstBound),
                    otherBounds,
                    desc);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    TypeParameter.OfMethod forMethod(MethodDesc desc) {
        try {
            return (TypeParameter.OfMethod) ofMethod.invokeExact(
                    List.copyOf(visible.values()),
                    List.copyOf(invisible.values()),
                    name,
                    Optional.ofNullable(firstBound),
                    otherBounds,
                    desc);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    TypeParameter.OfType forType(ClassDesc desc) {
        try {
            return (TypeParameter.OfType) ofType.invokeExact(
                    List.copyOf(visible.values()),
                    List.copyOf(invisible.values()),
                    name,
                    Optional.ofNullable(firstBound),
                    otherBounds,
                    desc);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
