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

/**
 * Wrapper class to represent a parameterized class with its param types.
 */
public class ParameterizedClass {
    private final Object type;
    private final Object[] parameterTypes;

    public ParameterizedClass(Object type, Object... parameterTypes) {
        this.type = type;
        this.parameterTypes = parameterTypes;
    }

    public Object getType() {
        return type;
    }

    public Object[] getParameterTypes() {
        return parameterTypes;
    }
}
