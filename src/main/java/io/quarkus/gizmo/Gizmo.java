package io.quarkus.gizmo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.objectweb.asm.Opcodes;

public final class Gizmo {

    public static final int ASM_API_VERSION = Opcodes.ASM9;

    private Gizmo() {
    }

    //================================================================================
    // utils used to simplify invocation of static/non-static methods of JDK classes
    // these constructs are not thread-safe and should not be used concurrently
    //================================================================================

    /**
     * Generates the bytecode that calls {@link Object#toString()}.
     * 
     * @param target
     * @param obj
     * @return the result
     */
    public static ResultHandle toString(BytecodeCreator target, ResultHandle obj) {
        return target.invokeVirtualMethod(TO_STRING, obj);
    }

    /**
     * Generates the bytecode that calls {@link Object#equals(Object)}.
     * 
     * @param target
     * @param obj
     * 
     * @return the result
     */
    public static ResultHandle equals(BytecodeCreator target, ResultHandle obj1, ResultHandle obj2) {
        return target.invokeVirtualMethod(EQUALS, obj1, obj2);
    }

    /**
     * Generates the bytecode that calls {@code System.out.println(obj)}.
     * 
     * @param target
     * @param obj
     */
    public static void systemOutPrintln(BytecodeCreator target, ResultHandle obj) {
        target.invokeVirtualMethod(PRINTLN, target.readStaticField(SYSTEM_OUT), obj);
    }

    /**
     * Generates the bytecode that calls {@code System.err.println(obj)}.
     * 
     * @param target
     * @param obj
     */
    public static void systemErrPrintln(BytecodeCreator target, ResultHandle obj) {
        target.invokeVirtualMethod(PRINTLN, target.readStaticField(SYSTEM_ERR), obj);
    }

    /**
     * <pre>
     * ResultHandle firstElement = Gizmo.invokeList(methodBytecode).instance(myList).get(1);
     * </pre>
     * 
     * @param target
     * @return the generator
     */
    public static JdkList listOperations(BytecodeCreator target) {
        return new JdkList(target);
    }

    /**
     * <pre>
     * ResultHandle size = Gizmo.invokeCollection(methodBytecode).instance(myList).size();
     * </pre>
     * 
     * @param target
     * @return the generator
     */
    public static JdkCollection collectionOperations(BytecodeCreator target) {
        return new JdkCollection(target);
    }

    /**
     * <pre>
     * ResultHandle set = Gizmo.invokeSet(methodBytecode).of(element);
     * </pre>
     * 
     * @param target
     * @return the generator
     */
    public static JdkSet setOperations(BytecodeCreator target) {
        return new JdkSet(target);
    }

    /**
     * <pre>
     * ResultHandle optionalFoo = Gizmo.invokeOptional(methodBytecode).ofNullable(foo);
     * </pre>
     * 
     * @param target
     * @return the generator
     */
    public static JdkOptional optionalOperations(BytecodeCreator target) {
        return new JdkOptional(target);
    }

    /**
     * <pre>
     * ResultHandle iterator = Gizmo.invokeIterable(methodBytecode).instance(myCollection).iterator();
     * </pre>
     * 
     * @param target
     * @return the generator
     */
    public static JdkIterable iterableOperations(BytecodeCreator target) {
        return new JdkIterable(target);
    }

    /**
     * <pre>
     * ResultHandle hasNext = Gizmo.invokeIterator(methodBytecode).instance(myIterator).hasNext();
     * </pre>
     * 
     * @param target
     * @return the generator
     */
    public static JdkIterator iteratorOperations(BytecodeCreator target) {
        return new JdkIterator(target);
    }

    /**
     * <pre>
     * ResultHandle mapping = Gizmo.invokeMap(methodBytecode).instance(myMap).get(myKey);
     * </pre>
     * 
     * @param target
     * @return the generator
     */
    public static JdkMap mapOperations(BytecodeCreator target) {
        return new JdkMap(target);
    }

