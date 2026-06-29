package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.constant.ClassDesc;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.testing.TestClassMaker;

public final class ClassVersionTest {

    private static final int JAVA_17_MAJOR = 61;
    private static final int JAVA_21_MAJOR = 65;

    @Test
    public void defaultVersionIsJava17() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestDefaultVersion");
        g.class_(desc, cc -> {
        });
        int major = readMajorVersion(tcm, desc);
        assertEquals(JAVA_17_MAJOR, major);
    }

    @Test
    public void withVersionSetsGlobalDefault() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withVersion(ClassVersion.V21));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestGlobalV21");
        g.class_(desc, cc -> {
        });
        int major = readMajorVersion(tcm, desc);
        assertEquals(JAVA_21_MAJOR, major);
    }

    @Test
    public void perClassSetVersionOverridesGlobal() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withVersion(ClassVersion.V21));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestPerClassOverride");
        g.class_(desc, cc -> {
            cc.setVersion(ClassVersion.V17);
        });
        int major = readMajorVersion(tcm, desc);
        assertEquals(JAVA_17_MAJOR, major);
    }

    @Test
    public void interfaceVersionFromGizmo() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withVersion(ClassVersion.V21));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestInterfaceV21");
        g.interface_(desc, ic -> {
        });
        int major = readMajorVersion(tcm, desc);
        assertEquals(JAVA_21_MAJOR, major);
    }

    private static int readMajorVersion(TestClassMaker tcm, ClassDesc desc) {
        return tcm.readClass(desc, bytes -> {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.getInt(); // magic
            buf.getShort(); // minor
            return buf.getShort() & 0xFFFF; // major
        });
    }
}
