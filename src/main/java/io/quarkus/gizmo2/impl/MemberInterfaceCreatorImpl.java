package io.quarkus.gizmo2.impl;

import static io.smallrye.classfile.ClassFile.*;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.smallrye.classfile.ClassBuilder;

/**
 * Implementation of a member interface creator.
 * Member interfaces are always implicitly static.
 */
public final class MemberInterfaceCreatorImpl extends InterfaceCreatorImpl {

    MemberInterfaceCreatorImpl(final GizmoImpl gizmo, final ClassDesc type, final ClassOutput output,
            final ClassBuilder zb, final TypeCreatorImpl enclosingType) {
        super(gizmo, type, output, zb, enclosingType);
        // member interfaces are always implicitly static
        modifiers |= ACC_STATIC;
    }

    @Override
    public ModifierLocation modifierLocation() {
        return ModifierLocation.MEMBER_INTERFACE;
    }

}
