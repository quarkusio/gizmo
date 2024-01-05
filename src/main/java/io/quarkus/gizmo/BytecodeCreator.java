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
import java.util.function.Function;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

/**
 * An interface for creating a methods bytecode.
 * <p>
 * This does not expose the full extent of Java bytecode, rather just the most common operations that generated
 * classes are likely to use.
 */
public interface BytecodeCreator extends AutoCloseable {

    /**
     * @return A {@link ResultHandle} that represents the current object
     */
    ResultHandle getThis();

    /**
     * Invokes a virtual method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    ResultHandle invokeVirtualMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args);

    /**
     * Invokes a virtual method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    default ResultHandle invokeVirtualMethod(MethodInfo descriptor, ResultHandle object, ResultHandle... args) {
        return invokeVirtualMethod(MethodDescriptor.of(descriptor), object, args);
    }

    /**
     * Invokes a interface method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    ResultHandle invokeInterfaceMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args);

    /**
     * Invokes a interface method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    default ResultHandle invokeInterfaceMethod(MethodInfo descriptor, ResultHandle object, ResultHandle... args) {
        return invokeInterfaceMethod(MethodDescriptor.of(descriptor), object, args);
    }

    /**
     * Invokes a static method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     *
     * @param descriptor The method descriptor
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    ResultHandle invokeStaticMethod(MethodDescriptor descriptor, ResultHandle... args);

    /**
     * Invokes a static method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     *
     * @param descriptor The method descriptor
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    default ResultHandle invokeStaticMethod(MethodInfo descriptor, ResultHandle... args) {
        return invokeStaticMethod(MethodDescriptor.of(descriptor), args);
    }

    /**
     * Invokes a static method of an interface, and returns a {@link ResultHandle} with the result, or null if the method is
     * void.
     *
     * @param descriptor The method descriptor
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    ResultHandle invokeStaticInterfaceMethod(MethodDescriptor descriptor, ResultHandle... args);

    /**
     * Invokes a static method of an interface, and returns a {@link ResultHandle} with the result, or null if the method is
     * void.
     *
     * @param descriptor The method descriptor
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    default ResultHandle invokeStaticInterfaceMethod(MethodInfo descriptor, ResultHandle... args) {
        return invokeStaticInterfaceMethod(MethodDescriptor.of(descriptor), args);
    }

    /**
     * Invokes a special method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     * <p>
     * Special methods are constructor invocations, or invocations on a superclass method of the current class.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    ResultHandle invokeSpecialMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args);

    /**
     * Invokes a special method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     * <p>
     * Special methods are constructor invocations, or invocations on a superclass method of the current class.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    default ResultHandle invokeSpecialMethod(MethodInfo descriptor, ResultHandle object, ResultHandle... args) {
        return invokeSpecialMethod(MethodDescriptor.of(descriptor), object, args);
    }

    /**
     * Invokes a special interface method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     * <p>
     * Special interface method invocations are invocations of a superinterface method of the current class.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    ResultHandle invokeSpecialInterfaceMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args);

    /**
     * Invokes a special interface method, and returns a {@link ResultHandle} with the result, or null if the method is void.
     * <p>
     * Special interface method invocations are invocations of a superinterface method of the current class.
     *
     * @param descriptor The method descriptor
     * @param object A {@link ResultHandle} representing the object to invoke on
     * @param args The method parameters
     * @return The method result, or null if a void method
     */
    default ResultHandle invokeSpecialInterfaceMethod(MethodInfo descriptor, ResultHandle object, ResultHandle... args) {
        return invokeSpecialInterfaceMethod(MethodDescriptor.of(descriptor), object, args);
    }

    /**
     * Creates a new instance of a given type, by calling the specified constructor, and returns a {@link ResultHandle}
     * representing the result
     *
     * @param descriptor The constructor descriptor
     * @param args The constructor parameters
     * @return The new instance
     */
    ResultHandle newInstance(MethodDescriptor descriptor, ResultHandle... args);

    /**
     * Creates a new instance of a given type, by calling the specified constructor, and returns a {@link ResultHandle}
     * representing the result
     *
     * @param descriptor The constructor descriptor
     * @param args The constructor parameters
     * @return The new instance
     */
    default ResultHandle newInstance(MethodInfo descriptor, ResultHandle... args) {
        return newInstance(MethodDescriptor.of(descriptor), args);
    }

