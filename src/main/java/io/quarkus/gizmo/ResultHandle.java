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

import java.util.Objects;

/**
 * Represents the result of an operation. Generally this will be the result of a method
 * that has been stored in a local variable, but it can also be other things, such as a read
 * from a field.
 * <p>
 * These result handles are tied to a specific {@link MethodCreator}.
 */
public class ResultHandle {

    static final ResultHandle NULL = new ResultHandle("Ljava/lang/Object;", null, null);

    private int no;
    private final String type;
    private final BytecodeCreatorImpl owner;
    private final Object constant;
    private ResultType resultType;

    ResultHandle(String type, BytecodeCreatorImpl owner) {
        this.type = type;
        this.owner = owner;
        this.constant = null;
        this.resultType = ResultType.UNUSED;
        verifyType(type);
    }

    //params need to be in a different order to avoid ambiguity
    ResultHandle(String type, BytecodeCreatorImpl owner, Object constant) {
        if (owner != null) {
            Objects.requireNonNull(constant);
        }
        this.type = type;
        this.no = -1;
        this.owner = owner;
        this.constant = constant;
        this.resultType = ResultType.CONSTANT;
        verifyType(type);
    }

    private void verifyType(String current) {
        if (current.isEmpty()) {
            throw new RuntimeException("Invalid type " + type);
        }
        int length = current.length();
        char firstChar = current.charAt(0);
        if (length == 1) {
            switch (firstChar) {
                case 'Z':
                case 'B':
                case 'S':
                case 'I':
                case 'J':
                case 'F':
                case 'D':
                case 'C':
                    return;
                default:
                    throw new RuntimeException("Invalid type " + type);
            }
        } else {
            if (firstChar == '[') {
                verifyType(current.substring(1));
            } else {
                if (!(firstChar == 'L' && current.charAt(length - 1) == ';')) {
                    throw new RuntimeException("Invalid type " + type);
                }
            }
        }

    }

    public void setNo(int no) {
        this.no = no;
        this.resultType = ResultType.LOCAL_VARIABLE;
    }

    public ResultType getResultType() {
        return resultType;
    }

    int getNo() {
        if (resultType != ResultType.LOCAL_VARIABLE) {
            throw new IllegalStateException("Cannot call getNo on a non-var ResultHandle");
        }
        return no;
    }

    void markSingleUse() {
        resultType = ResultType.SINGLE_USE;
    }

    /**
     * Returns the "static" type of this value in the JVM descriptor format.
     * That is, single character like {@code I} for primitive types,
     * {@code Ljava/lang/Object;} for class types, and {@code [I} or
     * {@code [[Ljava/lang/String;} for array types.
     */
    String getType() {
        return type;
    }

    BytecodeCreatorImpl getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "ResultHandle{" +
                "no=" + no +
                ", type='" + type + '\'' +
                ", owner=" + owner +
                '}';
    }

    public Object getConstant() {
        return constant;
    }

    enum ResultType {
        /**
         * A local variable
         */
        LOCAL_VARIABLE,
        /**
         * A constant loaded via LDC, ACONST_NULL, ICONST_* etc.
         */
        CONSTANT,
        /**
         * A result handle that is only used a single time, directly after it is created
         */
        SINGLE_USE,
        /**
         * A result handle that was never used
         */
        UNUSED;
    }
}
