/*
 * Copyright 2018 Red Hat, Inc.
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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TestClassLoader extends ClassLoader implements ClassOutput {

    private final Map<String, byte[]> appClasses = new HashMap<>();

    public TestClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> ex = findLoadedClass(name);
        if (ex != null) {
            return ex;
        }
        if (appClasses.containsKey(name)) {
            return findClass(name);
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = appClasses.get(name);
        if (bytes == null) {
            throw new ClassNotFoundException();
        }
        return defineClass(name, bytes, 0, bytes.length);
    }

    @Override
    public void write(String name, byte[] data) {
        if (System.getProperty("dumpClass") != null) {
            try {
                File dir = new File("target/test-classes/", name.substring(0, name.lastIndexOf("/")));
                dir.mkdirs();
                File output = new File("target/test-classes/", name + ".class");
                Files.write(output.toPath(), data);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot dump the class: " + name, e);
            }
        }
        appClasses.put(name.replace('/', '.'), data);
    }

    public Writer getSourceWriter(final String className) {
        File dir = new File("target/generated-test-sources/gizmo/", className.substring(0, className.lastIndexOf('/')));
        dir.mkdirs();
        File output = new File("target/generated-test-sources/gizmo/", className + ".zig");
        try {
            return Files.newBufferedWriter(output.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write .zig file for " + className, e);
        }
    }
}
