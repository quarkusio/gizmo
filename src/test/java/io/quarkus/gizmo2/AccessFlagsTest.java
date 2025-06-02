package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ModifierFlag;

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
        assertTrue(Arrays.asList(clazz.getInterfaces()).contains(Function.class));
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
            // check some invalid flags
            assertThrows(IllegalArgumentException.class, () -> cc.addFlag(ModifierFlag.STATIC));
            assertThrows(IllegalArgumentException.class, () -> cc.addFlag(ModifierFlag.SYNCHRONIZED));
        });
        Class<?> clazz = tcm.definedClass();
        assertFalse(clazz.isInterface());
        assertTrue(clazz.isSynthetic());
        assertFalse(Modifier.isPublic(clazz.getModifiers()));
        assertFalse(Modifier.isPrivate(clazz.getModifiers()));
        assertFalse(Modifier.isProtected(clazz.getModifiers()));
        assertTrue(Arrays.asList(clazz.getInterfaces()).contains(Function.class));
        assertEquals(Super.class, clazz.getSuperclass());
    }

    @Test
    public void testInterfaceDefaultFlags() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.FooInterface", cc -> {
            cc.extends_(Consumer.class);
        });
        Class<?> clazz = tcm.definedClass();
        assertTrue(clazz.isInterface());
        assertTrue(clazz.isSynthetic());
        assertTrue(Modifier.isPublic(clazz.getModifiers()));
        assertTrue(Modifier.isAbstract(clazz.getModifiers()));
        assertTrue(Arrays.asList(clazz.getInterfaces()).contains(Consumer.class));
        assertNull(clazz.getSuperclass());
    }

    @Test
    public void testPackagePrivateInterface() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_(ClassDesc.of("io.quarkus.gizmo2.FooInterface"), cc -> {
            cc.packagePrivate();
            cc.extends_(Consumer.class);
            assertThrows(IllegalArgumentException.class, () -> cc.setAccess(AccessLevel.PROTECTED));
            assertThrows(IllegalArgumentException.class, () -> cc.addFlag(ModifierFlag.SYNCHRONIZED));
        });
        Class<?> clazz = tcm.definedClass();
        assertTrue(clazz.isInterface());
        assertTrue(clazz.isSynthetic());
        assertFalse(Modifier.isPublic(clazz.getModifiers()));
        assertFalse(Modifier.isPrivate(clazz.getModifiers()));
        assertFalse(Modifier.isProtected(clazz.getModifiers()));
        assertTrue(Modifier.isAbstract(clazz.getModifiers()));
        assertTrue(Arrays.asList(clazz.getInterfaces()).contains(Consumer.class));
        assertNull(clazz.getSuperclass());
    }

    @Test
    public void testClassFieldsFlags() throws NoSuchFieldException, SecurityException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.FooFields", cc -> {
            cc.field("alpha", fc -> {
                fc.public_();
                fc.setType(Integer.class);
            });
            cc.field("bravo", fc -> {
                fc.packagePrivate();
                fc.setType(Double.class);
                // should be a no-op
                fc.removeFlag(ModifierFlag.STATIC);
                assertThrows(IllegalArgumentException.class, () -> fc.addFlag(ModifierFlag.STATIC));
            });
            cc.staticField("charlie", fc -> {
                fc.protected_();
                fc.setType(String.class);
                fc.setInitial(Const.of("oops"));
                assertThrows(IllegalArgumentException.class, () -> fc.addFlag(ModifierFlag.TRANSIENT));
                // should be a no-op
                fc.addFlag(ModifierFlag.STATIC);
                assertThrows(IllegalArgumentException.class, () -> fc.removeFlag(ModifierFlag.STATIC));
            });
            cc.field("delta", fc -> {
                fc.private_();
                fc.final_();
                fc.setType(Long.class);
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
                mc.body(BlockCreator::return_);
            });
            cc.method("bravo", mc -> {
                mc.packagePrivate();
                mc.body(BlockCreator::return_);
            });
            cc.staticMethod("charlie", mc -> {
                mc.final_();
                mc.body(BlockCreator::return_);
            });
            cc.method("delta", mc -> {
                mc.protected_();
                mc.body(BlockCreator::return_);
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
