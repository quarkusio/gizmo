package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.BlockCreator;

public final class MethodThrowsTest {
    @Test
    public void testThrowsDecl() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestThrowsDecl"), zc -> {
            zc.staticMethod("test", mc -> {
                mc.throws_(RuntimeException.class);
                mc.throws_(ClassNotFoundException.class);
                mc.body(BlockCreator::return_);
            });
        });
        Class<?>[] types = tcm.definedClass().getDeclaredMethod("test").getExceptionTypes();
        assertArrayEquals(new Class<?>[] { RuntimeException.class, ClassNotFoundException.class }, types);
    }
}
