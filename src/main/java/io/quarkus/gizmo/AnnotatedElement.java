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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

public interface AnnotatedElement {

    default AnnotationCreator addAnnotation(String annotationType) {
        return addAnnotation(annotationType, RetentionPolicy.RUNTIME);
    }

    AnnotationCreator addAnnotation(String annotationType, RetentionPolicy retentionPolicy);

    default AnnotationCreator addAnnotation(Class<?> annotationType) {
        Retention retention = annotationType.getAnnotation(Retention.class);
        return addAnnotation(annotationType.getName(), retention == null ? RetentionPolicy.CLASS : retention.value());
    }

    default void addAnnotation(AnnotationInstance annotation) {
        RetentionPolicy retention = annotation.runtimeVisible() ? RetentionPolicy.RUNTIME : RetentionPolicy.CLASS;
        AnnotationCreator ac = addAnnotation(annotation.name().toString(), retention);
        for (AnnotationValue member : annotation.values()) {
            ac.addValue(member.name(), member);
        }
    }

}
