package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.creator.GenericType;

public final class SignatureUtil {
    public static Signature of(GenericType type) {
        if (type instanceof GenericType.PrimitiveType pt) {
            return ofPrimitive(pt);
        } else if (type instanceof GenericType.ReferenceType rt) {
            return ofReference(rt);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public static Signature.BaseTypeSig ofPrimitive(GenericType.PrimitiveType type) {
        return Signature.BaseTypeSig.of(type.desc());
    }

    public static Signature.RefTypeSig ofReference(GenericType.ReferenceType type) {
        if (type instanceof GenericType.ArrayType at) {
            return ofArray(at);
        } else if (type instanceof GenericType.ClassType || type instanceof GenericType.ParameterizedType) {
            return ofClassOrParameterized(type);
        } else if (type instanceof GenericType.TypeVariable tv) {
            return ofTypeVariable(tv);
        } else {
            throw new IllegalArgumentException("Unsupported reference type: " + type);
        }
    }

    public static Signature.ArrayTypeSig ofArray(GenericType.ArrayType type) {
        return Signature.ArrayTypeSig.of(type.dimensions(), of(type.elementType()));
    }

    public static Signature.ThrowableSig ofThrowable(GenericType.ReferenceType type) {
        if (type instanceof GenericType.ClassType || type instanceof GenericType.ParameterizedType) {
            return ofClassOrParameterized(type);
        } else if (type instanceof GenericType.TypeVariable tv) {
            return ofTypeVariable(tv);
        } else {
            throw new IllegalArgumentException("Unsupported throwable type: " + type);
        }
    }

    public static Signature.ClassTypeSig ofClassOrParameterized(GenericType.ReferenceType owner) {
        if (owner instanceof GenericType.ClassType ct) {
            return ofClass(ct);
        } else if (owner instanceof GenericType.ParameterizedType pt) {
            return ofParameterized(pt);
        } else {
            throw new IllegalArgumentException("Not a class or parameterized type: " + owner);
        }
    }

    public static Signature.ClassTypeSig ofClass(GenericType.ClassType type) {
        Signature.ClassTypeSig owner = type.owner().map(SignatureUtil::ofClassOrParameterized).orElse(null);
        return Signature.ClassTypeSig.of(owner, type.desc());
    }

    public static Signature.ClassTypeSig ofParameterized(GenericType.ParameterizedType type) {
        Signature.ClassTypeSig owner = type.owner().map(SignatureUtil::ofClassOrParameterized).orElse(null);
        Signature.TypeArg[] typeArguments = type.typeArguments()
                .stream()
                .map(SignatureUtil::ofTypeArg)
                .toArray(Signature.TypeArg[]::new);
        return Signature.ClassTypeSig.of(owner, type.genericClass().desc(), typeArguments);
    }

    public static Signature.TypeArg ofTypeArg(GenericType.ReferenceType type) {
        if (type instanceof GenericType.WildcardType wt) {
            if (wt.lowerBound().isPresent()) {
                return Signature.TypeArg.superOf((Signature.RefTypeSig) of(wt.lowerBound().get()));
            } else if (wt.upperBound().isPresent()) {
                return Signature.TypeArg.extendsOf((Signature.RefTypeSig) of(wt.upperBound().get()));
            } else {
                return Signature.TypeArg.unbounded();
            }
        }
        return Signature.TypeArg.of(ofReference(type));
    }

    public static Signature.TypeVarSig ofTypeVariable(GenericType.TypeVariable type) {
        return Signature.TypeVarSig.of(type.name());
    }

    public static Signature.TypeParam ofTypeParam(GenericType.TypeVariable type) {
        return Signature.TypeParam.of(type.name(),
                type.firstBound().map(SignatureUtil::ofReference),
                type.nextBounds().stream().map(SignatureUtil::ofReference).toArray(Signature.RefTypeSig[]::new));
    }
}