    /**
     * Creates a {@code StringBuilder} generator that helps to generate a chain of
     * {@code append} calls and a final {@code toString} call.
     *
     * <pre>
     * StringBuilderGenerator str = Gizmo.newStringBuilder(bytecode);
     * str.append("constant");
     * str.append(someResultHandle);
     * ResultHandle result = str.callToString();
     * </pre>
     *
     * The {@code append} method mimics the regular {@code StringBuilder.append}, so
     * it accepts {@code ResultHandle}s of all types for which {@code StringBuilder}
     * has an overload:
     * <ul>
     *     <li>primitive types</li>
     *     <li>{@code char[]}</li>
     *     <li>{@code java.lang.String}</li>
     *     <li>{@code java.lang.Object}</li>
     * </ul>
     *
     * Notably, arrays except of {@code char[]} are appended using {@code Object.toString}
     * and if {@code Arrays.toString} should be used, it must be generated manually.
     * <p>
     * Methods for appending only a part of {@code char[]} or {@code CharSequence} are not
     * provided. Other {@code StringBuilder} methods are not provided either. This is just
     * a simple utility for generating code that concatenates strings, e.g. for implementing
     * the {@code toString} method.
     *
     * @param target
     * @return the generator
     */
    public static StringBuilderGenerator newStringBuilder(BytecodeCreator target) {
        return new StringBuilderGenerator(target);
    }

    /**
     * Generates the bytecode that calls the no-args {@link HashMap} constructor.
     * 
     * @param target
     * @return the result handle
     */
    public static ResultHandle newHashMap(BytecodeCreator target) {
        return target.newInstance(MethodDescriptor.ofConstructor(HashMap.class));
    }

    /**
     * Generates the bytecode that calls the no-args {@link HashSet} constructor.
     * 
     * @param target
     * @return the result handle
     */

    public static ResultHandle newHashSet(BytecodeCreator target) {
        return target.newInstance(MethodDescriptor.ofConstructor(HashSet.class));
    }

    /**
     * Generates the bytecode that calls the no-args {@link ArrayList} constructor.
     * 
     * @param target
     * @return the result handle
     */

    public static ResultHandle newArrayList(BytecodeCreator target) {
        return target.newInstance(MethodDescriptor.ofConstructor(ArrayList.class));
    }

    /**
     * Generates the bytecode that calls the {@link ArrayList} constructor with the specified initial capacity.
     * 
     * @param target
     * @return the result handle
     */
    public static ResultHandle newArrayList(BytecodeCreator target, int initialCapacity) {
        return target.newInstance(MethodDescriptor.ofConstructor(ArrayList.class, int.class), target.load(initialCapacity));
    }

    // Implementation classes

    /**
     * An abstract base for all generators.
     * <p>
     * This construct is not thread-safe and should not be used concurrently.
     */
    public abstract static class StaticInvocationGenerator {

        protected BytecodeCreator target;

        StaticInvocationGenerator(BytecodeCreator target) {
            setTarget(target);
        }

        void setTarget(BytecodeCreator target) {
            this.target = Objects.requireNonNull(target);
        }

    }

    /**
     * An abstract base for all instance invokers.
     */
    public abstract static class InstanceInvocationGenerator {

        protected final ResultHandle instance;

        InstanceInvocationGenerator(ResultHandle instance) {
            this.instance = Objects.requireNonNull(instance);
        }

    }

    /**
     * Helper class to build a bytecode generator for repetitive tasks.
     * 
     * <pre>
     * CustomInvocationGenerator alwaysReturnTrue = new CustomInvocationGenerator(methodBytecode, (bc, args) -> bc.load(true));
     * ResultHandle true1 = alwaysReturnTrue.invoke();
     * ResultHandle true2 = alwaysReturnTrue.invoke();
     * </pre>
     * 
     */
    public static class CustomInvocationGenerator extends StaticInvocationGenerator {

        private final BiFunction<BytecodeCreator, ResultHandle[], ResultHandle> fun;

        public CustomInvocationGenerator(BytecodeCreator target,
                BiFunction<BytecodeCreator, ResultHandle[], ResultHandle> fun) {
            super(target);
            this.fun = fun;
        }

        public ResultHandle invoke(ResultHandle... args) {
            return fun.apply(target, args);
        }

    }

    /**
     * Bytecode generator for static methods.
     * 
     * @see Optional
     * @see #on(ResultHandle)
     */
    public static class JdkOptional extends StaticInvocationGenerator {

