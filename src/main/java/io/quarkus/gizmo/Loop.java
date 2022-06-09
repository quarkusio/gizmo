/*
 * Copyright 2020 Red Hat, Inc.
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
 * A loop construct.
 */
public interface Loop {

    /**
     * The block inside the loop.
     *
     * @return the block
     */
    BytecodeCreator block();

    /**
     * Writes bytecode into the provided {@link BytecodeCreator} to make it jump back to the
     * start of the loop, effectively issuing a Java 'continue' statement. Generally this
     * will be applied to a branch of an if statement.
     *
     * @param creator The creator that should return to the start of the loop
     */
    void doContinue(BytecodeCreator creator);

    /**
     * Writes bytecode into the provided {@link BytecodeCreator} to make it exit the
     * loop, effectively issuing a Java 'break' statement. Generally this
     * will be applied to a branch of an if statement.
     *
     * @param creator The creator that should break from the loop
     */
    void doBreak(BytecodeCreator creator);

}
