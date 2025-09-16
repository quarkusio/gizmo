package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.ClassHierarchyResolver;
import io.quarkus.gizmo2.ClassHierarchyLocator;

public final class ClassHierarchyLocatorResultImpl implements ClassHierarchyLocator.Result {
    public static final ClassHierarchyLocator.Result OF_INTERFACE = new ClassHierarchyLocatorResultImpl(
            ClassHierarchyResolver.ClassHierarchyInfo.ofInterface());

    public static final ClassHierarchyLocator.Result OF_JAVA_LANG_OBJECT = new ClassHierarchyLocatorResultImpl(
            ClassHierarchyResolver.ClassHierarchyInfo.ofClass(null));

    final ClassHierarchyResolver.ClassHierarchyInfo info;

    public ClassHierarchyLocatorResultImpl(ClassHierarchyResolver.ClassHierarchyInfo info) {
        this.info = info;
    }
}
