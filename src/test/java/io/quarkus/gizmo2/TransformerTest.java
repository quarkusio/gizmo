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
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
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
    public void testAddMembers() throws Exception {
        FieldDesc name = FieldDesc.of(TARGET, "name", String.class);
        MethodDesc hello = MethodDesc.of(TransformTarget.class, "hello", String.class);
        Class<?> c = transform(ct -> {
            // rename the original and add a replacement with the same signature, like ArC does for intercepted static methods
            ct.method(MethodDesc.of(TransformTarget.class, "secret", String.class), mt -> mt.rename("secret$original"));
            ct.staticMethod("secret", smc -> {
                smc.returning(String.class);
                smc.body(b0 -> b0.return_(b0.withString(Const.of("intercepted "))
                        .concat(b0.invokeStatic(ClassMethodDesc.of(TARGET, "secret$original", String.class)))));
            });
            // a new constructor which sets an existing field
            ct.constructor(con -> {
                ParamVar p = con.parameter("name", String.class);
                con.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), ct.this_());
                    b0.set(ct.this_().field(name), p);
                    b0.return_();
                });
            });
            // a new field, and a new method which uses a lambda
            ct.field("counter", fc -> fc.setType(int.class));
            ct.method("greet", mc -> {
                mc.returning(Object.class);
                mc.body(b0 -> {
                    Expr supplier = b0.lambda(Supplier.class, lc -> {
                        Var self = lc.capture("self", ct.this_());
                        lc.body(b1 -> b1.return_(b1.invokeVirtual(hello, self)));
                    });
                    b0.return_(b0.invokeInterface(MethodDesc.of(Supplier.class, "get", Object.class), supplier));
                });
            });
        });
        Method secret = c.getDeclaredMethod("secret");
        secret.setAccessible(true);
        assertEquals("intercepted secret", secret.invoke(null));

        Constructor<?> ctor = c.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        Object instance = ctor.newInstance("world");
        Method greet = c.getDeclaredMethod("greet");
        greet.setAccessible(true);
        assertEquals("hello world", greet.invoke(instance));
        assertEquals(int.class, c.getDeclaredField("counter").getType());
    }

    @Test
    public void testInvalidAdditions() throws Exception {
        byte[] bytes = readTarget();
        Gizmo g = Gizmo.create();
        // field initializers would have to be merged into the existing initializers
        assertThrows(IllegalArgumentException.class, () -> g.transform(bytes, ct -> ct.staticField("x", fc -> {
            fc.setType(String.class);
            fc.setInitializer(b0 -> b0.yield(Const.of("x")));
        })));
        // clashes with an existing method
        assertThrows(IllegalArgumentException.class, () -> g.transform(bytes, ct -> ct.staticMethod("secret", smc -> {
            smc.returning(String.class);
            smc.body(b0 -> b0.return_(Const.of("x")));
        })));
        // anonymous classes would have to be added to the existing nest
        Gizmo anon = Gizmo.create().withLambdaStrategy(LambdaStrategy.ANONYMOUS_CLASS);
        assertThrows(IllegalArgumentException.class, () -> anon.transform(bytes, ct -> ct.staticMethod("run", smc -> {
            smc.returning(Object.class);
            smc.body(b0 -> b0.return_(b0.lambda(Runnable.class, lc -> lc.body(b1 -> b1.return_()))));
        })));
        // the transformer cannot be used after its builder returns
        ClassTransformer[] escaped = new ClassTransformer[1];
        g.transform(bytes, ct -> escaped[0] = ct);
        assertThrows(IllegalStateException.class, () -> escaped[0].removeField(FieldDesc.of(TARGET, "unused", int.class)));
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
