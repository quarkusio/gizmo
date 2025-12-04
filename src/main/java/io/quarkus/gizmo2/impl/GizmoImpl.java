package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.function.Consumer;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.ModifierConfigurator;
import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.smallrye.classfile.ClassFile;

public final class GizmoImpl implements Gizmo {
    private static final int[] DEFAULTS = ModifierLocation.values.stream().mapToInt(ModifierLocation::defaultModifierBits)
            .toArray();

    private final ClassOutput outputHandler;
    private final int[] modifiersByLocation;
    private final boolean debugInfo;
    private final boolean parameters;
    private final boolean lambdasAsAnonymousClasses;
    private final ClassFile.Option[] options;

    public GizmoImpl(final ClassOutput outputHandler) {
        this(outputHandler, DEFAULTS, true, true, false);
    }

    private GizmoImpl(final ClassOutput outputHandler, final int[] modifiersByLocation,
            final boolean debugInfo, final boolean parameters,
            final boolean lambdasAsAnonymousClasses) {
        this.outputHandler = outputHandler;
        this.modifiersByLocation = modifiersByLocation;
        this.debugInfo = debugInfo;
        this.parameters = parameters;
        this.lambdasAsAnonymousClasses = lambdasAsAnonymousClasses;
        ArrayList<ClassFile.Option> options = new ArrayList<>();
        options.add(ClassFile.StackMapsOption.DROP_STACK_MAPS);
        if (!debugInfo) {
            options.add(ClassFile.DebugElementsOption.DROP_DEBUG);
            options.add(ClassFile.LineNumbersOption.DROP_LINE_NUMBERS);
        }
        this.options = options.toArray(ClassFile.Option[]::new);
    }

    int getDefaultModifiers(ModifierLocation location) {
        return modifiersByLocation[location.ordinal()];
    }

    ClassFile createClassFile() {
        return ClassFile.of(options);
    }

    @Override
    public Gizmo withDefaultModifiers(final Consumer<ModifierConfigurator> builder) {
        final int[] flags = modifiersByLocation.clone();
        var configurator = new ModifierConfigurator() {

            public void remove(final ModifierLocation location, final ModifierFlag modifierFlag) {
                if (location.requires(modifierFlag)) {
                    throw new IllegalArgumentException(
                            "Flag %s cannot be removed for location %s".formatted(modifierFlag, location));
                }
                flags[location.ordinal()] &= ~modifierFlag.mask();
            }

            public void add(final ModifierLocation location, final ModifierFlag modifierFlag) {
                if (!location.supports(modifierFlag)) {
                    throw new IllegalArgumentException(
                            "Flag %s cannot be set for location %s".formatted(modifierFlag, location));
                }
                modifierFlag.forEachExclusive(f -> remove(location, f));
                flags[location.ordinal()] |= modifierFlag.mask();
            }

            public void set(final ModifierLocation location, final AccessLevel accessLevel) {
                if (!accessLevel.validIn(location)) {
                    throw new IllegalArgumentException(
                            "Access level %s is not valid for location %s".formatted(accessLevel, location));
                }
                flags[location.ordinal()] &= AccessLevel.fullMask();
                flags[location.ordinal()] |= accessLevel.mask();
            }
        };
        builder.accept(configurator);
        return new GizmoImpl(outputHandler, flags.clone(), debugInfo, parameters,
                lambdasAsAnonymousClasses);
    }

    boolean parameters() {
        return parameters;
    }

    boolean lambdasAsAnonymousClasses() {
        return lambdasAsAnonymousClasses;
    }

    @Override
    public Gizmo withOutput(final ClassOutput outputHandler) {
        return new GizmoImpl(outputHandler, modifiersByLocation, debugInfo, parameters,
                lambdasAsAnonymousClasses);
    }

    @Override
    public Gizmo withDebugInfo(final boolean debugInfo) {
        return new GizmoImpl(outputHandler, modifiersByLocation, debugInfo, parameters,
                lambdasAsAnonymousClasses);
    }

    @Override
    public Gizmo withParameters(final boolean parameters) {
        return new GizmoImpl(outputHandler, modifiersByLocation, debugInfo, parameters,
                lambdasAsAnonymousClasses);
    }

    @Override
    public Gizmo withLambdasAsAnonymousClasses(boolean lambdasAsAnonymousClasses) {
        return new GizmoImpl(outputHandler, modifiersByLocation, debugInfo, parameters,
                lambdasAsAnonymousClasses);
    }

    public ClassDesc class_(final ClassDesc desc, final Consumer<ClassCreator> builder) {
        if (!desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Descriptor must describe a valid class");
        }

        ClassFile cf = createClassFile();
        byte[] bytes = cf.build(desc, zb -> {
            ClassCreatorImpl tc = new ClassCreatorImpl(this, desc, outputHandler, zb);
            tc.preAccept();
            builder.accept(tc);
            tc.postAccept();
        });
        outputHandler.write(desc, bytes);
        return desc;
    }

    public ClassDesc interface_(final ClassDesc desc, final Consumer<InterfaceCreator> builder) {
        if (!desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Descriptor must describe a valid class");
        }
        ClassFile cf = createClassFile();
        byte[] bytes = cf.build(desc, zb -> {
            InterfaceCreatorImpl tc = new InterfaceCreatorImpl(this, desc, outputHandler, zb);
            tc.accept(builder);
        });
        outputHandler.write(desc, bytes);
        return desc;
    }
}
