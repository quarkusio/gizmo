package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.impl.FieldCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

public sealed interface FieldCreator extends MemberCreator permits InstanceFieldCreator, StaticFieldCreator, FieldCreatorImpl {
    FieldDesc desc();

    void withTypeSignature(Signature.ClassTypeSig type);

    void withType(ClassDesc type);

    default void withType(Class<?> type) {
        withType(Util.classDesc(type));
    }
}
