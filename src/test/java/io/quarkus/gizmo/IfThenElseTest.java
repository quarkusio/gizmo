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
import static org.junit.Assert.fail;

import java.util.function.Function;

import org.junit.Test;

public class IfThenElseTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testIfThenElse() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            AssignableResultHandle ret = method.createVariable(String.class);

            IfThenElse ifValue = method.ifThenElse(Gizmo.equals(method, method.getMethodParam(0), method.load("foo")));

            BytecodeCreator ifFooNext = ifValue.then();
            ifFooNext.assign(ret, ifFooNext.load("FOO!"));

            BytecodeCreator ifBar = ifValue.elseIf(b -> Gizmo.equals(b, method.getMethodParam(0), b.load("bar")));
            ifBar.assign(ret, ifBar.load("BAR!"));

            BytecodeCreator ifBaz = ifValue.elseIf(b -> Gizmo.equals(b, method.getMethodParam(0), b.load("baz")));
            ifBaz.assign(ret, ifBaz.load("BAZ!"));

            BytecodeCreator elseThen = ifValue.elseThen();
            elseThen.assign(ret, elseThen.load("OTHER!"));

            method.returnValue(ret);
        }
        Function<String, String> myInterface = (Function<String, String>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("FOO!", myInterface.apply("foo"));
        assertEquals("BAR!", myInterface.apply("bar"));
        assertEquals("BAZ!", myInterface.apply("baz"));
        assertEquals("OTHER!", myInterface.apply("fooz"));
    }

    @Test
    public void testInvalidaUsage() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            IfThenElse ifValue = method.ifThenElse(method.load(true));
            try {
                ifValue.elseIf(method.load(false));
                fail();
            } catch (IllegalStateException expected) {
                assertEquals("The initial [then] block was not created", expected.getMessage());
            }
            try {
                ifValue.elseThen();
                fail();
            } catch (IllegalStateException expected) {
                assertEquals("The initial [then] block was not created", expected.getMessage());
            }
            ifValue.then();
            ifValue.elseIf(method.load(false));
            try {
                ifValue.then();
                fail();
            } catch (IllegalStateException expected) {
                assertEquals("A following [else/else-if] block was already created", expected.getMessage());
            }
            ifValue.elseThen();
            try {
                ifValue.elseIf(method.load(false));
                fail();
            } catch (IllegalStateException expected) {
                assertEquals("The [else] block was already created", expected.getMessage());
            }
            try {
                ifValue.elseThen();
                fail();
            } catch (IllegalStateException expected) {
                assertEquals("The [else] block was already created", expected.getMessage());
            }
        }
    }

}