        public static final MethodDescriptor OF = MethodDescriptor.ofMethod(Optional.class, "of", Optional.class, Object.class);
        public static final MethodDescriptor OF_NULLABLE = MethodDescriptor.ofMethod(Optional.class, "ofNullable",
                Optional.class,
                Object.class);
        public static final MethodDescriptor GET = MethodDescriptor.ofMethod(Optional.class, "get", Object.class);
        public static final MethodDescriptor IS_PRESENT = MethodDescriptor.ofMethod(Optional.class, "isPresent", boolean.class);
        public static final MethodDescriptor IS_EMPTY = MethodDescriptor.ofMethod(Optional.class, "isEmpty", boolean.class);

        public JdkOptional(BytecodeCreator target) {
            super(target);
        }

        /**
         * 
         * @return bytecode generator for instance methods
         */
        public JdkOptionalInstance on(ResultHandle list) {
            return new JdkOptionalInstance(list);
        }

        /**
         * 
         * @param value
         * @return the result handle
         * @see Optional#of(Object)
         */
        public ResultHandle of(ResultHandle value) {
            return target.invokeStaticMethod(OF, value);
        }

        /**
         * 
         * @param value
         * @return the result handle
         * @see Optional#ofNullable(Object)
         */
        public ResultHandle ofNullable(ResultHandle value) {
            return target.invokeStaticMethod(OF_NULLABLE, value);
        }

        public class JdkOptionalInstance extends InstanceInvocationGenerator {

            JdkOptionalInstance(ResultHandle instance) {
                super(instance);
            }

            /**
             * 
             * @return the result handle
             * @see Optional#isPresent()
             */
            public ResultHandle isPresent() {
                return target.invokeVirtualMethod(IS_PRESENT,
                        instance);
            }

            /**
             * 
             * @return the result handle
             * @see Optional#isEmpty()
             */
            public ResultHandle isEmpty() {
                return target.invokeVirtualMethod(IS_EMPTY,
                        instance);
            }

            /**
             * 
             * @return the result handle
             * @see Optional#get()
             */
            public ResultHandle get() {
                return target.invokeVirtualMethod(GET,
                        instance);
            }

        }

    }

    /**
     * Bytecode generator for static methods.
     * 
     * @see Iterable
     * @see #on(ResultHandle)
     */
    public static class JdkIterable extends StaticInvocationGenerator {

        public static final MethodDescriptor ITERATOR = MethodDescriptor.ofMethod(Iterable.class, "iterator", Iterator.class);

        public JdkIterable(BytecodeCreator target) {
            super(target);
        }

        /**
         * 
         * @return bytecode generator for instance methods
         */
        public JdkIterableInstance on(ResultHandle iterable) {
            return new JdkIterableInstance(iterable);
        }

        public class JdkIterableInstance extends InstanceInvocationGenerator {

            JdkIterableInstance(ResultHandle instance) {
                super(instance);
            }

            /**
             * 
             * @return the result handle
             * @see Iterable#iterator()
             */
            public ResultHandle iterator() {
                return target.invokeInterfaceMethod(ITERATOR, instance);
            }
        }

    }

    /**
     * Bytecode generator for static methods.
     * 
     * @see Iterator
     * @see #on(ResultHandle)
     */
    public static class JdkIterator extends StaticInvocationGenerator {

        public static final MethodDescriptor NEXT = MethodDescriptor.ofMethod(Iterator.class, "next", Object.class);
        public static final MethodDescriptor HAS_NEXT = MethodDescriptor.ofMethod(Iterator.class, "hasNext", boolean.class);

        public JdkIterator(BytecodeCreator target) {
            super(target);
        }

        /**
         * 
         * @return bytecode generator for instance methods
         */
        public JdkIteratorInstance on(ResultHandle iterable) {
            return new JdkIteratorInstance(iterable);
        }

        public class JdkIteratorInstance extends InstanceInvocationGenerator {

            JdkIteratorInstance(ResultHandle instance) {
                super(instance);
            }

            /**
             * 
             * @return the result handle
             * @see Iterator#next()
             */
            public ResultHandle next() {
                return target.invokeInterfaceMethod(NEXT, instance);
            }

