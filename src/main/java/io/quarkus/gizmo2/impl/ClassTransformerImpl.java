package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MemberDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.transformer.ClassTransformer;
import io.quarkus.gizmo2.transformer.FieldTransformer;
import io.quarkus.gizmo2.transformer.MethodTransformer;
import io.smallrye.classfile.AccessFlags;
import io.smallrye.classfile.ClassBuilder;
import io.smallrye.classfile.ClassElement;
import io.smallrye.classfile.ClassFile;
import io.smallrye.classfile.ClassModel;
import io.smallrye.classfile.ClassTransform;
import io.smallrye.classfile.FieldModel;
import io.smallrye.classfile.Interfaces;
import io.smallrye.classfile.MethodModel;
import io.smallrye.classfile.constantpool.ClassEntry;
import io.smallrye.classfile.extras.reflect.ClassFileFormatVersion;
import io.smallrye.common.constraint.Assert;

public final class ClassTransformerImpl extends ModifiableTransformerImpl implements ClassTransformer {
    private final GizmoImpl gizmo;
    private final ClassModel model;
    private final ClassDesc type;
    private final boolean isInterface;
    private final Map<String, MethodTransformerImpl> methods = new HashMap<>();
    private final Map<String, FieldTransformerImpl> fields = new HashMap<>();
    private final Set<String> removedMethods = new HashSet<>();
    private final Set<String> removedFields = new HashSet<>();
    private final Set<ClassDesc> addedInterfaces = new LinkedHashSet<>();
    /**
     * The creator used for adding members; only set while the builder runs.
     */
    private TypeCreatorImpl creator;
    private boolean building;

    ClassTransformerImpl(final GizmoImpl gizmo, final ClassModel model) {
        this.gizmo = gizmo;
        this.model = model;
        this.type = model.thisClass().asSymbol();
        this.isInterface = (model.flags().flagsMask() & ClassFile.ACC_INTERFACE) != 0;
    }

    public ClassDesc type() {
        return type;
    }

    // transforming existing members

    public void method(final MethodDesc desc, final Consumer<MethodTransformer> builder) {
        checkBuilding();
        Assert.checkNotNullParam("builder", builder);
        String key = existingMethodKey(desc, desc.name(), desc.type());
        checkNotRemoved(removedMethods, key, desc);
        builder.accept(methods.computeIfAbsent(key, k -> new MethodTransformerImpl(false)));
    }

    public void constructor(final ConstructorDesc desc, final Consumer<MethodTransformer> builder) {
        checkBuilding();
        Assert.checkNotNullParam("builder", builder);
        String key = existingMethodKey(desc, "<init>", desc.type());
        checkNotRemoved(removedMethods, key, desc);
        builder.accept(methods.computeIfAbsent(key, k -> new MethodTransformerImpl(true)));
    }

    public void field(final FieldDesc desc, final Consumer<FieldTransformer> builder) {
        checkBuilding();
        Assert.checkNotNullParam("builder", builder);
        String key = existingFieldKey(desc);
        checkNotRemoved(removedFields, key, desc);
        builder.accept(fields.computeIfAbsent(key, k -> new FieldTransformerImpl()));
    }

    public void removeMethod(final MethodDesc desc) {
        checkBuilding();
        String key = existingMethodKey(desc, desc.name(), desc.type());
        checkNotTransformed(methods, key, desc);
        removedMethods.add(key);
    }

    public void removeConstructor(final ConstructorDesc desc) {
        checkBuilding();
        String key = existingMethodKey(desc, "<init>", desc.type());
        checkNotTransformed(methods, key, desc);
        removedMethods.add(key);
    }

    public void removeField(final FieldDesc desc) {
        checkBuilding();
        String key = existingFieldKey(desc);
        checkNotTransformed(fields, key, desc);
        removedFields.add(key);
    }

