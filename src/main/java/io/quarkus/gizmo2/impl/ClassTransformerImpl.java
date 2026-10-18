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
import io.smallrye.common.constraint.Assert;

public final class ClassTransformerImpl extends ModifiableTransformerImpl implements ClassTransformer {
    private final ClassModel model;
    private final ClassDesc type;
    private final Map<String, MethodTransformerImpl> methods = new HashMap<>();
    private final Map<String, FieldTransformerImpl> fields = new HashMap<>();
    private final Set<String> removedMethods = new HashSet<>();
    private final Set<String> removedFields = new HashSet<>();
    private final Set<ClassDesc> addedInterfaces = new LinkedHashSet<>();

    ClassTransformerImpl(final ClassModel model) {
        this.model = model;
        this.type = model.thisClass().asSymbol();
    }

    public ClassDesc type() {
        return type;
    }

    public void method(final MethodDesc desc, final Consumer<MethodTransformer> builder) {
        Assert.checkNotNullParam("builder", builder);
        String key = methodKey(desc, desc.name(), desc.type());
        checkNotRemoved(removedMethods, key, desc);
        builder.accept(methods.computeIfAbsent(key, k -> new MethodTransformerImpl(false)));
    }

    public void constructor(final ConstructorDesc desc, final Consumer<MethodTransformer> builder) {
        Assert.checkNotNullParam("builder", builder);
        String key = methodKey(desc, "<init>", desc.type());
        checkNotRemoved(removedMethods, key, desc);
        builder.accept(methods.computeIfAbsent(key, k -> new MethodTransformerImpl(true)));
    }

    public void field(final FieldDesc desc, final Consumer<FieldTransformer> builder) {
        Assert.checkNotNullParam("builder", builder);
        String key = fieldKey(desc);
        checkNotRemoved(removedFields, key, desc);
        builder.accept(fields.computeIfAbsent(key, k -> new FieldTransformerImpl()));
    }

    public void removeMethod(final MethodDesc desc) {
        String key = methodKey(desc, desc.name(), desc.type());
        checkNotTransformed(methods, key, desc);
        removedMethods.add(key);
    }

    public void removeConstructor(final ConstructorDesc desc) {
        String key = methodKey(desc, "<init>", desc.type());
        checkNotTransformed(methods, key, desc);
        removedMethods.add(key);
    }

    public void removeField(final FieldDesc desc) {
        String key = fieldKey(desc);
        checkNotTransformed(fields, key, desc);
        removedFields.add(key);
    }

    public void implements_(final ClassDesc interface_) {
        Assert.checkNotNullParam("interface_", interface_);
        if (!interface_.isClassOrInterface()) {
            throw new IllegalArgumentException("Not an interface type: " + interface_.displayName());
        }
        addedInterfaces.add(interface_);
    }

    byte[] transform(ClassFile cf) {
        ClassTransform transform = new ClassTransform() {
            boolean sawInterfaces;

            public void accept(final ClassBuilder cb, final ClassElement ce) {
                if (ce instanceof AccessFlags af) {
                    cb.withFlags(applyFlags(af.flagsMask()));
                } else if (ce instanceof Interfaces ifs) {
                    sawInterfaces = true;
                    cb.withInterfaceSymbols(mergeInterfaces(ifs.interfaces()));
                } else if (ce instanceof MethodModel mm) {
                    String key = mm.methodName().stringValue() + mm.methodType().stringValue();
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
                    String key = fm.fieldName().stringValue() + ":" + fm.fieldType().stringValue();
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

    private List<ClassDesc> mergeInterfaces(List<ClassEntry> existing) {
        Set<ClassDesc> all = new LinkedHashSet<>();
        for (ClassEntry entry : existing) {
            all.add(entry.asSymbol());
        }
        all.addAll(addedInterfaces);
        return new ArrayList<>(all);
    }

    private String methodKey(MemberDesc desc, String name, MethodTypeDesc methodType) {
        checkOwner(desc);
        String key = name + methodType.descriptorString();
        for (MethodModel mm : model.methods()) {
            if (key.equals(mm.methodName().stringValue() + mm.methodType().stringValue())) {
                return key;
            }
        }
        throw new IllegalArgumentException("No member " + desc + " in " + type.displayName());
    }

    private String fieldKey(FieldDesc desc) {
        checkOwner(desc);
        String key = desc.name() + ":" + desc.type().descriptorString();
        for (FieldModel fm : model.fields()) {
            if (key.equals(fm.fieldName().stringValue() + ":" + fm.fieldType().stringValue())) {
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