            /**
             * 
             * @return the result handle
             * @see Iterator#hasNext()
             */
            public ResultHandle hasNext() {
                return target.invokeInterfaceMethod(HAS_NEXT, instance);
            }

        }

    }

    /**
     * Bytecode generator for static methods.
     * 
     * @see Collection
     * @see #on(ResultHandle)
     */
    public static class JdkCollection extends JdkIterable {

        public static final MethodDescriptor SIZE = MethodDescriptor.ofMethod(Collection.class, "size", int.class);
        public static final MethodDescriptor IS_EMPTY = MethodDescriptor.ofMethod(Collection.class, "isEmpty", boolean.class);
        public static final MethodDescriptor CONTAINS = MethodDescriptor.ofMethod(Collection.class, "contains", boolean.class,
                Object.class);
        public static final MethodDescriptor ADD = MethodDescriptor.ofMethod(Collection.class, "add", boolean.class,
                Object.class);
        public static final MethodDescriptor ADD_ALL = MethodDescriptor.ofMethod(Collection.class, "addAll", boolean.class,
                Collection.class);
        public static final MethodDescriptor CLEAR = MethodDescriptor.ofMethod(Collection.class, "clear", void.class);

        public JdkCollection(BytecodeCreator target) {
            super(target);
        }

        /**
         * 
         * @return bytecode generator for instance methods
         */
        public JdkCollectionInstance on(ResultHandle list) {
            return new JdkCollectionInstance(list);
        }

        public class JdkCollectionInstance extends JdkIterableInstance {

            JdkCollectionInstance(ResultHandle instance) {
                super(instance);
            }

            /**
             * 
             * @return the result handle
             * @see Collection#size()
             */
            public ResultHandle size() {
                return target.invokeInterfaceMethod(SIZE, instance);
            }

            /**
             * 
             * @return the result handle
             * @see Collection#isEmpty()
             */
            public ResultHandle isEmpty() {
                return target.invokeInterfaceMethod(IS_EMPTY,
                        instance);
            }

            /**
             * 
             * @return the result handle
             * @see Collection#contains(Object)
             */
            public ResultHandle contains(ResultHandle obj) {
                return target.invokeInterfaceMethod(CONTAINS,
                        instance, obj);
            }

            /**
             * 
             * @return the result handle
             * @see Collection#add(Object)
             */
            public ResultHandle add(ResultHandle element) {
                return target.invokeInterfaceMethod(ADD,
                        instance, element);
            }

            /**
             * 
             * @return the result handle
             * @see Collection#addAll(Collection)
             */
            public ResultHandle addAll(ResultHandle collection) {
                return target.invokeInterfaceMethod(ADD_ALL,
                        instance, collection);
            }

            /**
             * 
             * @see Collection#clear()
             */
            public void clear() {
                target.invokeInterfaceMethod(CLEAR, instance);
            }

        }

    }

    /**
     * Bytecode generator for static methods.
     * 
     * @see List
     * @see #on(ResultHandle)
     */
    public static class JdkList extends JdkCollection {

        public static final MethodDescriptor GET = MethodDescriptor.ofMethod(List.class, "get", Object.class, int.class);
        public static final MethodDescriptor OF1 = MethodDescriptor.ofMethod(List.class, "of", List.class, Object.class);
        public static final MethodDescriptor OF2 = MethodDescriptor.ofMethod(List.class, "of", List.class, Object.class,
                Object.class);
        public static final MethodDescriptor OF3 = MethodDescriptor.ofMethod(List.class, "of", List.class, Object.class,
                Object.class, Object.class);

        public JdkList(BytecodeCreator target) {
            super(target);
        }

        /**
         * 
         * @return bytecode generator for instance methods
         */
        public JdkListInstance on(ResultHandle list) {
            return new JdkListInstance(list);
        }

        /**
         * 
         * @param e1
         * @return the result handle
         * @see List#of(Object)
         */
        public ResultHandle of(ResultHandle e1) {
            return target.invokeStaticInterfaceMethod(OF1, e1);
        }