    public void implements_(final ClassDesc interface_) {
        checkBuilding();
        Assert.checkNotNullParam("interface_", interface_);
        if (!interface_.isClassOrInterface()) {
            throw new IllegalArgumentException("Not an interface type: " + interface_.displayName());
        }
        addedInterfaces.add(interface_);
    }

    // adding members

    public FieldDesc field(final String name, final Consumer<InstanceFieldCreator> builder) {
        return classCreator().field(name, builder);
    }

    public StaticFieldVar staticField(final String name, final Consumer<StaticFieldCreator> builder) {
        return creator().staticField(name, builder);
    }

    public MethodDesc method(final String name, final Consumer<InstanceMethodCreator> builder) {
        return classCreator().method(name, builder);
    }

    public MethodDesc staticMethod(final String name, final Consumer<StaticMethodCreator> builder) {
        return creator().staticMethod(name, builder);
    }

    public ConstructorDesc constructor(final Consumer<ConstructorCreator> builder) {
        return classCreator().constructor(builder);
    }

    public This this_() {
        return creator().this_();
    }

    private TypeCreatorImpl creator() {
        checkBuilding();
        if (model.majorVersion() < ClassFile.JAVA_17_VERSION) {
            throw new IllegalArgumentException("Members can only be added to classes with a class file version of "
                    + "Java 17 or later, but " + type.displayName() + " has major version " + model.majorVersion());
        }
        return creator;
    }

    private ClassCreatorImpl classCreator() {
        if (isInterface) {
            throw new IllegalArgumentException(
                    "Instance members and constructors cannot be added to interface " + type.displayName());
        }
        return (ClassCreatorImpl) creator();
    }

    private void checkBuilding() {
        if (!building) {
            throw new IllegalStateException("The class transformer may only be used while its builder runs");
        }
    }

    // applying the transformation

    byte[] transform(final ClassFile cf, final Consumer<ClassTransformer> builder) {
        ClassTransform transform = new ClassTransform() {
            boolean sawInterfaces;

            public void atStart(final ClassBuilder cb) {
                // added members are written to `cb` as soon as their builders return
                creator = isInterface
                        ? new InterfaceCreatorImpl(gizmo, type, gizmo.outputHandler(), cb)
                        : new ClassCreatorImpl(gizmo, type, gizmo.outputHandler(), cb);
                creator.initForTransform(model.flags().flagsMask(),
                        model.superclass().map(ClassEntry::asSymbol).orElse(null),
                        ClassFileFormatVersion.fromMajor(model.majorVersion()));
                building = true;
                try {
                    builder.accept(ClassTransformerImpl.this);
                } finally {
                    building = false;
                }
                String unsupported = creator.unsupportedForTransform();
                if (unsupported != null) {
                    throw new IllegalArgumentException(
                            "Added members of " + type.displayName() + " require " + unsupported
                                    + ", which is not supported when transforming a class");
                }
                checkDuplicates();
            }

            public void accept(final ClassBuilder cb, final ClassElement ce) {
                if (ce instanceof AccessFlags af) {
                    cb.withFlags(applyFlags(af.flagsMask()));
                } else if (ce instanceof Interfaces ifs) {
                    sawInterfaces = true;
                    cb.withInterfaceSymbols(mergeInterfaces(ifs.interfaces()));
                } else if (ce instanceof MethodModel mm) {
                    String key = methodKey(mm);
                    if (removedMethods.contains(key)) {
                        return;
                    }
                    MethodTransformerImpl mt = methods.get(key);
                    if (mt == null) {
                        cb.with(mm);
                    } else {
                        mt.apply(cb, mm);
                    }
                } else if (ce instanceof FieldModel fm) {
                    String key = fieldKey(fm);
                    if (removedFields.contains(key)) {
                        return;
                    }
                    FieldTransformerImpl ft = fields.get(key);
                    if (ft == null) {
                        cb.with(fm);
                    } else {
                        ft.apply(cb, fm);
                    }
                } else {
                    ClassElement kept = filterElement(ce);
                    if (kept != null) {
                        cb.with(kept);
                    }
                }
            }

            public void atEnd(final ClassBuilder cb) {
                if (!sawInterfaces && !addedInterfaces.isEmpty()) {
                    cb.withInterfaceSymbols(List.copyOf(addedInterfaces));
                }
            }
        };
        return cf.transformClass(model, transform);
    }

