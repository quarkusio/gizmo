package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.testing.TestClassMaker;

public final class CollectionConstantsTest {
    @Test
    public void testConstantStringList() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallList");
        List<Object> emptyList = List.of();
        List<String> threeList = List.of("Foo", "Bar", "Baz");
        List<String> elevenList = List.of("Foo", "Bar", "Baz", "Zap", "Quux", "Thud", "Gorp", "Fom", "Zop", "Zork", "Gork");
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            // case: empty
            zc.staticMethod("test0", mc -> {
                mc.returning(List.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(emptyList));
                });
            });
            // case: small number of args
            zc.staticMethod("test1", mc -> {
                mc.returning(List.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(threeList));
                });
            });
            // case: big enough to force varargs
            zc.staticMethod("test2", mc -> {
                mc.returning(List.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(elevenList));
                });
            });
        });
        assertEquals(emptyList, tcm.staticMethod(desc, "test0", StringListTest.class).get());
        assertEquals(threeList, tcm.staticMethod(desc, "test1", StringListTest.class).get());
        assertEquals(elevenList, tcm.staticMethod(desc, "test2", StringListTest.class).get());
    }

    @Test
    public void testConstantClassList() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallList");
        List<Class<?>> threeList = List.of(Object.class, String.class, Class.class);
        List<Class<?>> elevenList = List.of(Object.class, String.class, Class.class, Integer.class,
                Byte.class, Float.class, List.class, Throwable.class, Long.class, Double.class, Void.class);
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            // case: small number of args
            zc.staticMethod("test0", mc -> {
                mc.returning(List.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(threeList));
                });
            });
            // case: big enough to force varargs
            zc.staticMethod("test1", mc -> {
                mc.returning(List.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(elevenList));
                });
            });
        });
        assertEquals(threeList, tcm.staticMethod(desc, "test0", ClassListTest.class).get());
        assertEquals(elevenList, tcm.staticMethod(desc, "test1", ClassListTest.class).get());
    }

    @Test
    public void testConstantStringSet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallSet");
        Set<Object> emptySet = Set.of();
        Set<String> threeSet = Set.of("Foo", "Bar", "Baz");
        Set<String> elevenSet = Set.of("Foo", "Bar", "Baz", "Zap", "Quux", "Thud", "Gorp", "Fom", "Zop", "Zork", "Gork");
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            // case: empty
            zc.staticMethod("test0", mc -> {
                mc.returning(Set.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(emptySet));
                });
            });
            // case: small number of args
            zc.staticMethod("test1", mc -> {
                mc.returning(Set.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(threeSet));
                });
            });
            // case: big enough to force varargs
            zc.staticMethod("test2", mc -> {
                mc.returning(Set.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(elevenSet));
                });
            });
        });
        assertEquals(emptySet, tcm.staticMethod(desc, "test0", StringSetTest.class).get());
        assertEquals(threeSet, tcm.staticMethod(desc, "test1", StringSetTest.class).get());
        assertEquals(elevenSet, tcm.staticMethod(desc, "test2", StringSetTest.class).get());
    }

    @Test
    public void testConstantClassSet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallSet");
        Set<Class<?>> threeSet = Set.of(Object.class, String.class, Class.class);
        Set<Class<?>> elevenSet = Set.of(Object.class, String.class, Class.class, Integer.class,
                Byte.class, Float.class, Set.class, Throwable.class, Long.class, Double.class, Void.class);
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            // case: small number of args
            zc.staticMethod("test0", mc -> {
                mc.returning(Set.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(threeSet));
                });
            });
            // case: big enough to force varargs
            zc.staticMethod("test1", mc -> {
                mc.returning(Set.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(elevenSet));
                });
            });
        });
        assertEquals(threeSet, tcm.staticMethod(desc, "test0", ClassSetTest.class).get());
        assertEquals(elevenSet, tcm.staticMethod(desc, "test1", ClassSetTest.class).get());
    }

    @Test
    public void testConstantStringMap() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallMap");
        Map<String, String> emptyMap = Map.of();
        Collector<String, ?, Map<String, String>> collector = Collectors.toMap(Function.identity(), Function.identity());
        Map<String, String> threeMap = Stream.of("Foo", "Bar", "Baz").collect(collector);
        Map<String, String> elevenMap = Stream
                .of("Foo", "Bar", "Baz", "Zap", "Quux", "Thud", "Gorp", "Fom", "Zop", "Zork", "Gork").collect(collector);
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            // case: empty
            zc.staticMethod("test0", mc -> {
                mc.returning(Map.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(emptyMap));
                });
            });
            // case: small number of args
            zc.staticMethod("test1", mc -> {
                mc.returning(Map.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(threeMap));
                });
            });
            // case: big enough to force varargs
            zc.staticMethod("test2", mc -> {
                mc.returning(Map.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(elevenMap));
                });
            });
        });
        assertEquals(emptyMap, tcm.staticMethod(desc, "test0", StringMapTest.class).get());
        assertEquals(threeMap, tcm.staticMethod(desc, "test1", StringMapTest.class).get());
        assertEquals(elevenMap, tcm.staticMethod(desc, "test2", StringMapTest.class).get());
    }

    @Test
    public void testConstantClassMap() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallMap");
        Collector<Class<?>, ?, Map<String, Class<?>>> collector = Collectors.toMap(Class::getName, Function.identity());
        Map<String, Class<?>> threeMap = Stream.of(Object.class, String.class, Class.class).collect(collector);
        Map<String, Class<?>> elevenMap = Stream.of(Object.class, String.class, Class.class, Integer.class,
                Byte.class, Float.class, Map.class, Throwable.class, Long.class, Double.class, Void.class).collect(collector);
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            // case: small number of args
            zc.staticMethod("test0", mc -> {
                mc.returning(Map.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(threeMap));
                });
            });
            // case: big enough to force varargs
            zc.staticMethod("test1", mc -> {
                mc.returning(Map.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(Const.of(elevenMap));
                });
            });
        });
        assertEquals(threeMap, tcm.staticMethod(desc, "test0", ClassMapTest.class).get());
        assertEquals(elevenMap, tcm.staticMethod(desc, "test1", ClassMapTest.class).get());
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testResourceStringList() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallList");
        List<String> big1 = List.of(
                "Lorem ipsum dolor sit amet,",
                "consectetur adipiscing elit,",
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "Ut enim ad minim veniam,",
                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                "Excepteur sint occaecat cupidatat non proident,",
                "sunt in culpa qui officia deserunt mollit anim id est laborum.",
                "Lorem ipsum dolor sit amet,",
                "consectetur adipiscing elit,",
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "Ut enim ad minim veniam,",
                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                "Excepteur sint occaecat cupidatat non proident,",
                "sunt in culpa qui officia deserunt mollit anim id est laborum.");
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            zc.staticMethod("test0", mc -> {
                mc.returning(List.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(zc.stringListResourceConstant("big1", big1));
                });
            });
        });
        assertEquals(big1, tcm.staticMethod(desc, "test0", StringListTest.class).get());
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testResourceStringSet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallSet");
        Set<String> big1 = Set.of(
                "Lorem ipsum dolor sit amet,",
                "consectetur adipiscing elit,",
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "Ut enim ad minim veniam,",
                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                "Excepteur sint occaecat cupidatat non proident,",
                "sunt in culpa qui officia deserunt mollit anim id est laborum.");
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            zc.staticMethod("test0", mc -> {
                mc.returning(Set.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(zc.stringSetResourceConstant("big1", big1));
                });
            });
        });
        assertEquals(big1, tcm.staticMethod(desc, "test0", StringSetTest.class).get());
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testResourceStringMap() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestSmallMap");
        Map<String, String> big1 = Stream.of(
                "Lorem ipsum dolor sit amet,",
                "consectetur adipiscing elit,",
                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "Ut enim ad minim veniam,",
                "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                "Excepteur sint occaecat cupidatat non proident,",
                "sunt in culpa qui officia deserunt mollit anim id est laborum.").collect(
                        Collectors.toMap(
                                Function.identity(), Function.identity()));
        g.class_(desc, zc -> {
            zc.sourceFile(file());
            zc.staticMethod("test0", mc -> {
                mc.returning(Map.class);
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.return_(zc.stringMapResourceConstant("big1", big1));
                });
            });
        });
        assertEquals(big1, tcm.staticMethod(desc, "test0", StringMapTest.class).get());
    }

    public interface StringListTest {
        List<String> get();
    }

    public interface StringSetTest {
        Set<String> get();
    }

    public interface StringMapTest {
        Map<String, String> get();
    }

    public interface ClassListTest {
        List<Class<?>> get();
    }

    public interface ClassSetTest {
        Set<Class<?>> get();
    }

    public interface ClassMapTest {
        Map<String, Class<?>> get();
    }

    private static final StackWalker SW = StackWalker.getInstance();

    // get the line # after the call to this method
    private static int nextLine() {
        return SW.walk(s -> s.skip(1).findFirst().orElseThrow()).getLineNumber() + 1;
    }

    // get my source file name
    private static String file() {
        return SW.walk(s -> s.skip(1).findFirst().orElseThrow()).getFileName();
    }
}
