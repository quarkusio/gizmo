/*
 * Copyright 2022 Red Hat, Inc.
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

import java.util.function.Function;

/**
 * An if-then-else construct.
 * <p>
 * This construct is not thread-safe and should not be re-used.
 */
public interface IfThenElse {

    /**
     * This block is executed if the result handle that was passed to {@link BytecodeCreator#ifThenElse(ResultHandle)} is
     * evaluated as {@code true}.
     * 
     * @return the {@code then} block
     */
    BytecodeCreator then();

    /**
     * Creates a new else-if statement. The block is executed if the condition result handle is evaluated as {@code true}.
     * <p>
     * Note that the condition result handle must already exist.
     * 
     * <pre>
     * boolean test = foo.testIsOk();
     * if (someOtherCondition) {
     *     // do action "A"
     * } else if (test) {
     *     // do action "B"
     * }
     * </pre>
     * 
     * If you need to create the condition result handle inside the if-else statement then use the {@link #elseIf(Function)}
     * method instead.
     * 
     * @param condition
     * @return the {@code else-if} block
     */
    BytecodeCreator elseIf(ResultHandle condition);

    /**
     * Creates a new else-if statement. The block is executed if the condition result handle returned from the function is
     * evaluated as {@code true}.
     * <p>
     * The argument of the function represents the block that is executed when the else-if statement is evaluated.
     * <p>
     * In order to generate the bytecode for the "action B" branch in the following example:
     * 
     * <pre>
     * if (someOtherCondition) {
     *     // do action "A"
     * } else if (foo.testIsOk()) {
     *     // do action "B"
     * }
     * </pre>
     * 
     * The code needs to look like:
     * 
     * <pre>
     * IfThenElse ifAction = method.ifThenElse(someOtherCondition);
     * BytecodeCreator ifTestOk = ifValue.elseIf(b -> b.invokeVirtualMethod(testIsOkMethodDescriptor, fooInstance)));
     * // do action "B"
     * </pre>
     * 
     * @param test
     * @return the {@code else-if} block
     */
    BytecodeCreator elseIf(Function<BytecodeCreator, ResultHandle> test);

    /**
     * This block is executed if no condition result handle was evaluated as {@code true}.
     * 
     * @return the {@code else} block
     */
    BytecodeCreator elseThen();

}
