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

class ForEachLoopImpl extends LoopImpl implements ForEachLoop {

    private final ResultHandle element;
    private final BranchResult result;

    ForEachLoopImpl(BytecodeCreatorImpl enclosing, ResultHandle iterable) {
        super(enclosing);
        ResultHandle iterator = Gizmo.iterableOperations(enclosing).on(iterable).iterator();
        this.result = ifTrue(Gizmo.iteratorOperations(this).on(iterator).hasNext());
        this.element = Gizmo.iteratorOperations(result.trueBranch()).on(iterator).next();
    }

    @Override
    public ResultHandle element() {
        return element;
    }

    @Override
    protected BranchResult result() {
        return result;
    }

}