        /**
         * 
         * @param e1
         * @param e2
         * @return the result handle
         * @see List#of(Object, Object)
         */
        public ResultHandle of(ResultHandle e1, ResultHandle e2) {
            return target.invokeStaticInterfaceMethod(OF2, e1, e2);
        }

        /**
         * 
         * @param e1
         * @param e2
         * @param e3
         * @return the result handle
         * @see List#of(Object, Object, Object)
         */
        public ResultHandle of(ResultHandle e1, ResultHandle e2, ResultHandle e3) {
            return target.invokeStaticInterfaceMethod(OF3, e1, e2, e3);
        }

        public class JdkListInstance extends JdkCollectionInstance {

            JdkListInstance(ResultHandle instance) {
                super(instance);
            }

            /**
             * 
             * @param index
             * @return the result handle
             * @see List#get(int)
             */
            public ResultHandle get(int index) {
                return get(target.load(index));
            }

            /**
             * 
             * @param index
             * @return the result handle
             * @see List#get(int)
             */
            public ResultHandle get(ResultHandle index) {
                return target.invokeInterfaceMethod(GET, instance,
                        index);
            }

        }

    }

    /**
     * 
     * @see Set
     */
    public static class JdkSet extends JdkCollection {

        public static final MethodDescriptor OF1 = MethodDescriptor.ofMethod(Set.class, "of", Set.class, Object.class);
        public static final MethodDescriptor OF2 = MethodDescriptor.ofMethod(Set.class, "of", Set.class, Object.class,
                Object.class);
        public static final MethodDescriptor OF3 = MethodDescriptor.ofMethod(Set.class, "of", Set.class, Object.class,
                Object.class, Object.class);

        public JdkSet(BytecodeCreator target) {
            super(target);
        }

        public JdkSetInstance on(ResultHandle list) {
            return new JdkSetInstance(list);
        }

        public ResultHandle of(ResultHandle e1) {
            return target.invokeStaticInterfaceMethod(OF1, e1);
        }

        public ResultHandle of(ResultHandle e1, ResultHandle e2) {
            return target.invokeStaticInterfaceMethod(OF2, e1, e2);
        }

        public ResultHandle of(ResultHandle e1, ResultHandle e2, ResultHandle e3) {
            return target.invokeStaticInterfaceMethod(OF3, e1, e2, e3);
        }

        public class JdkSetInstance extends JdkCollectionInstance {

            JdkSetInstance(ResultHandle instance) {
                super(instance);
            }

        }

    }

    /**
     * 
     * @see Map
     */
    public static class JdkMap extends StaticInvocationGenerator {

        public static final MethodDescriptor GET = MethodDescriptor.ofMethod(Map.class, "get", Object.class, Object.class);
        public static final MethodDescriptor PUT = MethodDescriptor.ofMethod(Map.class, "put", Object.class, Object.class,
                Object.class);
        public static final MethodDescriptor OF1 = MethodDescriptor.ofMethod(Map.class, "of", Map.class, Object.class,
                Object.class);
        public static final MethodDescriptor SIZE = MethodDescriptor.ofMethod(Map.class, "size", int.class);
        public static final MethodDescriptor IS_EMPTY = MethodDescriptor.ofMethod(Map.class, "isEmpty", boolean.class);
        public static final MethodDescriptor CONTAINS_KEY = MethodDescriptor.ofMethod(Map.class, "containsKey", boolean.class,
                Object.class);

        public JdkMap(BytecodeCreator target) {
            super(target);
        }

        public JdkMapInstance instance(ResultHandle list) {
            return new JdkMapInstance(list);
        }

        public ResultHandle of(ResultHandle k1, ResultHandle v1) {
            return target.invokeStaticInterfaceMethod(OF1, k1, v1);
        }

        public class JdkMapInstance extends InstanceInvocationGenerator {

            JdkMapInstance(ResultHandle instance) {
                super(instance);
            }

            public ResultHandle get(ResultHandle key) {
                return target.invokeInterfaceMethod(GET, instance,
                        key);
            }

            public ResultHandle put(ResultHandle key, ResultHandle val) {
                return target.invokeInterfaceMethod(PUT, instance,
                        key, val);
            }

            public ResultHandle size() {
                return target.invokeInterfaceMethod(SIZE, instance);
            }

