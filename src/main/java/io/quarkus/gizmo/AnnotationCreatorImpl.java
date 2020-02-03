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

import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;
import java.util.Map;

class AnnotationCreatorImpl implements AnnotationCreator {

    private Map<String, Object> values = new LinkedHashMap<>();
    private final String annotationType;
    private final RetentionPolicy retentionPolicy;

    AnnotationCreatorImpl(String annotationType, RetentionPolicy retentionPolicy) {
        this.annotationType = annotationType;
        if (retentionPolicy == RetentionPolicy.SOURCE) {
            throw new IllegalArgumentException("Unsupported retention policy SOURCE");
        }
        this.retentionPolicy = retentionPolicy;
    }

    @Override
    public void addValue(String name, Object value) {
        values.put(name, value);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public RetentionPolicy getRetentionPolicy() {
        return retentionPolicy;
    }
}
