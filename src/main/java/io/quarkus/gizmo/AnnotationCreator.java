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

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Consumer;

public interface AnnotationCreator {

    /**
     * Add a new element with the given {@code name} and {@code value}. If {@code value} is a {@link Map},
     * it is treated as a nested annotation, with the map's entries being the nested annotation's
     * elements' names and values; the map must have an "annotationType" entry whose value is either
     * the class or the class name of the nested annotation. Alternatively,
     * {@link AnnotationCreator#addNested(String, String)} or {@link AnnotationCreator#addNested(String, Class)} can
     * be used to create nested annotations.
     *
     * @param name The name of the annotation element to add
     * @param value The value of the annotation element to add
     */
    void addValue(String name, Object value);

    /**
     * Adds a nested annotation element, and return a new {@link AnnotationCreator} to populate it.
     *
     * @param name The name of the nested annotation element
     * @param annotationType The class name of the nested annotation
     * @return A new {@link AnnotationCreator} that can be used to populate the nested annotation.
     */
    AnnotationCreator addNested(String name, String annotationType);

    /**
     * Adds a nested annotation element, and return a new {@link AnnotationCreator} to populate it.
     *
     * @param name The name of the nested annotation element
     * @param annotationType The class of the nested annotation
     * @return A new {@link AnnotationCreator} that can be used to populate the nested annotation.
     */
    default AnnotationCreator addNested(String name, Class<? extends Annotation> annotationType) {
        return addNested(name, annotationType.getName());
    }

    /**
     * Adds an array of nested annotation element, using the given AnnotationCreator consumers to generate it.
     *
     * @param name The name of the nested annotation element
     * @param annotationType The class name of the nested annotation
     * @param annotationArrayCreator Consumers that generate the elements of the array
     */
    void addNestedArray(String name, String annotationType, AnnotationCreatorConsumer... annotationArrayCreator);

    /**
     * Adds an array of nested annotation element, using the given AnnotationCreator consumers to generate it.
     *
     * @param name The name of the nested annotation element
     * @param annotationType The class of the nested annotation
     * @param annotationArrayCreator Consumers that generate the elements of the array
     */

    default void addNestedArray(String name, Class<? extends Annotation> annotationType, AnnotationCreatorConsumer... annotationArrayCreator) {
        addNestedArray(name, annotationType.getName(), annotationArrayCreator);
    }

    interface AnnotationCreatorConsumer extends Consumer<AnnotationCreator> {}
}
