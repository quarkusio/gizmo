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

import java.util.List;

/**
 * A class that builds the body of a method without needing to understand java bytecode.
 */
public interface MethodCreator
        extends MemberCreator<MethodCreator>, BytecodeCreator, AnnotatedElement, SignatureElement<MethodCreator> {

    /**
     * Adds an exception to the method signature
     *
     * @param exception The exception
     *
     * @return This creator
     */
    MethodCreator addException(String exception);

    /**
     * Adds an exception to the method signature
     *
     * @param exception The exception
     *
     * @return This creator
     */
    default MethodCreator addException(Class<?> exception) {
        return addException(exception.getName());
    }

    /**
     *
     * @return The exceptions thrown by this method
     */
    List<String> getExceptions();

    /**
     *
     * @return The method descriptor
     */
    MethodDescriptor getMethodDescriptor();

    AnnotatedElement getParameterAnnotations(int param);

    /**
     * Sets names of all this method's parameters. The length of {@code parameterNames}
     * must be equal to the number of this method's parameters. Removes previously set
     * parameter names if {@code parameterNames} is {@code null}.
     * <p>
     * It is required to set all parameter names in one go, because parameter names are stored in bytecode
     * as a simple sequence, without positions.
     * <p>
     * When generating a method with mandated or synthetic parameters (such as an inner
     * class constructor), remember that these parameters must have names too.
     *
     * @param parameterNames names of all parameters of this method
     */
    void setParameterNames(String[] parameterNames);

}