            public ResultHandle isEmpty() {
                return target.invokeInterfaceMethod(IS_EMPTY,
                        instance);
            }

            public ResultHandle containsKey(ResultHandle key) {
                return target.invokeInterfaceMethod(CONTAINS_KEY,
                        instance, key);
            }

        }

    }

    public static class StringBuilderGenerator {
        private static final MethodDescriptor CONSTRUCTOR = MethodDescriptor.ofConstructor(StringBuilder.class);
        private static final MethodDescriptor APPEND_BOOLEAN = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, boolean.class);
        private static final MethodDescriptor APPEND_INT = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, int.class);
        private static final MethodDescriptor APPEND_LONG = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, long.class);
        private static final MethodDescriptor APPEND_FLOAT = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, float.class);
        private static final MethodDescriptor APPEND_DOUBLE = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, double.class);
        private static final MethodDescriptor APPEND_CHAR = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, char.class);
        private static final MethodDescriptor APPEND_CHAR_ARRAY = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, char[].class);
        private static final MethodDescriptor APPEND_STRING = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, String.class);
        private static final MethodDescriptor APPEND_CHAR_SEQUENCE = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, CharSequence.class);
        private static final MethodDescriptor APPEND_OBJECT = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, Object.class);
        private static final MethodDescriptor TO_STRING = MethodDescriptor.ofMethod(StringBuilder.class, "toString", String.class);

        private final BytecodeCreator bytecode;
        private final ResultHandle instance;

        private StringBuilderGenerator(BytecodeCreator bytecode) {
            this.bytecode = bytecode;
            this.instance = bytecode.newInstance(CONSTRUCTOR);
        }

        public StringBuilderGenerator append(ResultHandle value) {
            switch (value.getType()) {
                case "Z": // boolean
                    bytecode.invokeVirtualMethod(APPEND_BOOLEAN, instance, value);
                    break;
                case "B": // byte
                case "S": // short
                case "I": // int
                    bytecode.invokeVirtualMethod(APPEND_INT, instance, value);
                    break;
                case "J": // long
                    bytecode.invokeVirtualMethod(APPEND_LONG, instance, value);
                    break;
                case "F": // float
                    bytecode.invokeVirtualMethod(APPEND_FLOAT, instance, value);
                    break;
                case "D": // double
                    bytecode.invokeVirtualMethod(APPEND_DOUBLE, instance, value);
                    break;
                case "C": // char
                    bytecode.invokeVirtualMethod(APPEND_CHAR, instance, value);
                    break;
                case "[C": // char[]
                    bytecode.invokeVirtualMethod(APPEND_CHAR_ARRAY, instance, value);
                    break;
                case "Ljava/lang/String;":
                    bytecode.invokeVirtualMethod(APPEND_STRING, instance, value);
                    break;
                case "Ljava/lang/CharSequence;":
                    bytecode.invokeVirtualMethod(APPEND_CHAR_SEQUENCE, instance, value);
                    break;
                default:
                    bytecode.invokeVirtualMethod(APPEND_OBJECT, instance, value);
                    break;
            }
            return this;
        }

        public StringBuilderGenerator append(char constant) {
            return append(bytecode.load(constant));
        }

        public StringBuilderGenerator append(String constant) {
            return append(bytecode.load(constant));
        }

        public ResultHandle callToString() {
            return bytecode.invokeVirtualMethod(TO_STRING, instance);
        }

        public ResultHandle getInstance() {
            return instance;
        }
    }

    public static final MethodDescriptor TO_STRING = MethodDescriptor.ofMethod(Object.class, "toString", String.class);
    public static final MethodDescriptor EQUALS = MethodDescriptor.ofMethod(Object.class, "equals", boolean.class,
            Object.class);
    public static final MethodDescriptor PRINTLN = MethodDescriptor.ofMethod(PrintStream.class, "println", void.class,
            String.class);
    public static final FieldDescriptor SYSTEM_OUT = FieldDescriptor.of(System.class, "out", PrintStream.class);
    public static final FieldDescriptor SYSTEM_ERR = FieldDescriptor.of(System.class, "err", PrintStream.class);

}
