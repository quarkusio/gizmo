/*
 * Copyright 2022 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import io.quarkus.gizmo.Switch.EnumSwitch;
import io.quarkus.gizmo.Switch.StringSwitch;

public class SwitchTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testStringSwitch() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            AssignableResultHandle ret = method.createVariable(String.class);
            // String ret;
            // switch(arg) {
            //     case "boom", "foo" -> ret = "fooo";
            //     case "bar" -> ret = "barr";
            //     case "baz" -> ret = "bazz";
            //     default -> ret = null;
            // }
            // return ret;
            StringSwitch s = method.stringSwitch(method.getMethodParam(0));
            s.caseOf(List.of("boom", "foo"), bc -> {
                bc.assign(ret, bc.load("foooboom"));
            });
            s.caseOf("bar", bc -> {
                bc.assign(ret, bc.load("barr"));
            });
            s.caseOf("baz", bc -> {
                bc.assign(ret, bc.load("bazz"));
            });
            s.defaultCase(bc -> bc.assign(ret, bc.loadNull()));

            method.returnValue(ret);
        }
        Function<String, String> myInterface = (Function<String, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("foooboom", myInterface.apply("boom"));
        assertEquals("foooboom", myInterface.apply("foo"));
        assertEquals("barr", myInterface.apply("bar"));
        assertEquals("bazz", myInterface.apply("baz"));
        assertNull(myInterface.apply("unknown"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStringSwitchFallThrough() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            AssignableResultHandle ret = method.createVariable(String.class);
            // String ret;
            // switch(arg) {
            //     case "boom":
            //     case "foo":
            //          ret = "fooo";
            //          break;
            //      case "bar":
            //          ret = "barr"
            //      case "baz"
            //          ret = "bazz";
            //          break;
            //      default:
            //          ret = null;
            // }
            // return ret;
            StringSwitch s = method.stringSwitch(method.getMethodParam(0));
            s.fallThrough();
            s.caseOf(List.of("boom", "foo"), bc -> {
                bc.assign(ret, bc.load("fooo"));
                s.doBreak(bc);
            });
            s.caseOf("bar", bc -> {
                bc.assign(ret, bc.load("barr"));
            });
            s.caseOf("baz", bc -> {
                bc.assign(ret, bc.load("bazz"));
                s.doBreak(bc);
            });
            s.defaultCase(bc -> bc.assign(ret, bc.loadNull()));

            method.returnValue(ret);
        }
        Function<String, String> myInterface = (Function<String, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("fooo", myInterface.apply("boom"));
        assertEquals("fooo", myInterface.apply("foo"));
        assertEquals("bazz", myInterface.apply("bar"));
        assertEquals("bazz", myInterface.apply("baz"));
        assertNull(myInterface.apply("unknown"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStringSwitchWithHashCollision()
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Test that a couple of string literals that share the same hash code 
        assertEquals("Aa".hashCode(), "BB".hashCode());

        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            AssignableResultHandle ret = method.createVariable(String.class);
            // String ret;
            // switch(arg) {
            //     case "Aa":
            //          ret = "aa";
            //          break;
            //     case "BB":
            //          ret = "bb"
            //          break;
            //      default:
            //          ret = null;
            // }
            // return ret;
            StringSwitch s = method.stringSwitch(method.getMethodParam(0));
            s.fallThrough();
            s.caseOf("Aa", bc -> {
                bc.assign(ret, bc.load("aa"));
                s.doBreak(bc);
            });
            s.caseOf("BB", bc -> {
                bc.assign(ret, bc.load("bb"));
                s.doBreak(bc);
            });
            s.defaultCase(bc -> bc.assign(ret, bc.loadNull()));

            method.returnValue(ret);
        }
        Function<String, String> myInterface = (Function<String, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("aa", myInterface.apply("Aa"));
        assertEquals("bb", myInterface.apply("BB"));
        assertNull(myInterface.apply("unknown"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyStringSwitch() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            AssignableResultHandle ret = method.createVariable(String.class);
            method.assign(ret, method.loadNull());
            method.stringSwitch(method.getMethodParam(0));
            method.returnValue(ret);
        }
        Function<String, String> myInterface = (Function<String, String>) cl.loadClass("com.MyTest").newInstance();
        assertNull(myInterface.apply("foo"));
    }

    @Test
    public void testStringSwitchDuplicateCase() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            AssignableResultHandle ret = method.createVariable(String.class);
            method.assign(ret, method.loadNull());
            StringSwitch s = method.stringSwitch(method.getMethodParam(0));
            try {
                s.caseOf("foo", bc -> {
                });
                s.caseOf("foo", bc -> {
                });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                s.caseOf(List.of("foo"), bc -> {
                });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                s.caseOf(List.of("bar", "baz", "bar"), bc -> {
                });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            method.returnValue(ret);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStringSwitchReturn() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            // switch(arg) {
            //     case "boom":
            //     case "foo":
            //          return "fooo";
            //     case "bar":
            //          return "barr"
            //      default:
            //          return null;
            // }
            StringSwitch s = method.stringSwitch(method.getMethodParam(0));
            s.fallThrough();
            s.caseOf(List.of("boom", "foo"), bc -> {
                bc.returnValue(bc.load("fooo"));
            });
            s.caseOf("bar", bc -> {
                bc.returnValue(bc.load("barr"));
            });
            s.defaultCase(bc -> bc.returnNull());
        }
        Function<String, String> myInterface = (Function<String, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("fooo", myInterface.apply("foo"));
        assertEquals("barr", myInterface.apply("bar"));
        assertNull(myInterface.apply("unknown"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnumSwitch() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            // switch(status) {
            //     case ON, OFF -> return status.toString();
            //     case UNKNOWN -> return "?";
            //     default: -> return null;
            // }
            EnumSwitch<Status> s = method.enumSwitch(method.getMethodParam(0), Status.class);
            s.caseOf(List.of(Status.ON, Status.OFF), bc -> {
                bc.returnValue(Gizmo.toString(bc, method.getMethodParam(0)));
            });
            s.caseOf(Status.UNKNOWN, bc -> {
                bc.returnValue(bc.load("?"));
            });
            s.defaultCase(bc -> bc.returnNull());
        }
        Function<Status, String> myInterface = (Function<Status, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("ON", myInterface.apply(Status.ON));
        assertEquals("OFF", myInterface.apply(Status.OFF));
        assertEquals("?", myInterface.apply(Status.UNKNOWN));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnumSwitchFallThrough() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            AssignableResultHandle ret = method.createVariable(String.class);
            // String ret;
            // switch(status) {
            //     case ON:
            //        ret = "on";
            //     case OFF:
            //        ret = "off";
            //     default:
            //        ret = "??";
            // }
            EnumSwitch<Status> s = method.enumSwitch(method.getMethodParam(0), Status.class);
            s.fallThrough();
            s.caseOf(Status.ON, bc -> bc.assign(ret, bc.load("on")));
            s.caseOf(Status.OFF, bc -> bc.assign(ret, bc.load("off")));
            s.defaultCase(bc -> bc.assign(ret, bc.load("??")));
            method.returnValue(ret);
        }
        Function<Status, String> myInterface = (Function<Status, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("??", myInterface.apply(Status.ON));
        assertEquals("??", myInterface.apply(Status.OFF));
        assertEquals("??", myInterface.apply(Status.UNKNOWN));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnumSwitchMissingConstant() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            // switch(status) {
            //     case OFF:
            //          return status.toString();
            //     case UNKNOWN:
            //          return "?";
            //      default:
            //          return null;
            // }
            EnumSwitch<Status> s = method.enumSwitch(method.getMethodParam(0), Status.class);
            s.caseOf(Status.OFF, bc -> {
                bc.returnValue(bc.load("offf"));
            });
            s.caseOf(Status.UNKNOWN, bc -> {
                bc.returnValue(bc.load("?"));
            });
            s.defaultCase(bc -> bc.returnNull());
        }
        Function<Status, String> myInterface = (Function<Status, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("offf", myInterface.apply(Status.OFF));
        assertEquals("?", myInterface.apply(Status.UNKNOWN));
        assertNull(myInterface.apply(Status.ON));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEmptyEnumSwitch() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            method.enumSwitch(method.getMethodParam(0), Status.class);
            method.returnNull();
        }
        Function<Status, String> myInterface = (Function<Status, String>) cl.loadClass("com.MyTest").newInstance();
        assertNull(myInterface.apply(Status.ON));
    }

    @Test
    public void testEnumSwitchDuplicateCase() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            EnumSwitch<Status> s = method.enumSwitch(method.getMethodParam(0), Status.class);
            try {
                s.caseOf(Status.ON, bc -> {
                });
                s.caseOf(Status.ON, bc -> {
                });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                s.caseOf(List.of(Status.ON), bc -> {
                });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                s.caseOf(List.of(Status.ON, Status.OFF, Status.ON), bc -> {
                });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            method.returnNull();
        }
    }

    public enum Status {
        ON,
        OFF,
        UNKNOWN
    }

}
