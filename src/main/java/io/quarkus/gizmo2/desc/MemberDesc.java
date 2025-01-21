package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.FieldDesc;

public sealed interface MemberDesc permits ConstructorDesc, FieldDesc, MethodDesc {
    ClassDesc owner();

    String name();

    StringBuilder toString(StringBuilder b);
}
