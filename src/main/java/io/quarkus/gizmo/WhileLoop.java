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
 * A while loop statement.
 */
public interface WhileLoop {

    /**
     * The block is executed until the condition evaluates to false.
     * 
     * @return the while block
     */
    BytecodeCreator block();

    /**
     * The scope returned from this method should be used to continue/break the while statement.
     * 
     * <pre>
     * WhileLoop loop = method.whileLoop(bc -> bc.ifTrue(condition));
     * BytecodeCreator block = loop.block();
     * // If counter > 5 we break the loop even if the condition is still true 
     * block.ifIntegerGreaterThan(counter, block.load(5)).trueBranch().breakScope(loop.scope());
     * </pre>
     * 
     * @return the scope that could be used to skip the current iteration or terminate the statement
     * @see BytecodeCreator#continueScope(BytecodeCreator)
     * @see BytecodeCreator#breakScope(BytecodeCreator)
     */
    BytecodeCreator scope();
}