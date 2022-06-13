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

import org.objectweb.asm.MethodVisitor;

abstract class LoopImpl extends BytecodeCreatorImpl implements Loop {

    LoopImpl(BytecodeCreatorImpl enclosing) {
        super(enclosing);
    }

    protected abstract BranchResult result();

    @Override
    protected void writeOperations(MethodVisitor visitor) {
        result().trueBranch().continueScope(this);
        result().falseBranch().breakScope(this);
        super.writeOperations(visitor);
    }

    @Override
    public BytecodeCreator block() {
        return result().trueBranch();
    }

    @Override
    public void doContinue(BytecodeCreator creator) {
        creator.continueScope(this);
    }

    @Override
    public void doBreak(BytecodeCreator creator) {
        creator.breakScope(this);
    }

}
