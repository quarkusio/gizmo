package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

public sealed interface MemberDesc permits ConstructorDesc, FieldDesc, MethodDesc {
    ClassDesc owner();

    String name();
}
