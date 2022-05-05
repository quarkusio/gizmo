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

public interface AnnotationCreator {
    /**
     * Returns an {@link AnnotationCreator} for annotation of given {@code annotationType}.
     * <p>
     * Useful for representing nested annotations (see {@link #addValue(String, Object) addValue}).
     *
     * @param annotationType class name of the annotation to create, must not be {@code null}
     * @return an {@code AnnotationCreator}, never {@code null}
     */
    static AnnotationCreator of(String annotationType) {
        return of(annotationType, RetentionPolicy.RUNTIME);
    }

    /**
     * Returns an {@link AnnotationCreator} for annotation of given {@code annotationType}.
     * <p>
     * Useful for representing nested annotations (see {@link #addValue(String, Object) addValue}).
     *
     * @param annotationType type of the annotation to create, must not be {@code null}
     * @return an {@code AnnotationCreator}, never {@code null}
     */
    static AnnotationCreator of(Class<?> annotationType) {
        Retention retention = annotationType.getAnnotation(Retention.class);
        return of(annotationType.getName(), retention == null ? RetentionPolicy.SOURCE : retention.value());
    }

    /**
     * Returns an {@link AnnotationCreator} for annotation of given {@code annotationType}
     * which has given {@code retentionPolicy}.
     * <p>
     * Useful for representing nested annotations (see {@link #addValue(String, Object) addValue}).
     *
     * @param annotationType class name of the annotation to create, must not be {@code null}
     * @param retentionPolicy retention policy of the annotation type
     * @return an {@code AnnotationCreator}, never {@code null}
     */
    static AnnotationCreator of(String annotationType, RetentionPolicy retentionPolicy) {
        return new AnnotationCreatorImpl(annotationType, retentionPolicy);
    }

    /**
     * Same as {@link #addValue(String, Object)}, but returns {@code this} to allow fluent usage.
     */
    default AnnotationCreator add(String name, Object value) {
        addValue(name, value);
        return this;
    }

    /**
     * Add a new annotation element with the given {@code name} and {@code value}. The name may be any permissible
     * annotation element name (for example, {@code toString} or {@code annotationType} are invalid). The value may be:
     * <ul>
     * <li>primitive wrapper type</li>
     * <li>{@link String}</li>
     * <li>{@link Enum}</li>
     * <li>{@link Class}</li>
     * <li>nested annotation as an {@link AnnotationCreator} created using {@code AnnotationCreator.of()},
     * or as a Jandex {@link org.jboss.jandex.AnnotationInstance AnnotationInstance}</li>
     * <li>array of previously mentioned types</li>
     * </ul>
     * In addition to the types listed above, the value may also be a Jandex
     * {@link org.jboss.jandex.AnnotationValue AnnotationValue}.
     *
     * @param name name of the annotation element to add
     * @param value value of the annotation element to add
     */
    void addValue(String name, Object value);
}
