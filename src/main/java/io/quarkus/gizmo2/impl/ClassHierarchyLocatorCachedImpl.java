package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.constant.ClassDesc;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import io.quarkus.gizmo2.ClassHierarchyLocator;

public final class ClassHierarchyLocatorCachedImpl implements ClassHierarchyLocator {
    // using an extra object because certain `Map`s do not support `null` values
    private static final ClassHierarchyLocator.Result NULL = new ClassHierarchyLocatorResultImpl(null);

    private final Map<ClassDesc, Result> cache;
    private final Function<ClassDesc, Result> getter;

    public ClassHierarchyLocatorCachedImpl(ClassHierarchyLocator locator, Supplier<Map<ClassDesc, Result>> cacheFactory) {
        Map<ClassDesc, Result> cache = cacheFactory.get();
        checkNotNullParam("cache", cache);
        this.cache = cache;
        this.getter = locator::locate;
    }

    @Override
    public Result locate(ClassDesc clazz) {
        Result result = cache.computeIfAbsent(clazz, getter);
        return result == NULL ? null : result;
    }
}