    ResultHandle newArray(String type, ResultHandle length);

    default ResultHandle newArray(String type, int length) {
        return newArray(type, load(length));
    }

    default ResultHandle newArray(Class<?> type, ResultHandle length) {
        return newArray(type.getName(), length);
    }

    default ResultHandle newArray(Class<?> type, int length) {
        return newArray(type.getName(), load(length));
    }

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(String val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(byte val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(short val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(char val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(int val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(long val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(float val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(double val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    ResultHandle load(boolean val);

    /**
     * Returns a {@link ResultHandle} representing the specified value
     *
     * @param val The value
     * @return A {@link ResultHandle} representing the specified value
     */
    default ResultHandle load(Enum<?> val) {
        return readStaticField(FieldDescriptor.of(val.getDeclaringClass(), val.name(), val.getDeclaringClass()));
    }

    /**
     * Returns a {@link ResultHandle} representing the specified class
     *
     * Note that in almost all circumstances you are better off using {@link #loadClassFromTCCL(String)},
     * as it can load classes that are not visible from the current class. This method should only be used
     * if you are sure that the class being loaded will be accessible from the generated bytecode.
     *
     * @param className The class name
     * @return A {@link ResultHandle} representing the specified class
     */
    ResultHandle loadClass(String className);

    /**
     * Returns a {@link ResultHandle} representing the specified class
     *
     * Note that in almost all circumstances you are better off using {@link #loadClassFromTCCL(Class)},
     * as it can load classes that are not visible from the current class. This method should only be used
     * if you are sure that the class being loaded will be accessible from the generated bytecode.
     *
     * @param val The class to load
     * @return A {@link ResultHandle} representing the specified class
     */
    default ResultHandle loadClass(Class<?> val) {
        return loadClass(val.getName());
    }

    /**
     * Returns a {@link ResultHandle} representing the specified class
     *
     * Note that in almost all circumstances you are better off using {@link #loadClassFromTCCL(ClassInfo)},
     * as it can load classes that are not visible from the current class. This method should only be used
     * if you are sure that the class being loaded will be accessible from the generated bytecode.
     *
     * @param val The class to load
     * @return A {@link ResultHandle} representing the specified class
     */
    default ResultHandle loadClass(ClassInfo val) {
        return loadClass(val.name().toString());
    }

    /**
     * Returns a {@link ResultHandle} representing the specified class
     *
     * This method can load all classes, including package private classes that are not visible to the
     * current bytecode.
     *
     * @param className The class name
     * @return A {@link ResultHandle} representing the specified class
     */
    ResultHandle loadClassFromTCCL(String className);

    /**
     * Returns a {@link ResultHandle} representing the specified class loaded from the TCCL.
     *
     * This method can load all classes, including package private classes that are not visible to the
     * current bytecode.
     *
     * @param val The class to load
     *
     * @return A {@link ResultHandle} representing the specified class
     */
    default ResultHandle loadClassFromTCCL(Class<?> val) {
        return loadClassFromTCCL(val.getName());
    }

    /**
     * Returns a {@link ResultHandle} representing the specified class
     *
     * This method can load all classes, including package private classes that are not visible to the
     * current bytecode.
     *
     * @param val The class to load
     * @return A {@link ResultHandle} representing the specified class
     */
    default ResultHandle loadClassFromTCCL(ClassInfo val) {
        return loadClassFromTCCL(val.name().toString());
    }

    /**
     * Returns a {@link ResultHandle} representing {@code null}}
     *
     * @return A {@link ResultHandle} representing {@code null}}
     */
    ResultHandle loadNull();

    /**
     * Writes the specified value to an instance field
     *
     * @param fieldDescriptor The field to write to
     * @param instance A {@link ResultHandle} representing the instance that contains the field
     * @param value A {@link ResultHandle} representing the value
     */
    void writeInstanceField(FieldDescriptor fieldDescriptor, ResultHandle instance, ResultHandle value);

    /**
     * Writes the specified value to an instance field
     *
     * @param fieldDescriptor The field to write to
     * @param instance A {@link ResultHandle} representing the instance that contains the field
     * @param value A {@link ResultHandle} representing the value
     */
    default void writeInstanceField(FieldInfo fieldDescriptor, ResultHandle instance, ResultHandle value) {
        writeInstanceField(FieldDescriptor.of(fieldDescriptor), instance, value);
    }

    /**
     * Reads an instance field and returns a {@link ResultHandle} representing the result. The result of the read is stored
     * in a local variable, so even if the field value changes the {@link ResultHandle} will represent the same result.
     *
     * @param fieldDescriptor The field to read from
     * @param instance A {@link ResultHandle} representing the instance that contains the field
     * @return A {@link ResultHandle} representing the field value at the current point in time
     */
    ResultHandle readInstanceField(FieldDescriptor fieldDescriptor, ResultHandle instance);

    /**
     * Reads an instance field and returns a {@link ResultHandle} representing the result. The result of the read is stored
     * in a local variable, so even if the field value changes the {@link ResultHandle} will represent the same result.
     *
     * @param fieldDescriptor The field to read from
     * @param instance A {@link ResultHandle} representing the instance that contains the field
     * @return A {@link ResultHandle} representing the field value at the current point in time
     */
    default ResultHandle readInstanceField(FieldInfo fieldDescriptor, ResultHandle instance) {
        return readInstanceField(FieldDescriptor.of(fieldDescriptor), instance);
    }

    /**
     * Writes the specified value to an static field
     *
     * @param fieldDescriptor The field to write to
     * @param value A {@link ResultHandle} representing the value
     */
    void writeStaticField(FieldDescriptor fieldDescriptor, ResultHandle value);

    /**
     * Writes the specified value to an static field
     *
     * @param fieldDescriptor The field to write to
     * @param value A {@link ResultHandle} representing the value
     */
    default void writeStaticField(FieldInfo fieldDescriptor, ResultHandle value) {
        writeStaticField(FieldDescriptor.of(fieldDescriptor), value);
    }

    /**
     * Reads a static field and returns a {@link ResultHandle} representing the result. The result of the read is stored
     * in a local variable, so even if the field value changes the {@link ResultHandle} will represent the same result.
     *
     * @param fieldDescriptor The field to read from
     * @return A {@link ResultHandle} representing the field value at the current point in time
     */
    ResultHandle readStaticField(FieldDescriptor fieldDescriptor);

    /**
     * Reads a static field and returns a {@link ResultHandle} representing the result. The result of the read is stored
     * in a local variable, so even if the field value changes the {@link ResultHandle} will represent the same result.
     *
     * @param fieldDescriptor The field to read from
     * @return A {@link ResultHandle} representing the field value at the current point in time
     */
    default ResultHandle readStaticField(FieldInfo fieldDescriptor) {
        return readStaticField(FieldDescriptor.of(fieldDescriptor));
    }

    ResultHandle arrayLength(ResultHandle array);

    ResultHandle readArrayValue(ResultHandle array, ResultHandle index);

    void writeArrayValue(ResultHandle array, ResultHandle index, ResultHandle value);

    default ResultHandle readArrayValue(ResultHandle array, int index) {
        return readArrayValue(array, load(index));
    }

    default void writeArrayValue(ResultHandle array, int index, ResultHandle value) {
        writeArrayValue(array, load(index), value);
    }

    /**
     * Create a local variable which can be assigned within this scope.
     *
     * @param typeDescr the type descriptor of the variable's type (must not be {@code null})
     * @return the assignable local variable (not {@code null})
     */
    AssignableResultHandle createVariable(final String typeDescr);

    /**
     * Create a local variable which can be assigned within this scope.
     *
     * @param type the type of the variable's type (must not be {@code null})
     * @return the assignable local variable (not {@code null})
     */
    default AssignableResultHandle createVariable(final Class<?> type) {
        Objects.requireNonNull(type);
        return createVariable(DescriptorUtils.classToStringRepresentation(type));
    }

    /**
     * Assign the given value to the given assignable target.
     *
     * @param target the assignment target (must not be {@code null})
     * @param value the value to assign (must not be {@code null})
     */
    void assign(AssignableResultHandle target, ResultHandle value);

    /**
     * Add a {@code try} block.
     *
     * @return the {@code try} block
     */
    TryBlock tryBlock();

    /**
     * Compares two {@code long} values and pushes the resulting integer to the stack. If {@code value1}
     * is greater than {@code value2}, the result is 1; if {@code value1} is equal to {@code value2},
     * the result is 0; if {@code value1} is less than {@code value2}, the result is -1.
     * One of the {@code if*} methods should be used to process the result. Both parameters
     * must be of type {@code long}.
     *
     * @param value1 first {@code long} value to compare
     * @param value2 second {@code long} value to compare
     * @return the comparison result
     */
    ResultHandle compareLong(ResultHandle value1, ResultHandle value2);

    /**
     * Compares two {@code float} values and pushes the resulting integer to the stack. If {@code value1}
     * is greater than {@code value2}, the result is 1; if {@code value1} is equal to {@code value2},
     * the result is 0; if {@code value1} is less than {@code value2}, the result is -1.
     * One of the {@code if*} methods should be used to process the result. Both parameters
     * must be of type {@code float}.
     * <p>
     * If one of the values is NaN, the result is 1 if {@code nanComparesAsGreater} is true and -1 if not.
     *
     * @param value1 first {@code long} value to compare
     * @param value2 second {@code long} value to compare
     * @param nanComparesAsGreater whether presence of NaN should result in "greater"
     * @return the comparison result
     */
    ResultHandle compareFloat(ResultHandle value1, ResultHandle value2, boolean nanComparesAsGreater);

    /**
     * Compares two {@code double} values and pushes the resulting integer to the stack. If {@code value1}
     * is greater than {@code value2}, the result is 1; if {@code value1} is equal to {@code value2},
     * the result is 0; if {@code value1} is less than {@code value2}, the result is -1.
     * One of the {@code if*} methods should be used to process the result. Both parameters
     * must be of type {@code double}.
     * <p>
     * If one of the values is NaN, the result is 1 if {@code nanComparesAsGreater} is true and -1 if not.
     *
     * @param value1 first {@code long} value to compare
     * @param value2 second {@code long} value to compare
     * @param nanComparesAsGreater whether presence of NaN should result in "greater"
     * @return the comparison result
     */
    ResultHandle compareDouble(ResultHandle value1, ResultHandle value2, boolean nanComparesAsGreater);

    /**
     * An if statement.
     * <p>
     * resultHandle must be an integer type or boolean. If this value is true or non-zero the
     * {@link BranchResult#trueBranch} code will be executed, otherwise the {@link BranchResult#falseBranch}
     * will be run.
     *
     * @param resultHandle The result to compare with zero
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifNonZero(ResultHandle resultHandle);

    /**
     * An if statement.
     * <p>
     * resultHandle must be an integer type or boolean. If this value is false or zero the
     * {@link BranchResult#trueBranch} code will be executed, otherwise the {@link BranchResult#falseBranch}
     * will be run.
     *
     * @param resultHandle The result to compare with zero
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifZero(ResultHandle resultHandle);

    /**
     * An if statement. An alias for {@link BytecodeCreator#ifNonZero(ResultHandle)}.
     *
     * @param resultHandle The result to compare with zero
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifTrue(ResultHandle resultHandle);

    /**
     * An if statement. An alias for {@link BytecodeCreator#ifZero(ResultHandle)}.
     *
     * @param resultHandle The result to compare with zero
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifFalse(ResultHandle resultHandle);

    /**
     * An if statement. If the value is {@code null} the {@link BranchResult#trueBranch} code will be executed, otherwise the
     * {@link BranchResult#falseBranch} will be
     * run.
     *
     * @param resultHandle
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifNull(ResultHandle resultHandle);

    /**
     * An if statement. If the value is not {@code null} the {@link BranchResult#trueBranch} code will be executed, otherwise
     * the {@link BranchResult#falseBranch} will be
     * run.
     * 
     * @param resultHandle
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifNotNull(ResultHandle resultHandle);

    /**
     * An if statement.
     * <p>
     * resultHandle must be an integer type. If this value is greater than zero the
     * {@link BranchResult#trueBranch} code will be executed, otherwise the {@link BranchResult#falseBranch}
     * will be run.
     * 
     * @param resultHandle
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifGreaterThanZero(ResultHandle resultHandle);

    /**
     * An if statement.
     * <p>
     * resultHandle must be an integer type. If this value is greater or equals to zero the
     * {@link BranchResult#trueBranch} code will be executed, otherwise the {@link BranchResult#falseBranch}
     * will be run.
     * 
     * @param resultHandle
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifGreaterEqualZero(ResultHandle resultHandle);

    /**
     * An if statement.
     * <p>
     * resultHandle must be an integer type. If this value is lower than zero the
     * {@link BranchResult#trueBranch} code will be executed, otherwise the {@link BranchResult#falseBranch}
     * will be run.
     * 
     * @param resultHandle
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifLessThanZero(ResultHandle resultHandle);

    /**
     * An if statement.
     * <p>
     * resultHandle must be an integer type. If this value is lower or equals to zero the
     * {@link BranchResult#trueBranch} code will be executed, otherwise the {@link BranchResult#falseBranch}
     * will be run.
     * 
     * @param resultHandle
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifLessEqualZero(ResultHandle resultHandle);

    /**
     * An if statement.
     * <p>
     * Values must be integer types. If value1 is equal to value2 the {@link BranchResult#trueBranch} code will be executed,
     * otherwise the {@link BranchResult#falseBranch} will be run.
     * 
     * @param value1
     * @param value2
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifIntegerEqual(ResultHandle value1, ResultHandle value2);

    /**
     * An if statement.
     * <p>
     * Values must be integer types. If value1 is greater than value2 the {@link BranchResult#trueBranch} code will be executed,
     * otherwise the {@link BranchResult#falseBranch} will be run.
     * 
     * @param value1
     * @param value2
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifIntegerGreaterThan(ResultHandle value1, ResultHandle value2);

    /**
     * An if statement.
     * <p>
     * Values must be integer types. If value1 is greater or equal to value2 the {@link BranchResult#trueBranch} code will be
     * executed,
     * otherwise the {@link BranchResult#falseBranch} will be run.
     * 
     * @param value1
     * @param value2
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifIntegerGreaterEqual(ResultHandle value1, ResultHandle value2);

    /**
     * An if statement.
     * <p>
     * Values must be integer types. If value1 is less than value2 the {@link BranchResult#trueBranch} code will be executed,
     * otherwise the {@link BranchResult#falseBranch} will be run.
     * 
     * @param value1
     * @param value2
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifIntegerLessThan(ResultHandle value1, ResultHandle value2);

    /**
     * Create a new if-then-else construct.
     * <p>
     * The {@link IfThenElse#then()} block is executed if the condition result handle evaluates to {@code true}.
     * 
     * @param condition
     * @return the if-then-else construct
     */
    IfThenElse ifThenElse(ResultHandle condition);

    /**
     * Checks if the given resultHandle is an instance of the target type
     *
     * @param resultHandle the result handle
     * @param testType the cast target class
     * @return a boolean result handle with the result of the instanceof call
     */
    default ResultHandle instanceOf(ResultHandle resultHandle, Class<?> testType) {
        return instanceOf(resultHandle, testType.getName());
    }

    /**
     * Checks if the given resultHandle is an instance of the target type
     *
     * @param resultHandle the result handle
     * @param testType the cast target class
     * @return a boolean result handle with the result of the instanceof call
     */
    ResultHandle instanceOf(ResultHandle resultHandle, String testType);

    /**
     * An if statement.
     * <p>
     * Values must be integer types. If value1 is less or equal to value2 the {@link BranchResult#trueBranch} code will be
     * executed,
     * otherwise the {@link BranchResult#falseBranch} will be run.
     * 
     * @param value1
     * @param value2
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifIntegerLessEqual(ResultHandle value1, ResultHandle value2);

    /**
     * An if statement.
     * <p>
     * If references are equal (object identity) the {@link BranchResult#trueBranch} code will be
     * executed, otherwise the {@link BranchResult#falseBranch} will be run.
     * 
     * @param ref1
     * @param ref2
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifReferencesEqual(ResultHandle ref1, ResultHandle ref2);

    /**
     * An if statement.
     * <p>
     * If references are <em>not</em> equal (as in object identity) the {@link BranchResult#trueBranch} code will be
     * executed, otherwise the {@link BranchResult#falseBranch} will be run.
     * <p>
     * This method is dual to {@link #ifReferencesEqual(ResultHandle, ResultHandle)} and can be used
     * to emit bytecode that is closer to what javac emits, when useful.
     *
     * @param ref1
     * @param ref2
     * @return The branch result that is used to build the if statement
     */
    BranchResult ifReferencesNotEqual(ResultHandle ref1, ResultHandle ref2);

    /**
     * @param i The method parameter number, zero indexed
     * @return A {@link ResultHandle} representing the parameter
     */
    ResultHandle getMethodParam(int i);

    /**
     * Creates an instance of a functional interface
     * <p>
     * The resulting {@link FunctionCreator} can be used to both define the functions
     * bytecode, and to get a {@link ResultHandle} that represents the instance of the function.
     *
     * @param functionalInterface A functional interface
     * @return The function builder
     */
    FunctionCreator createFunction(Class<?> functionalInterface);

    /**
     * Represents a return statement. If this is a void method the return value must be null, otherwise it must be a
     * {@link ResultHandle} of the correct type which will be returned from the method.
     *
     * @param returnValue The value to return
     */
    void returnValue(ResultHandle returnValue);

    /**
     * Marks a return from a {@code void} method or a constructor.
     * <p>
     * Equivalent to {@code returnValue(null)}.
     *
     * @see #returnValue(ResultHandle)
     */
    default void returnVoid() {
        returnValue(null);
    }

    /**
     * Creates a return statement that returns the {@code null} reference.
     * <p>
     * Equivalent to {@code returnValue(loadNull())}.
     *
     * @see #returnValue(ResultHandle)
     */
    default void returnNull() {
        returnValue(loadNull());
    }

    /**
     * Creates a return statement that returns given boolean constant.
     * <p>
     * Equivalent to {@code returnValue(load(value))}.
     *
     * @param value the boolean constant
     * @see #returnValue(ResultHandle)
     */
    default void returnBoolean(boolean value) {
        returnValue(load(value));
    }

    /**
     * Creates a return statement that returns given integer constant.
     * <p>
     * Equivalent to {@code returnValue(load(value))}.
     *
     * @param value the integer constant
     * @see #returnValue(ResultHandle)
     */
    default void returnInt(int value) {
        returnValue(load(value));
    }

    /**
     * Throws an exception
     *
     * @param exception A result handle representing the exception to throw
     */
    void throwException(ResultHandle exception);

    /**
     * Perform a check cast operation which transforms the type of the given result handle.
     *
     * @param resultHandle the result handle
     * @param castTarget the cast target type descriptor
     * @return a new result handle with updated type
     */
    ResultHandle checkCast(ResultHandle resultHandle, String castTarget);

    /**
     * Perform a check cast operation which transforms the type of the given result handle.
     *
     * @param resultHandle the result handle
     * @param castTarget the cast target class
     * @return a new result handle with updated type
     */
    default ResultHandle checkCast(ResultHandle resultHandle, Class<?> castTarget) {
        return checkCast(resultHandle, castTarget.getName());
    }

    /**
     * Throws an exception. The exception must have a constructor that takes a single String argument
     *
     * @param exceptionType The exception type
     * @param message The exception message
     */
    default <T extends Throwable> void throwException(Class<T> exceptionType, String message) {
        try {
            exceptionType.getDeclaredConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Exception does not have appropriate constructor");
        }
        ResultHandle res = newInstance(MethodDescriptor.ofConstructor(exceptionType, String.class), load(message));
        throwException(res);
    }

    /**
     * Rethrows an exception. The exception must have a constructor that takes (String, Throwable)
     *
     * @param exceptionType The exception type
     * @param message The exception message
     * @param existing The exception to wrap
     */
    default <T extends Throwable> void throwException(Class<T> exceptionType, String message, ResultHandle existing) {
        try {
            exceptionType.getDeclaredConstructor(String.class, Throwable.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Exception does not have appropriate constructor");
        }
        ResultHandle res = newInstance(MethodDescriptor.ofConstructor(exceptionType, String.class, Throwable.class),
                load(message), existing);
        throwException(res);
    }

    default ResultHandle marshalAsArray(Class<?> arrayClass, ResultHandle... params) {
        ResultHandle array = newArray(arrayClass, load(params.length));
        for (int i = 0; i < params.length; ++i) {
            writeArrayValue(array, load(i), params[i]);
        }
        return array;
    }

    /**
     * Determine if this bytecode creator is scoped within the given bytecode creator.
     *
     * @param other the other bytecode creator
     * @return {@code true} if this bytecode creator is scoped within the given creator, {@code false} otherwise
     */
    boolean isScopedWithin(BytecodeCreator other);

    /**
     * Go to the top of the given scope.
     *
     * @param scope the scope to continue
     */
    void continueScope(BytecodeCreator scope);

    /**
     * Go to the top of this scope.
     */
    default void continueScope() {
        continueScope(this);
    }

    /**
     * Go to the end of the given scope.
     *
     * @param scope the scope to break out of
     */
    void breakScope(BytecodeCreator scope);

    /**
     * Go to the end of this scope.
     */
    default void breakScope() {
        breakScope(this);
    }

    /**
     * Create a nested scope. Bytecode added to the nested scope will be inserted at this point of the
     * enclosing scope.
     *
     * @return the nested scope
     */
    BytecodeCreator createScope();

    /**
     * Create a new while loop statement.
     * 
     * @param conditionFun A function used to create the condition. The true branch continues executing the block and the false
     *        branch terminates the statement.
     * @return the while loop statement
     */
    WhileLoop whileLoop(Function<BytecodeCreator, BranchResult> conditionFun);

    /**
     * Create a new for-each loop construct.
     * 
     * @param iterable
     * @return the for-each construct
     */
    ForEachLoop forEach(ResultHandle iterable);

    /**
     * Adds the two result handles together and returns the result
     * 
     * @param a1 The first number
     * @param a2 The second number
     * @return The result
     */
    ResultHandle add(ResultHandle a1, ResultHandle a2);

    /**
     * Multiplies the two result handles together and returns the result
     *
     * @param a1 The first number
     * @param a2 The second number
     * @return The result
     */
    ResultHandle multiply(ResultHandle a1, ResultHandle a2);

    /**
     * Divides the first result handles by the second and returns the result
     *
     * @param a1 The first number
     * @param a2 The second number
     * @return The result
     */
    ResultHandle divide(ResultHandle a1, ResultHandle a2);

    /**
     * Computes the bitwise AND of the two result handles and returns the result
     *
     * @param a1 The first number
     * @param a2 The second number
     * @return The result
     */
    ResultHandle bitwiseAnd(ResultHandle a1, ResultHandle a2);

    /**
     * Computes the bitwise OR of the two result handles and returns the result
     *
     * @param a1 The first number
     * @param a2 The second number
     * @return The result
     */
    ResultHandle bitwiseOr(ResultHandle a1, ResultHandle a2);

    /**
     * Computes the bitwise XOR of the two result handles and returns the result
     *
     * @param a1 The first number
     * @param a2 The second number
     * @return The result
     */
    ResultHandle bitwiseXor(ResultHandle a1, ResultHandle a2);

    /**
     * Increments a ResultHandle
     * 
     * @param toIncrement The number to increment
     * @return The result
     */
    default ResultHandle increment(ResultHandle toIncrement) {
        return add(toIncrement, load(1));
    }

    /**
     * Create a new switch construct for a string value.
     * 
     * @param value The string value to switch on
     * @return the switch construct
     */
    Switch.StringSwitch stringSwitch(ResultHandle value);

    /**
     * Create a new switch construct for an enum constant.
     * 
     * @param <E>
     * @param value The enum constant to switch on
     * @param enumClass
     * @return the switch construct
     */
    <E extends Enum<E>> Switch.EnumSwitch<E> enumSwitch(ResultHandle value, Class<E> enumClass);

    /**
     * Indicate that the scope is no longer in use. The scope may refuse additional instructions after this method
     * is called.
     */
    @Override
    default void close() {
    }

}
