package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.smallrye.classfile.ClassBuilder;

/**
 * Implementation of a member class creator.
 * Whether the class is a static member class or a non-static inner class is determined
 * by whether the {@link ModifierFlag#STATIC STATIC} modifier flag is set.
 * <p>
 * For non-static inner classes, a synthetic {@code this$N} field is added to hold a reference
 * to the enclosing instance, and every constructor is augmented with a leading parameter
 * for the outer instance. The {@code this$N} field is assigned before the super constructor
 * call using the {@code preInits} mechanism.
 */
public final class MemberClassCreatorImpl extends ClassCreatorImpl {
    private boolean innerClassSetUp;
    private String outerFieldName;
    private FieldDesc outerFieldDesc;
    /**
     * Holds the synthetic outer-instance constructor parameter for the constructor currently being built.
     * Set by {@link #constructor(Consumer)} before the user's builder runs; read by the pre-initializer
     * that assigns {@code this$N}.
     */
    private ParamVar currentOuterParam;

    MemberClassCreatorImpl(final GizmoImpl gizmo, final ClassDesc type, final ClassOutput output,
            final ClassBuilder zb, final TypeCreatorImpl enclosingType) {
        super(gizmo, type, output, zb, enclosingType);
    }

    @Override
    public ModifierLocation modifierLocation() {
        return ModifierLocation.MEMBER_CLASS;
    }

    /**
     * Compute the outer depth for {@code this$N} naming.
     * Counts non-static enclosing types between this class and the nearest static or top-level boundary.
     *
     * @return the depth value N for the {@code this$N} field name
     */
    private int computeOuterDepth() {
        int depth = 0;
        TypeCreatorImpl cur = enclosingType();
        while (cur != null && cur.enclosingType() != null) {
            // interfaces are always static — stop counting
            if (cur instanceof MemberInterfaceCreatorImpl) {
                break;
            }
            // static member class resets the counter
            if (cur instanceof MemberClassCreatorImpl && Modifier.isStatic(cur.modifiers)) {
                break;
            }
            depth++;
            cur = cur.enclosingType();
        }
        return depth;
    }

    /**
     * Lazily set up inner class machinery (the {@code this$N} field and its pre-initializer).
     * Called on the first constructor creation for non-static member classes.
     */
    private void ensureInnerClassSetup() {
        if (!innerClassSetUp) {
            innerClassSetUp = true;
            int depth = computeOuterDepth();
            outerFieldName = "this$" + depth;
            // create the synthetic this$N field
            outerFieldDesc = field(outerFieldName, ifc -> {
                ifc.setType(enclosingType().type());
                ifc.setAccess(AccessLevel.PRIVATE);
                ifc.addFlag(ModifierFlag.FINAL);
                ifc.addFlag(ModifierFlag.SYNTHETIC);
            });
            // assign this$N = outerParam BEFORE super constructor call (JVMS allows putfield on uninitializedThis)
            instancePreinitializer(b0 -> {
                b0.set(this_().field(outerFieldDesc), currentOuterParam);
            });
        }
    }

    /**
     * Ensure that the inner class machinery is set up, and return the field descriptor
     * for the synthetic {@code this$N} field.
     * This method is safe to call at any time; it is idempotent.
     *
     * @return the field descriptor for the {@code this$N} field (not {@code null})
     */
    FieldDesc outerFieldDesc() {
        ensureInnerClassSetup();
        return outerFieldDesc;
    }

    @Override
    public ConstructorDesc constructor(final Consumer<ConstructorCreator> builder) {
        if (!Modifier.isStatic(modifiers)) {
            // non-static inner class: inject synthetic outer instance parameter
            ensureInnerClassSetup();
            return super.constructor(cc -> {
                currentOuterParam = cc.parameter(outerFieldName, enclosingType().type());
                builder.accept(cc);
            });
        } else {
            // static member class: no outer instance parameter
            return super.constructor(builder);
        }
    }

}
