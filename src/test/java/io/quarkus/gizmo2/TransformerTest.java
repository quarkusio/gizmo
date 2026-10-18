package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.transformer.ClassTransformer;

public class TransformerTest {
    private static final ClassDesc TARGET = ClassDesc.of(TransformTarget.class.getName());

    @Test
    public void testClassFlagsInterfacesAndAnnotations() throws Exception {
        Class<?> c = transform(ct -> {
            ct.removeFlag(ModifierFlag.FINAL);
            ct.public_();
            ct.removeAnnotation(Deprecated.class);
            ct.implements_(Serializable.class);
        });
        assertFalse(Modifier.isFinal(c.getModifiers()));
        assertTrue(Modifier.isPublic(c.getModifiers()));
        assertFalse(c.isAnnotationPresent(Deprecated.class));
        assertEquals(List.of(Serializable.class), List.of(c.getInterfaces()));
    }

    @Test
    public void testMethodAndConstructor() throws Exception {
        Class<?> c = transform(ct -> {
            ct.constructor(ConstructorDesc.of(TARGET), mt -> mt.packagePrivate());
            ct.method(MethodDesc.of(TransformTarget.class, "hello", String.class), mt -> {
                mt.removeFlag(ModifierFlag.FINAL);
                mt.rename("hello$original");
            });
            ct.method(MethodDesc.of(TransformTarget.class, "secret", String.class), mt -> mt.public_());
        });
        Constructor<?> ctor = c.getDeclaredConstructor();
        assertFalse(Modifier.isPrivate(ctor.getModifiers()));
        ctor.setAccessible(true);
        Object instance = ctor.newInstance();

        assertThrows(NoSuchMethodException.class, () -> c.getDeclaredMethod("hello"));
        Method renamed = c.getDeclaredMethod("hello$original");
        assertFalse(Modifier.isFinal(renamed.getModifiers()));
        // the code is copied unchanged
        renamed.setAccessible(true);
        assertEquals("hello target", renamed.invoke(instance));

        Method secret = c.getDeclaredMethod("secret");
        assertTrue(Modifier.isPublic(secret.getModifiers()));
        assertTrue(Modifier.isStatic(secret.getModifiers()));
        secret.setAccessible(true);
        assertEquals("secret", secret.invoke(null));
    }

    @Test
    public void testField() throws Exception {
        Class<?> c = transform(ct -> ct.field(FieldDesc.of(TransformTarget.class, "name"), ft -> {
            ft.public_();
            ft.removeAnnotation(Deprecated.class);
        }));
        Field name = c.getDeclaredField("name");
        assertTrue(Modifier.isPublic(name.getModifiers()));
        assertFalse(name.isAnnotationPresent(Deprecated.class));
    }

    @Test
    public void testRemoveMembers() throws Exception {
        Class<?> c = transform(ct -> {
            ct.removeMethod(MethodDesc.of(TransformTarget.class, "gone", void.class));
            ct.removeField(FieldDesc.of(TransformTarget.class, "unused"));
        });
        assertThrows(NoSuchMethodException.class, () -> c.getDeclaredMethod("gone"));
        assertThrows(NoSuchFieldException.class, () -> c.getDeclaredField("unused"));
        // other members are untouched
        assertNotNull(c.getDeclaredMethod("hello"));
        assertNotNull(c.getDeclaredField("name"));
    }

    @Test
    public void testInvalidRequests() throws Exception {
        byte[] bytes = readTarget();
        Gizmo g = Gizmo.create();
        // no such member
        assertThrows(IllegalArgumentException.class, () -> g.transform(bytes,
                ct -> ct.method(MethodDesc.of(TransformTarget.class, "missing", void.class), mt -> {
                })));
        // member of another class
        assertThrows(IllegalArgumentException.class, () -> g.transform(bytes,
                ct -> ct.removeMethod(MethodDesc.of(Object.class, "toString", String.class))));
        // constructors cannot be renamed
        assertThrows(IllegalArgumentException.class, () -> g.transform(bytes,
                ct -> ct.constructor(ConstructorDesc.of(TARGET), mt -> mt.rename("foo"))));
        // a member cannot be both transformed and removed
        MethodDesc gone = MethodDesc.of(TransformTarget.class, "gone", void.class);
        assertThrows(IllegalArgumentException.class, () -> g.transform(bytes, ct -> {
            ct.method(gone, mt -> mt.private_());
            ct.removeMethod(gone);
        }));
    }

    private static Class<?> transform(Consumer<ClassTransformer> builder) throws IOException {
        byte[] transformed = Gizmo.create().transform(readTarget(), builder);
        return new Loader().define(TransformTarget.class.getName(), transformed);
    }

    private static byte[] readTarget() throws IOException {
        try (InputStream is = TransformTarget.class.getResourceAsStream("TransformTarget.class")) {
            return is.readAllBytes();
        }
    }

    static final class Loader extends ClassLoader {
        Loader() {
            super(TransformerTest.class.getClassLoader());
        }

        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
