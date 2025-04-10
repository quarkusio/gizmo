package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class AccessFlagsTest {

    @Test
    public void testClassDefaultFlags() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Foo"), cc -> {
            cc.implements_(Function.class);
            cc.extends_(Super.class);
        });
        Class<?> clazz = tcm.definedClass();
        assertFalse(clazz.isInterface());
        assertTrue(clazz.isSynthetic());
        assertTrue(Modifier.isPublic(clazz.getModifiers()));
        assertTrue(Arrays.stream(clazz.getInterfaces()).anyMatch(c -> c.equals(Function.class)));
        assertEquals(Super.class, clazz.getSuperclass());
    }

    @Test
    public void testPackagePrivateClass() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Foo"), cc -> {
            cc.packagePrivate();
            cc.implements_(Function.class);
            cc.extends_(Super.class);
        });
        Class<?> clazz = tcm.definedClass();
        assertFalse(clazz.isInterface());
        assertTrue(clazz.isSynthetic());
        assertFalse(Modifier.isPublic(clazz.getModifiers()));
        assertFalse(Modifier.isPrivate(clazz.getModifiers()));
        assertFalse(Modifier.isProtected(clazz.getModifiers()));
        assertTrue(Arrays.stream(clazz.getInterfaces()).anyMatch(c -> c.equals(Function.class)));
        assertEquals(Super.class, clazz.getSuperclass());
    }

    @Test
    public void testInterfaceDefaultFlags() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.FooInterface", cc -> {
            cc.implements_(Consumer.class);
        });
        Class<?> clazz = tcm.definedClass();
        assertTrue(clazz.isInterface());
        assertTrue(clazz.isSynthetic());
        assertTrue(Modifier.isPublic(clazz.getModifiers()));
        assertTrue(Modifier.isAbstract(clazz.getModifiers()));
        assertTrue(Arrays.stream(clazz.getInterfaces()).anyMatch(c -> c.equals(Consumer.class)));
        assertNull(clazz.getSuperclass());
    }

    @Test
    public void testPackagePrivateInterface() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_(ClassDesc.of("io.quarkus.gizmo2.FooInterface"), cc -> {
            cc.packagePrivate();
            cc.implements_(Consumer.class);
        });
        Class<?> clazz = tcm.definedClass();
        assertTrue(clazz.isInterface());
        assertTrue(clazz.isSynthetic());
        assertFalse(Modifier.isPublic(clazz.getModifiers()));
        assertFalse(Modifier.isPrivate(clazz.getModifiers()));
        assertFalse(Modifier.isProtected(clazz.getModifiers()));
        assertTrue(Modifier.isAbstract(clazz.getModifiers()));
        assertTrue(Arrays.stream(clazz.getInterfaces()).anyMatch(c -> c.equals(Consumer.class)));
        assertNull(clazz.getSuperclass());
    }

    @Test
    public void testClassFieldsFlags() throws NoSuchFieldException, SecurityException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.FooFields", cc -> {
            cc.field("alpha", fc -> {
                fc.public_();
                fc.withType(Integer.class);
            });
            cc.field("bravo", fc -> {
                fc.packagePrivate();
                fc.withType(Double.class);
            });
            cc.staticField("charlie", fc -> {
                fc.protected_();
                fc.withType(String.class);
                fc.withInitial(Constant.of("oops"));
            });
            cc.field("delta", fc -> {
                fc.private_();
                fc.final_();
                fc.withType(Long.class);
            });
        });
        Class<?> clazz = tcm.definedClass();
        assertTrue(clazz.isSynthetic());
        assertTrue(Modifier.isPublic(clazz.getModifiers()));

        Field alpha = clazz.getDeclaredField("alpha");
        assertTrue(Modifier.isPublic(alpha.getModifiers()));
        assertEquals(Integer.class, alpha.getType());

        Field bravo = clazz.getDeclaredField("bravo");
        assertFalse(Modifier.isPublic(bravo.getModifiers()));
        assertFalse(Modifier.isPrivate(bravo.getModifiers()));
        assertFalse(Modifier.isProtected(bravo.getModifiers()));
        assertEquals(Double.class, bravo.getType());

        Field charlie = clazz.getDeclaredField("charlie");
        assertTrue(Modifier.isStatic(charlie.getModifiers()));
        assertFalse(Modifier.isPrivate(charlie.getModifiers()));
        assertTrue(Modifier.isProtected(charlie.getModifiers()));
        assertEquals(String.class, charlie.getType());

        Field delta = clazz.getDeclaredField("delta");
        assertFalse(Modifier.isStatic(delta.getModifiers()));
        assertTrue(Modifier.isPrivate(delta.getModifiers()));
        assertTrue(Modifier.isFinal(delta.getModifiers()));
        assertEquals(Long.class, delta.getType());
    }

    @Test
    public void testClassMethodFlags() throws NoSuchMethodException, SecurityException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.FooMethods", cc -> {
            cc.method("alpha", mc -> {
                mc.public_();
                mc.body(bc -> bc.return_());
            });
            cc.method("bravo", mc -> {
                mc.packagePrivate();
                mc.body(bc -> bc.return_());
            });
            cc.staticMethod("charlie", mc -> {
                mc.final_();
                mc.body(bc -> bc.return_());
            });
            cc.method("delta", mc -> {
                mc.protected_();
                mc.body(bc -> bc.return_());
            });
        });
        Class<?> clazz = tcm.definedClass();
        assertTrue(clazz.isSynthetic());
        assertTrue(Modifier.isPublic(clazz.getModifiers()));

        Method alpha = clazz.getDeclaredMethod("alpha");
        assertTrue(Modifier.isPublic(alpha.getModifiers()));
        assertEquals(void.class, alpha.getReturnType());

        Method bravo = clazz.getDeclaredMethod("bravo");
        assertFalse(Modifier.isPublic(bravo.getModifiers()));
        assertFalse(Modifier.isPrivate(bravo.getModifiers()));
        assertFalse(Modifier.isProtected(bravo.getModifiers()));
        assertEquals(void.class, bravo.getReturnType());

        Method charlie = clazz.getDeclaredMethod("charlie");
        assertTrue(Modifier.isStatic(charlie.getModifiers()));
        assertFalse(Modifier.isPrivate(charlie.getModifiers()));
        assertTrue(Modifier.isFinal(charlie.getModifiers()));
        assertEquals(void.class, charlie.getReturnType());

        Method delta = clazz.getDeclaredMethod("delta");
        assertFalse(Modifier.isPrivate(delta.getModifiers()));
        assertFalse(Modifier.isStatic(delta.getModifiers()));
        assertTrue(Modifier.isProtected(delta.getModifiers()));
        assertEquals(void.class, delta.getReturnType());
    }

    public static class Super {

    }
}
