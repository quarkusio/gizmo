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

class IfThenElseImpl extends BytecodeCreatorImpl implements IfThenElse {

    private byte state;
    private BranchResult result;

    public IfThenElseImpl(BytecodeCreatorImpl enclosing, ResultHandle value) {
        super(enclosing);
        this.state = INIT;
        this.result = enclosing.ifTrue(value);
    }

    @Override
    public BytecodeCreator then() {
        if (state != INIT) {
            throw new IllegalStateException("A following [else/else-if] block was already created");
        }
        state = THEN;
        return result.trueBranch();
    }

    @Override
    public BytecodeCreator elseIf(ResultHandle value) {
        if (state == INIT) {
            throw initialThenNotCreated();
        }
        if (state == ELSE) {
            throw elseAlreadyCreated();
        }
        state = ELSEIF;
        BytecodeCreator falseBranch = result.falseBranch();
        result = falseBranch.ifTrue(value);
        return result.trueBranch();
    }

    @Override
    public BytecodeCreator elseIf(Function<BytecodeCreator, ResultHandle> test) {
        ResultHandle val = test.apply(result.falseBranch());
        return elseIf(val);
    }

    @Override
    public BytecodeCreator elseThen() {
        if (state == INIT) {
            throw initialThenNotCreated();
        }
        if (state == ELSE) {
            throw elseAlreadyCreated();
        }
        state = ELSE;
        return result.falseBranch();
    }

    private IllegalStateException initialThenNotCreated() {
        return new IllegalStateException("The initial [then] block was not created");
    }

    private IllegalStateException elseAlreadyCreated() {
        return new IllegalStateException("The [else] block was already created");
    }

    private static byte INIT = 0;
    private static byte THEN = 1;
    private static byte ELSEIF = 2;
    private static byte ELSE = 3;

}
