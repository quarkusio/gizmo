package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;

public final class LockingTest {
    @Test
    public void testMonitorIsAcquired() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_(ClassDesc.of("io.quarkus.gizmo2.TestMonitorIsAcquired"), zc -> {
            zc.public_();
            zc.staticMethod("testMonitor", mc -> {
                ParamVar monitor = mc.parameter("monitor", Object.class);
                ParamVar action1 = mc.parameter("action1", Runnable.class);
                ParamVar action2 = mc.parameter("action2", Runnable.class);
                mc.body(b0 -> {
                    b0.synchronized_(monitor, b1 -> {
                        b1.invokeInterface(MethodDesc.of(Runnable.class, "run", void.class), action1);
                    });
                    b0.invokeInterface(MethodDesc.of(Runnable.class, "run", void.class), action2);
                    b0.return_();
                });
            });
        });
        Object monitor = new Object();
        tcm.staticMethod(desc, "testMonitor", TestMonitor.class).run(
                monitor,
                () -> {
                    assertTrue(Thread.holdsLock(monitor));
                },
                () -> {
                    assertFalse(Thread.holdsLock(monitor));
                });
        assertThrows(IllegalStateException.class, () -> tcm.staticMethod(desc, "testMonitor", TestMonitor.class).run(
                monitor,
                () -> {
                    assertTrue(Thread.holdsLock(monitor));
                    throw new IllegalStateException();
                },
                () -> {
                    fail("Not reachable");
                }));
        assertFalse(Thread.holdsLock(monitor));
    }

    public interface TestMonitor {
        void run(Object monitor, Runnable action1, Runnable action2);
    }

    @Test
    public void testLockIsAcquired() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_(ClassDesc.of("io.quarkus.gizmo2.TestLockIsAcquired"), zc -> {
            zc.public_();
            zc.staticMethod("testLock", mc -> {
                ParamVar lock = mc.parameter("lock", Lock.class);
                ParamVar action1 = mc.parameter("action1", Runnable.class);
                ParamVar action2 = mc.parameter("action2", Runnable.class);
                mc.body(b0 -> {
                    b0.locked(lock, b1 -> {
                        b1.invokeInterface(MethodDesc.of(Runnable.class, "run", void.class), action1);
                    });
                    b0.invokeInterface(MethodDesc.of(Runnable.class, "run", void.class), action2);
                    b0.return_();
                });
            });
        });
        ReentrantLock lock = new ReentrantLock();
        tcm.staticMethod(desc, "testLock", TestLock.class).run(
                lock,
                () -> {
                    assertTrue(lock.isHeldByCurrentThread());
                },
                () -> {
                    assertFalse(lock.isHeldByCurrentThread());
                });
        assertThrows(IllegalStateException.class, () -> tcm.staticMethod(desc, "testLock", TestLock.class).run(
                lock,
                () -> {
                    assertTrue(lock.isHeldByCurrentThread());
                    throw new IllegalStateException();
                },
                () -> {
                    fail("Not reachable");
                }));
        assertFalse(lock.isHeldByCurrentThread());
    }

    public interface TestLock {
        void run(Lock lock, Runnable action1, Runnable action2);
    }
}