    /**
     * Check that the added members do not clash with the existing members which are kept.
     */
    private void checkDuplicates() {
        Set<String> existingMethods = new HashSet<>();
        for (MethodModel mm : model.methods()) {
            String key = methodKey(mm);
            if (!removedMethods.contains(key)) {
                MethodTransformerImpl mt = methods.get(key);
                String newName = mt == null ? null : mt.newName();
                existingMethods.add(newName == null ? key : newName + mm.methodType().stringValue());
            }
        }
        for (MethodDesc added : creator.methods.keySet()) {
            if (existingMethods.contains(added.name() + added.type().descriptorString())) {
                throw new IllegalArgumentException("Added method " + added + " already exists");
            }
        }
        for (ConstructorDesc added : creator.constructors) {
            if (existingMethods.contains("<init>" + added.type().descriptorString())) {
                throw new IllegalArgumentException("Added constructor " + added + " already exists");
            }
        }
        Set<String> existingFields = new HashSet<>();
        for (FieldModel fm : model.fields()) {
            String key = fieldKey(fm);
            if (!removedFields.contains(key)) {
                existingFields.add(fm.fieldName().stringValue());
            }
        }
        for (FieldDesc added : creator.fields.keySet()) {
            if (existingFields.contains(added.name())) {
                throw new IllegalArgumentException("Added field " + added + " already exists");
            }
        }
    }

    private List<ClassDesc> mergeInterfaces(List<ClassEntry> existing) {
        Set<ClassDesc> all = new LinkedHashSet<>();
        for (ClassEntry entry : existing) {
            all.add(entry.asSymbol());
        }
        all.addAll(addedInterfaces);
        return new ArrayList<>(all);
    }

    private static String methodKey(MethodModel mm) {
        return mm.methodName().stringValue() + mm.methodType().stringValue();
    }

    private static String fieldKey(FieldModel fm) {
        return fm.fieldName().stringValue() + ":" + fm.fieldType().stringValue();
    }

    private String existingMethodKey(MemberDesc desc, String name, MethodTypeDesc methodType) {
        checkOwner(desc);
        String key = name + methodType.descriptorString();
        for (MethodModel mm : model.methods()) {
            if (key.equals(methodKey(mm))) {
                return key;
            }
        }
        throw new IllegalArgumentException("No member " + desc + " in " + type.displayName());
    }

    private String existingFieldKey(FieldDesc desc) {
        checkOwner(desc);
        String key = desc.name() + ":" + desc.type().descriptorString();
        for (FieldModel fm : model.fields()) {
            if (key.equals(fieldKey(fm))) {
                return key;
            }
        }
        throw new IllegalArgumentException("No member " + desc + " in " + type.displayName());
    }

    private void checkOwner(MemberDesc desc) {
        Assert.checkNotNullParam("desc", desc);
        if (!desc.owner().equals(type)) {
            throw new IllegalArgumentException(
                    "Member " + desc + " does not belong to the transformed class " + type.displayName());
        }
    }

    private static void checkNotRemoved(Set<String> removed, String key, MemberDesc desc) {
        if (removed.contains(key)) {
            throw new IllegalArgumentException("Member " + desc + " was already removed");
        }
    }

    private static void checkNotTransformed(Map<String, ?> transformed, String key, MemberDesc desc) {
        if (transformed.containsKey(key)) {
            throw new IllegalArgumentException("Member " + desc + " is being transformed and cannot also be removed");
        }
    }
}
