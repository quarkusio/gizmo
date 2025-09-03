package io.quarkus.gizmo2.desc;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.impl.Util;

/**
 * A holder class for common JDK descriptors, organized by class.
 */
public final class Descs {
    private Descs() {
    }

    public static final class Object {
        private Object() {
        }

        public static final ClassDesc desc = CD_Object;

        public static final ClassMethodDesc equals = ClassMethodDesc.of(desc, "equals", CD_boolean, CD_Object);
        public static final ClassMethodDesc getClass = ClassMethodDesc.of(desc, "getClass", CD_Class);
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(desc, "hashCode", CD_int);
        public static final ClassMethodDesc toString = ClassMethodDesc.of(desc, "toString", CD_String);
    }

    public static final class Class {
        private Class() {
        }

        public static final ClassDesc desc = CD_Class;

        public static final ClassMethodDesc asSubclass = ClassMethodDesc.of(desc, "asSubclass", CD_Class, CD_Class);
        public static final ClassMethodDesc cast = ClassMethodDesc.of(desc, "cast", CD_Object, CD_Object);
        public static final ClassMethodDesc getClassLoader = ClassMethodDesc.of(desc, "getClassLoader", ClassLoader.desc);
        public static final ClassMethodDesc getName = ClassMethodDesc.of(desc, "getName", CD_String);
        public static final ClassMethodDesc isInterface = ClassMethodDesc.of(desc, "isInterface", CD_boolean);
    }

    public static final class ClassLoader {
        private ClassLoader() {
        }

        public static final ClassDesc desc = Util.classDesc(java.lang.ClassLoader.class);

        public static final ClassMethodDesc loadClass = ClassMethodDesc.of(desc, "loadClass", CD_Class, CD_String);
    }

    public static final class Collection {
        private Collection() {
        }

        public static final ClassDesc desc = Util.classDesc(java.util.Collection.class);

        public static final InterfaceMethodDesc isEmpty = InterfaceMethodDesc.of(desc, "isEmpty", CD_boolean);
        public static final InterfaceMethodDesc size = InterfaceMethodDesc.of(desc, "size", CD_int);
        public static final InterfaceMethodDesc clear = InterfaceMethodDesc.of(desc, "clear", CD_void);
        public static final InterfaceMethodDesc contains = InterfaceMethodDesc.of(desc, "contains", CD_boolean, CD_Object);
    }
}
