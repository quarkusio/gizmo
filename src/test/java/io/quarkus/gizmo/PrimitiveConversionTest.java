/*
 * Copyright 2024 Red Hat, Inc.
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

import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class PrimitiveConversionTest {
    public interface Primitives {
        byte getbyte();
        short getshort();
        int getint();
        long getlong();
        float getfloat();
        double getdouble();
        char getchar();
    }

    // skipping `boolean` because there are no primitive conversions to/from boolean (except identity)
    private static final List<Class<?>> PRIMITIVES = List.of(byte.class, short.class, int.class, long.class,
            float.class, double.class, char.class);

    @Test
    public void fromByte() throws Exception {
        test(m -> m.load((byte) 42));
    }

    @Test
    public void fromShort() throws Exception {
        test(m -> m.load((short) 42));
    }

    @Test
    public void fromChar() throws Exception {
        test(m -> m.load((char) 42));
    }

    @Test
    public void fromInt() throws Exception {
        test(m -> m.load(42));
    }

    @Test
    public void fromLong() throws Exception {
        test(m -> m.load(42L));
    }

    @Test
    public void fromFloat() throws Exception {
        test(m -> m.load(42.0F));
    }

    @Test
    public void fromDouble() throws Exception {
        test(m -> m.load(42.0));
    }

    private void test(Function<MethodCreator, ResultHandle> load42) throws ReflectiveOperationException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder()
                .classOutput(cl)
                .className("com.MyTest")
                .interfaces(Primitives.class)
                .build()) {
            for (Class<?> primitive : PRIMITIVES) {
                MethodCreator m = creator.getMethodCreator("get" + primitive.getName(), primitive);
                ResultHandle val = load42.apply(m);
                ResultHandle converted = m.convertPrimitive(val, primitive);
                m.returnValue(converted);
            }
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Primitives primitives = (Primitives) clazz.getDeclaredConstructor().newInstance();

        assertEquals((byte) 42, primitives.getbyte());
        assertEquals((short) 42, primitives.getshort());
        assertEquals((char) 42, primitives.getchar());
        assertEquals(42, primitives.getint());
        assertEquals(42L, primitives.getlong());
        assertEquals(42.0F, primitives.getfloat(), 0.0001F);
        assertEquals(42.0, primitives.getdouble(), 0.0001);
    }
}
