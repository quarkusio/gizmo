package io.quarkus.gizmo2.desc;

import static java.lang.constant.ConstantDescs.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * A holder class for commonly-used JDK descriptors.
 * <h3>Naming conventions</h3>
 * Descriptors are named as follows, by convention:
 * <ul>
 * <li>{@code CD_<className>} - Class descriptor (type {@link ClassDesc})</li>
 * <li>{@code MD_<className>.<methodName>[_[<argType>][<argCount>]]} - Method descriptor (type {@link MethodDesc}, or one of its
 * two subtypes)</li>
 * <li>{@code FD_<className>.<fieldName>} - Field descriptor (type {@link FieldDesc})</li>
 * </ul>
 * Member descriptors (i.e. field, method, and nested class descriptors) are further organized
 * here by their kind and owner class.
 * For example, to find the method descriptor for {@link Class#getName()},
 * you would use {@link MD_Class#getName Descs.MD_Class.getName}.
 * To find the field descriptor for {@link System#out},
 * you would use {@link FD_System#out FD_System.out}.
 * <p>
 * For methods which have many overloads, there is usually a static method
 * which accepts the argument type(s) and returns the correct descriptor.
 * For example, to find the descriptor for {@link String#valueOf(float)},
 * you can call
 * <code>{@link MD_String#valueOf(ClassDesc) Descs.MD_String.valueOf}({@link ConstantDescs#CD_float CD_float})</code>.
 * <p>
 * <h3>Relationship to {@link ConstantDescs}</h3>
 * The JDK-provided {@code ConstantDescs} class follows the same naming convention for descriptor naming.
 * If a constant is available in that class (as of the earliest JDK supported by this project),
 * it is generally not redundantly provided here.
 * <p>
 * For example, this class provides {@link Descs#CD_Collection}, however it does not
 * provide {@link ConstantDescs#CD_List}.
 */
public final class Descs {

    private Descs() {
    }

    //=====================================================
    // Class descriptors
    //=====================================================

    //-----------------------------------------------------
    // Common array types
    //-----------------------------------------------------

    public static final ClassDesc CD_boolean_array = CD_boolean.arrayType();
    public static final ClassDesc CD_byte_array = CD_byte.arrayType();
    public static final ClassDesc CD_char_array = CD_char.arrayType();
    public static final ClassDesc CD_short_array = CD_short.arrayType();
    public static final ClassDesc CD_int_array = CD_int.arrayType();
    public static final ClassDesc CD_long_array = CD_long.arrayType();
    public static final ClassDesc CD_float_array = CD_float.arrayType();
    public static final ClassDesc CD_double_array = CD_double.arrayType();

    public static final ClassDesc CD_Object_array = CD_Object.arrayType();

    //-----------------------------------------------------
    // java.lang
    //-----------------------------------------------------

    public static final ClassDesc CD_AutoCloseable = Util.classDesc(AutoCloseable.class);
    public static final ClassDesc CD_CharSequence = Util.classDesc(CharSequence.class);
    public static final ClassDesc CD_ClassLoader = Util.classDesc(ClassLoader.class);
    public static final ClassDesc CD_Comparable = Util.classDesc(Comparable.class);
    public static final ClassDesc CD_Iterable = Util.classDesc(Iterable.class);
    public static final ClassDesc CD_StringBuilder = Util.classDesc(StringBuilder.class);
    public static final ClassDesc CD_System = Util.classDesc(System.class);
    public static final ClassDesc CD_Thread = Util.classDesc(Thread.class);
    public static final ClassDesc CD_Throwable_array = CD_Throwable.arrayType();

    // throwable types

    public static final ClassDesc CD_RuntimeException = Util.classDesc(RuntimeException.class);

    //-----------------------------------------------------
    // java.lang.annotation
    //-----------------------------------------------------

    public static final ClassDesc CD_Annotation = Util.classDesc(Annotation.class);
    public static final ClassDesc CD_Inherited = Util.classDesc(Inherited.class);
    public static final ClassDesc CD_Repeatable = Util.classDesc(Repeatable.class);
    public static final ClassDesc CD_Retention = Util.classDesc(Retention.class);
    public static final ClassDesc CD_Target = Util.classDesc(Target.class);
    public static final ClassDesc CD_ElementType = Util.classDesc(ElementType.class);
    public static final ClassDesc CD_RetentionPolicy = Util.classDesc(RetentionPolicy.class);

    //-----------------------------------------------------
    // java.lang.invoke
    //-----------------------------------------------------

    public static final ClassDesc CD_ConstantCallSite = Util.classDesc(ConstantCallSite.class);
    public static final ClassDesc CD_LambdaMetafactory = Util.classDesc(LambdaMetafactory.class);
    public static final ClassDesc CD_MethodHandles_Lookup_ClassOption = Util.classDesc(MethodHandles.Lookup.ClassOption.class);
    public static final ClassDesc CD_MethodHandles_Lookup_ClassOption_array = CD_MethodHandles_Lookup_ClassOption.arrayType();

    //-----------------------------------------------------
    // java.lang.ref
    //-----------------------------------------------------

    public static final ClassDesc CD_Reference = Util.classDesc(Reference.class);

    //-----------------------------------------------------
    // java.util
    //-----------------------------------------------------

    public static final ClassDesc CD_ArrayList = Util.classDesc(ArrayList.class);
    public static final ClassDesc CD_Arrays = Util.classDesc(Arrays.class);
    public static final ClassDesc CD_Collection = Util.classDesc(Collection.class);
    public static final ClassDesc CD_HashMap = Util.classDesc(HashMap.class);
    public static final ClassDesc CD_HashSet = Util.classDesc(HashSet.class);
    public static final ClassDesc CD_Iterator = Util.classDesc(Iterator.class);
    public static final ClassDesc CD_Map_Entry = Util.classDesc(Map.Entry.class);
    public static final ClassDesc CD_Map_Entry_array = CD_Map_Entry.arrayType();
    public static final ClassDesc CD_Objects = Util.classDesc(Objects.class);
    public static final ClassDesc CD_Optional = Util.classDesc(Optional.class);
    public static final ClassDesc CD_Base64 = Util.classDesc(Base64.class);
    public static final ClassDesc CD_Base64_Decoder = Util.classDesc(Base64.Decoder.class);

    //-----------------------------------------------------
    // java.util.concurrent
    //-----------------------------------------------------

    public static final ClassDesc CD_Lock = Util.classDesc(Lock.class);

    //-----------------------------------------------------
    // java.util.function
    //-----------------------------------------------------

    public static final ClassDesc CD_Consumer = Util.classDesc(Consumer.class);
    public static final ClassDesc CD_Supplier = Util.classDesc(Supplier.class);

    //-----------------------------------------------------
    // java.io
    //-----------------------------------------------------

    public static final ClassDesc CD_InputStream = Util.classDesc(InputStream.class);
    public static final ClassDesc CD_OutputStream = Util.classDesc(OutputStream.class);
    public static final ClassDesc CD_PrintStream = Util.classDesc(PrintStream.class);

    //=====================================================
    // Method descriptors
    //=====================================================

    //-----------------------------------------------------
    // java.lang
    //-----------------------------------------------------

    public static final class FD_System {
        private FD_System() {
        }

        public static final FieldDesc in = FieldDesc.of(CD_System, "in", CD_InputStream);
        public static final FieldDesc out = FieldDesc.of(CD_System, "out", CD_PrintStream);
        public static final FieldDesc err = FieldDesc.of(CD_System, "err", CD_PrintStream);
    }

    public static final class MD_Object {
        private MD_Object() {
        }

        public static final ClassMethodDesc equals = ClassMethodDesc.of(CD_Object, "equals", CD_boolean, CD_Object);
        public static final ClassMethodDesc getClass = ClassMethodDesc.of(CD_Object, "getClass", CD_Class);
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Object, "hashCode", CD_int);
        public static final ClassMethodDesc toString = ClassMethodDesc.of(CD_Object, "toString", CD_String);
    }

    public static final class MD_Class {

        private MD_Class() {
        }

        public static final ClassMethodDesc asSubclass = ClassMethodDesc.of(CD_Class, "asSubclass", CD_Class, CD_Class);
        public static final ClassMethodDesc cast = ClassMethodDesc.of(CD_Class, "cast", CD_Object, CD_Object);
        public static final ClassMethodDesc getClassLoader = ClassMethodDesc.of(CD_Class, "getClassLoader", CD_ClassLoader);
        public static final ClassMethodDesc getName = ClassMethodDesc.of(CD_Class, "getName", CD_String);
        public static final ClassMethodDesc isInterface = ClassMethodDesc.of(CD_Class, "isInterface", CD_boolean);
        public static final ClassMethodDesc forName = ClassMethodDesc.of(CD_Class, "forName", CD_Class, CD_String);
        public static final ClassMethodDesc forName_3 = ClassMethodDesc.of(CD_Class, "forName", CD_Class, CD_String,
                CD_boolean, CD_ClassLoader);
        public static final ClassMethodDesc desiredAssertionStatus = ClassMethodDesc.of(CD_Class, "desiredAssertionStatus",
                CD_boolean);
        public static final ClassMethodDesc getResourceAsStream = ClassMethodDesc.of(CD_Class, "getResourceAsStream",
                CD_InputStream, CD_String);
    }

    public static final class MD_String {
        private MD_String() {
        }

        public static final ClassMethodDesc valueOf_boolean = ClassMethodDesc.of(CD_String, "valueOf", CD_String, CD_boolean);
        public static final ClassMethodDesc valueOf_char = ClassMethodDesc.of(CD_String, "valueOf", CD_String, CD_char);
        public static final ClassMethodDesc valueOf_char_array = ClassMethodDesc.of(CD_String, "valueOf", CD_String,
                CD_char_array);
        public static final ClassMethodDesc valueOf_int = ClassMethodDesc.of(CD_String, "valueOf", CD_String, CD_int);
        public static final ClassMethodDesc valueOf_long = ClassMethodDesc.of(CD_String, "valueOf", CD_String, CD_long);
        public static final ClassMethodDesc valueOf_float = ClassMethodDesc.of(CD_String, "valueOf", CD_String, CD_float);
        public static final ClassMethodDesc valueOf_double = ClassMethodDesc.of(CD_String, "valueOf", CD_String, CD_double);
        public static final ClassMethodDesc valueOf_Object = ClassMethodDesc.of(CD_String, "valueOf", CD_String, CD_Object);

        public static ClassMethodDesc valueOf(ClassDesc inputType) {
            return switch (inputType.descriptorString()) {
                case "Z" -> valueOf_boolean;
                case "B", "S", "I" -> valueOf_int;
                case "C" -> valueOf_char;
                case "J" -> valueOf_long;
                case "F" -> valueOf_float;
                case "D" -> valueOf_double;
                case "[C" -> valueOf_char_array;
                default -> valueOf_Object;
            };
        }

        public static final ClassMethodDesc charAt = ClassMethodDesc.of(CD_String, "charAt", CD_char, CD_int);
        public static final ClassMethodDesc codePointAt = ClassMethodDesc.of(CD_String, "codePointAt", CD_int, CD_int);
        public static final ClassMethodDesc compareTo = ClassMethodDesc.of(CD_String, "compareTo", CD_int, CD_String);
        public static final ClassMethodDesc concat = ClassMethodDesc.of(CD_String, "concat", CD_String, CD_String);
        public static final ClassMethodDesc indexOf_String = ClassMethodDesc.of(CD_String, "indexOf", CD_int, CD_String);
        public static final ClassMethodDesc indexOf_int = ClassMethodDesc.of(CD_String, "indexOf", CD_int, CD_int);
        public static final ClassMethodDesc isBlank = ClassMethodDesc.of(CD_String, "isBlank", CD_boolean);
        public static final ClassMethodDesc isEmpty = ClassMethodDesc.of(CD_String, "isEmpty", CD_boolean);
        public static final ClassMethodDesc lastIndexOf_String = ClassMethodDesc.of(CD_String, "lastIndexOf", CD_int,
                CD_String);
        public static final ClassMethodDesc lastIndexOf_int = ClassMethodDesc.of(CD_String, "lastIndexOf", CD_int, CD_int);
        public static final ClassMethodDesc length = ClassMethodDesc.of(CD_String, "length", CD_int);
        public static final ClassMethodDesc substring_1 = ClassMethodDesc.of(CD_String, "substring", CD_String, CD_int);
        public static final ClassMethodDesc substring_2 = ClassMethodDesc.of(CD_String, "substring", CD_String, CD_int, CD_int);
    }

    public static final class MD_StringBuilder {
        private MD_StringBuilder() {
        }

        public static final ClassMethodDesc append_boolean = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_boolean);
        public static final ClassMethodDesc append_int = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_int);
        public static final ClassMethodDesc append_long = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_long);
        public static final ClassMethodDesc append_float = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_float);
        public static final ClassMethodDesc append_double = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_double);
        public static final ClassMethodDesc append_char = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_char);
        public static final ClassMethodDesc append_char_array = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_char_array);
        public static final ClassMethodDesc append_String = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_String);
        public static final ClassMethodDesc append_CharSequence = ClassMethodDesc.of(CD_StringBuilder, "append",
                CD_StringBuilder, CD_CharSequence);
        public static final ClassMethodDesc append_Object = ClassMethodDesc.of(CD_StringBuilder, "append", CD_StringBuilder,
                CD_Object);

        public static ClassMethodDesc append(ClassDesc type) {
            return switch (type.descriptorString()) {
                case "Z" -> append_boolean;
                case "B", "S", "I" -> append_int;
                case "J" -> append_long;
                case "F" -> append_float;
                case "D" -> append_double;
                case "C" -> append_char;
                case "[C" -> append_char_array;
                case "[Ljava/lang/String;" -> append_String;
                case "[Ljava/lang/CharSequence;" -> append_CharSequence;
                default -> append_Object;
            };
        }

        public static final ClassMethodDesc appendCodePoint = ClassMethodDesc.of(CD_StringBuilder, "appendCodePoint",
                CD_StringBuilder, CD_int);
        public static final ClassMethodDesc setLength = ClassMethodDesc.of(CD_StringBuilder, "setLength", CD_void, CD_int);
        public static final ClassMethodDesc compareTo = ClassMethodDesc.of(CD_StringBuilder, "compareTo", CD_int,
                CD_StringBuilder);
    }

    public static final class MD_Boolean {
        private MD_Boolean() {
        }

        /**
         * The descriptor of {@link Boolean#hashCode(boolean)}.
         * For {@link Boolean#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Boolean, "hashCode", CD_int, CD_boolean);
    }

    public static final class MD_Byte {
        private MD_Byte() {
        }

        /**
         * The descriptor of {@link Byte#hashCode(byte)}.
         * For {@link Byte#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Byte, "hashCode", CD_int, CD_byte);
    }

    public static final class MD_Short {
        private MD_Short() {
        }

        /**
         * The descriptor of {@link Short#hashCode(short)}.
         * For {@link Short#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Short, "hashCode", CD_int, CD_short);
    }

    public static final class MD_Character {
        private MD_Character() {
        }

        /**
         * The descriptor of {@link Character#hashCode(char)}.
         * For {@link Character#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Character, "hashCode", CD_int, CD_char);
    }

    public static final class MD_Integer {
        private MD_Integer() {
        }

        /**
         * The descriptor of {@link Integer#hashCode(int)}.
         * For {@link Integer#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Integer, "hashCode", CD_int, CD_int);
    }

    public static final class MD_Long {
        private MD_Long() {
        }

        /**
         * The descriptor of {@link Long#hashCode(long)}.
         * For {@link Long#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Long, "hashCode", CD_int, CD_long);
    }

    public static final class MD_Float {
        private MD_Float() {
        }

        /**
         * The descriptor of {@link Float#hashCode(float)}.
         * For {@link Float#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Float, "hashCode", CD_int, CD_float);
        public static final ClassMethodDesc floatToIntBits = ClassMethodDesc.of(CD_Float, "floatToIntBits", CD_int, CD_float);
    }

    public static final class MD_Double {
        private MD_Double() {
        }

        /**
         * The descriptor of {@link Double#hashCode(double)}.
         * For {@link Double#hashCode()}, use {@link MD_Object#hashCode}.
         */
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Double, "hashCode", CD_int, CD_double);
        public static final ClassMethodDesc doubleToLongBits = ClassMethodDesc.of(CD_Double, "doubleToLongBits", CD_long,
                CD_double);
    }

    public static final class MD_ClassLoader {
        private MD_ClassLoader() {
        }

        public static final ClassMethodDesc loadClass = ClassMethodDesc.of(CD_ClassLoader, "loadClass", CD_Class, CD_String);
    }

    public static final class MD_AutoCloseable {
        private MD_AutoCloseable() {
        }

        public static final InterfaceMethodDesc close = InterfaceMethodDesc.of(CD_AutoCloseable, "close", CD_void);
    }

    public static final class MD_Thread {
        private MD_Thread() {
        }

        public static final ClassMethodDesc currentThread = ClassMethodDesc.of(CD_Thread, "currentThread", CD_Thread);
    }

    public static final class MD_Iterable {
        private MD_Iterable() {
        }

        public static final InterfaceMethodDesc iterator = InterfaceMethodDesc.of(CD_Iterable, "iterator", CD_Iterator);
    }

    public static final class MD_Comparable {
        private MD_Comparable() {
        }

        public static final InterfaceMethodDesc compareTo = InterfaceMethodDesc.of(CD_Comparable, "compareTo", CD_int,
                CD_Object);
    }

    public static final class MD_Throwable {
        private MD_Throwable() {
        }

        public static final ClassMethodDesc getMessage = ClassMethodDesc.of(CD_Throwable, "getMessage", CD_String);
        public static final ClassMethodDesc getLocalizedMessage = ClassMethodDesc.of(CD_Throwable, "getLocalizedMessage",
                CD_String);
        public static final ClassMethodDesc getCause = ClassMethodDesc.of(CD_Throwable, "getCause", CD_Throwable);
        public static final ClassMethodDesc getSuppressed = ClassMethodDesc.of(CD_Throwable, "getCause", CD_Throwable_array);
        public static final ClassMethodDesc addSuppressed = ClassMethodDesc.of(CD_Throwable, "addSuppressed", CD_void,
                CD_Throwable);
    }

    //-----------------------------------------------------
    // java.lang.invoke
    //-----------------------------------------------------

    public static final class MD_VarHandle {
        private MD_VarHandle() {
        }

        public static final ClassMethodDesc loadLoadFence = ClassMethodDesc.of(CD_VarHandle, "loadLoadFence", CD_void);
        public static final ClassMethodDesc storeStoreFence = ClassMethodDesc.of(CD_VarHandle, "storeStoreFence", CD_void);
        public static final ClassMethodDesc acquireFence = ClassMethodDesc.of(CD_VarHandle, "acquireFence", CD_void);
        public static final ClassMethodDesc releaseFence = ClassMethodDesc.of(CD_VarHandle, "releaseFence", CD_void);
        public static final ClassMethodDesc fullFence = ClassMethodDesc.of(CD_VarHandle, "fullFence", CD_void);
    }

    public static final class MD_MethodHandle {
        private MD_MethodHandle() {
        }

        public static final ClassMethodDesc asType = ClassMethodDesc.of(CD_MethodHandle, "asType", CD_MethodHandle,
                CD_MethodType);
    }

    public static final class MD_MethodHandles {
        private MD_MethodHandles() {
        }

        public static final ClassMethodDesc constant = ClassMethodDesc.of(CD_MethodHandles, "constant", CD_MethodHandle,
                CD_Class, CD_Object);

        public static final class Lookup {
            private Lookup() {
            }

            public static final ClassMethodDesc defineHiddenClass = ClassMethodDesc.of(CD_MethodHandles_Lookup,
                    "defineHiddenClass", CD_MethodHandles_Lookup, CD_byte_array, CD_boolean,
                    CD_MethodHandles_Lookup_ClassOption_array);
            public static final ClassMethodDesc lookupClass = ClassMethodDesc.of(CD_MethodHandles_Lookup, "lookupClass",
                    CD_Class);
            public static final ClassMethodDesc findConstructor = ClassMethodDesc.of(CD_MethodHandles_Lookup, "findConstructor",
                    CD_MethodHandle, CD_MethodType);
        }
    }

    public static final class MD_MethodType {
        private MD_MethodType() {
        }

        public static final ClassMethodDesc changeReturnType = ClassMethodDesc.of(CD_MethodType, "changeReturnType",
                CD_MethodType, CD_Class);
        public static final ClassMethodDesc parameterCount = ClassMethodDesc.of(CD_MethodType, "parameterCount", CD_int);
    }

    //-----------------------------------------------------
    // java.lang.ref
    //-----------------------------------------------------

    public static final class MD_Reference {
        private MD_Reference() {
        }

        public static final ClassMethodDesc reachabilityFence = ClassMethodDesc.of(CD_Reference, "reachabilityFence", CD_void,
                CD_Object);
    }

    //-----------------------------------------------------
    // java.io
    //-----------------------------------------------------

    public static final class MD_InputStream {
        private MD_InputStream() {
        }

        public static final ClassMethodDesc read = ClassMethodDesc.of(CD_InputStream, "read", CD_int);
    }

    public static final class MD_PrintStream {
        private MD_PrintStream() {
        }

        public static final ClassMethodDesc printf = ClassMethodDesc.of(CD_PrintStream, "printf", CD_PrintStream, CD_String,
                CD_Object_array);
    }

    //-----------------------------------------------------
    // java.util
    //-----------------------------------------------------

    public static final class MD_Collection {
        private MD_Collection() {
        }

        public static final InterfaceMethodDesc isEmpty = InterfaceMethodDesc.of(CD_Collection, "isEmpty", CD_boolean);
        public static final InterfaceMethodDesc size = InterfaceMethodDesc.of(CD_Collection, "size", CD_int);
        public static final InterfaceMethodDesc clear = InterfaceMethodDesc.of(CD_Collection, "clear", CD_void);
        public static final InterfaceMethodDesc containsAll = InterfaceMethodDesc.of(CD_Collection, "containsAll",
                CD_boolean, CD_Collection);
        public static final InterfaceMethodDesc contains = InterfaceMethodDesc.of(CD_Collection, "contains", CD_boolean,
                CD_Object);
        public static final InterfaceMethodDesc add = InterfaceMethodDesc.of(CD_Collection, "add", CD_boolean, CD_Object);
        public static final InterfaceMethodDesc addAll = InterfaceMethodDesc.of(CD_Collection, "addAll", CD_boolean,
                CD_Collection);
        public static final InterfaceMethodDesc remove = InterfaceMethodDesc.of(CD_Collection, "remove", CD_boolean, CD_Object);
        public static final InterfaceMethodDesc removeAll = InterfaceMethodDesc.of(CD_Collection, "removeAll",
                CD_boolean, CD_Collection);
    }

    public static final class MD_List {
        private MD_List() {
        }

        public static final InterfaceMethodDesc of_0 = InterfaceMethodDesc.of(CD_List, "of", CD_List);
        public static final InterfaceMethodDesc of_1 = InterfaceMethodDesc.of(CD_List, "of", CD_List, CD_Object);
        public static final InterfaceMethodDesc of_2 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(2, CD_Object));
        public static final InterfaceMethodDesc of_3 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(3, CD_Object));
        public static final InterfaceMethodDesc of_4 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(4, CD_Object));
        public static final InterfaceMethodDesc of_5 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(5, CD_Object));
        public static final InterfaceMethodDesc of_6 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(6, CD_Object));
        public static final InterfaceMethodDesc of_7 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(7, CD_Object));
        public static final InterfaceMethodDesc of_8 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(8, CD_Object));
        public static final InterfaceMethodDesc of_9 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(9, CD_Object));
        public static final InterfaceMethodDesc of_10 = InterfaceMethodDesc.of(CD_List, "of", CD_List,
                Collections.nCopies(10, CD_Object));
        public static final InterfaceMethodDesc of_array = InterfaceMethodDesc.of(CD_List, "of", CD_List, CD_Object_array);

        public static InterfaceMethodDesc of_n(int n) {
            return switch (n) {
                case 0 -> of_0;
                case 1 -> of_1;
                case 2 -> of_2;
                case 3 -> of_3;
                case 4 -> of_4;
                case 5 -> of_5;
                case 6 -> of_6;
                case 7 -> of_7;
                case 8 -> of_8;
                case 9 -> of_9;
                case 10 -> of_10;
                default -> throw new IllegalArgumentException("Invalid list element count");
            };
        }

        public static final InterfaceMethodDesc copyOf = InterfaceMethodDesc.of(CD_List, "copyOf", CD_List, CD_Collection);

        public static final InterfaceMethodDesc get = InterfaceMethodDesc.of(CD_List, "get", CD_Object, CD_int);
    }

    public static final class MD_Set {
        private MD_Set() {
        }

        public static final InterfaceMethodDesc of_0 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set);
        public static final InterfaceMethodDesc of_1 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set, CD_Object);
        public static final InterfaceMethodDesc of_2 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(2, CD_Object));
        public static final InterfaceMethodDesc of_3 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(3, CD_Object));
        public static final InterfaceMethodDesc of_4 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(4, CD_Object));
        public static final InterfaceMethodDesc of_5 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(5, CD_Object));
        public static final InterfaceMethodDesc of_6 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(6, CD_Object));
        public static final InterfaceMethodDesc of_7 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(7, CD_Object));
        public static final InterfaceMethodDesc of_8 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(8, CD_Object));
        public static final InterfaceMethodDesc of_9 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(9, CD_Object));
        public static final InterfaceMethodDesc of_10 = InterfaceMethodDesc.of(CD_Set, "of", CD_Set,
                Collections.nCopies(10, CD_Object));
        public static final InterfaceMethodDesc of_array = InterfaceMethodDesc.of(CD_Set, "of", CD_Set, CD_Object_array);

        public static InterfaceMethodDesc of_n(int n) {
            return switch (n) {
                case 0 -> of_0;
                case 1 -> of_1;
                case 2 -> of_2;
                case 3 -> of_3;
                case 4 -> of_4;
                case 5 -> of_5;
                case 6 -> of_6;
                case 7 -> of_7;
                case 8 -> of_8;
                case 9 -> of_9;
                case 10 -> of_10;
                default -> throw new IllegalArgumentException("Invalid set element count");
            };
        }

        public static final InterfaceMethodDesc copyOf = InterfaceMethodDesc.of(CD_Set, "copyOf", CD_Set, CD_Collection);
    }

    public static final class MD_Map {

        private MD_Map() {
        }

        public static final InterfaceMethodDesc of_0 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map);
        public static final InterfaceMethodDesc of_1 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(2, CD_Object));
        public static final InterfaceMethodDesc of_2 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(4, CD_Object));
        public static final InterfaceMethodDesc of_3 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(6, CD_Object));
        public static final InterfaceMethodDesc of_4 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(8, CD_Object));
        public static final InterfaceMethodDesc of_5 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(10, CD_Object));
        public static final InterfaceMethodDesc of_6 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(12, CD_Object));
        public static final InterfaceMethodDesc of_7 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(14, CD_Object));
        public static final InterfaceMethodDesc of_8 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(16, CD_Object));
        public static final InterfaceMethodDesc of_9 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(18, CD_Object));
        public static final InterfaceMethodDesc of_10 = InterfaceMethodDesc.of(CD_Map, "of", CD_Map,
                Collections.nCopies(20, CD_Object));
        public static final InterfaceMethodDesc ofEntries = InterfaceMethodDesc.of(CD_Map, "ofEntries", CD_Map,
                CD_Map_Entry_array);

        public static InterfaceMethodDesc of_n(int n) {
            return switch (n) {
                case 0 -> of_0;
                case 1 -> of_1;
                case 2 -> of_2;
                case 3 -> of_3;
                case 4 -> of_4;
                case 5 -> of_5;
                case 6 -> of_6;
                case 7 -> of_7;
                case 8 -> of_8;
                case 9 -> of_9;
                case 10 -> of_10;
                default -> throw new IllegalArgumentException("Invalid map element count");
            };
        }

        public static final InterfaceMethodDesc entry = InterfaceMethodDesc.of(CD_Map, "entry", CD_Map_Entry, CD_Object,
                CD_Object);

        public static final InterfaceMethodDesc get = InterfaceMethodDesc.of(CD_Map, "get", CD_Object, CD_Object);
        public static final InterfaceMethodDesc put = InterfaceMethodDesc.of(CD_Map, "put", CD_Object, CD_Object, CD_Object);
        public static final InterfaceMethodDesc remove = InterfaceMethodDesc.of(CD_Map, "remove", CD_Object, CD_Object);
        public static final InterfaceMethodDesc isEmpty = InterfaceMethodDesc.of(CD_Map, "isEmpty", CD_boolean);
        public static final InterfaceMethodDesc size = InterfaceMethodDesc.of(CD_Map, "size", CD_int);
        public static final InterfaceMethodDesc containsKey = InterfaceMethodDesc.of(CD_Map, "containsKey", CD_boolean,
                CD_Object);
        public static final InterfaceMethodDesc containsValue = InterfaceMethodDesc.of(CD_Map, "containsValue", CD_boolean,
                CD_Object);
        public static final InterfaceMethodDesc clear = InterfaceMethodDesc.of(CD_Map, "clear", CD_void);
        public static final InterfaceMethodDesc keySet = InterfaceMethodDesc.of(CD_Map, "keySet", CD_Set);
        public static final InterfaceMethodDesc values = InterfaceMethodDesc.of(CD_Map, "values", CD_Collection);
        public static final InterfaceMethodDesc entrySet = InterfaceMethodDesc.of(CD_Map, "entrySet", CD_Set);

        public static final class Entry {
            private Entry() {
            }

            public static final InterfaceMethodDesc getKey = InterfaceMethodDesc.of(CD_Map_Entry, "getKey", CD_Object);
            public static final InterfaceMethodDesc getValue = InterfaceMethodDesc.of(CD_Map_Entry, "getValue", CD_Object);
        }
    }

    public static final class MD_Iterator {
        private MD_Iterator() {
        }

        public static final InterfaceMethodDesc hasNext = InterfaceMethodDesc.of(CD_Iterator, "hasNext", CD_boolean);
        public static final InterfaceMethodDesc next = InterfaceMethodDesc.of(CD_Iterator, "next", CD_Object);
        public static final InterfaceMethodDesc remove = InterfaceMethodDesc.of(CD_Iterator, "remove", CD_void);
        public static final InterfaceMethodDesc forEachRemaining = InterfaceMethodDesc.of(CD_Iterator, "forEachRemaining",
                CD_void, CD_Consumer);
    }

    public static final class MD_Arrays {
        private MD_Arrays() {
        }

        public static final ClassMethodDesc equals_boolean = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean,
                CD_boolean_array, CD_boolean_array);
        public static final ClassMethodDesc equals_byte = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_byte_array,
                CD_byte_array);
        public static final ClassMethodDesc equals_char = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_char_array,
                CD_char_array);
        public static final ClassMethodDesc equals_short = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_short_array,
                CD_short_array);
        public static final ClassMethodDesc equals_int = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_int_array,
                CD_int_array);
        public static final ClassMethodDesc equals_long = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_long_array,
                CD_long_array);
        public static final ClassMethodDesc equals_float = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_float_array,
                CD_float_array);
        public static final ClassMethodDesc equals_double = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_double_array,
                CD_double_array);
        public static final ClassMethodDesc equals_Object = ClassMethodDesc.of(CD_Arrays, "equals", CD_boolean, CD_Object_array,
                CD_Object_array);

        public static ClassMethodDesc equals(ClassDesc elementType) {
            return switch (elementType.descriptorString().charAt(0)) {
                case 'Z' -> equals_boolean;
                case 'B' -> equals_byte;
                case 'C' -> equals_char;
                case 'S' -> equals_short;
                case 'I' -> equals_int;
                case 'J' -> equals_long;
                case 'F' -> equals_float;
                case 'D' -> equals_double;
                case 'L', '[' -> equals_Object;
                default -> throw Assert.impossibleSwitchCase(elementType);
            };
        }

        public static final ClassMethodDesc hashCode_boolean = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int,
                CD_boolean_array);
        public static final ClassMethodDesc hashCode_byte = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int, CD_byte_array);
        public static final ClassMethodDesc hashCode_char = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int, CD_char_array);
        public static final ClassMethodDesc hashCode_short = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int, CD_short_array);
        public static final ClassMethodDesc hashCode_int = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int, CD_int_array);
        public static final ClassMethodDesc hashCode_long = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int, CD_long_array);
        public static final ClassMethodDesc hashCode_float = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int, CD_float_array);
        public static final ClassMethodDesc hashCode_double = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int,
                CD_double_array);
        public static final ClassMethodDesc hashCode_Object = ClassMethodDesc.of(CD_Arrays, "hashCode", CD_int,
                CD_Object_array);

        public static ClassMethodDesc hashCode(ClassDesc elementType) {
            return switch (elementType.descriptorString().charAt(0)) {
                case 'Z' -> hashCode_boolean;
                case 'B' -> hashCode_byte;
                case 'C' -> hashCode_char;
                case 'S' -> hashCode_short;
                case 'I' -> hashCode_int;
                case 'J' -> hashCode_long;
                case 'F' -> hashCode_float;
                case 'D' -> hashCode_double;
                case 'L', '[' -> hashCode_Object;
                default -> throw Assert.impossibleSwitchCase(elementType);
            };
        }

        public static final ClassMethodDesc toString_boolean = ClassMethodDesc.of(CD_Arrays, "toString", CD_String,
                CD_boolean_array);
        public static final ClassMethodDesc toString_byte = ClassMethodDesc.of(CD_Arrays, "toString", CD_String, CD_byte_array);
        public static final ClassMethodDesc toString_char = ClassMethodDesc.of(CD_Arrays, "toString", CD_String, CD_char_array);
        public static final ClassMethodDesc toString_short = ClassMethodDesc.of(CD_Arrays, "toString", CD_String,
                CD_short_array);
        public static final ClassMethodDesc toString_int = ClassMethodDesc.of(CD_Arrays, "toString", CD_String, CD_int_array);
        public static final ClassMethodDesc toString_long = ClassMethodDesc.of(CD_Arrays, "toString", CD_String, CD_long_array);
        public static final ClassMethodDesc toString_float = ClassMethodDesc.of(CD_Arrays, "toString", CD_String,
                CD_float_array);
        public static final ClassMethodDesc toString_double = ClassMethodDesc.of(CD_Arrays, "toString", CD_String,
                CD_double_array);
        public static final ClassMethodDesc toString_Object = ClassMethodDesc.of(CD_Arrays, "toString", CD_String,
                CD_Object_array);

        public static ClassMethodDesc toString(ClassDesc elementType) {
            return switch (elementType.descriptorString().charAt(0)) {
                case 'Z' -> toString_boolean;
                case 'B' -> toString_byte;
                case 'C' -> toString_char;
                case 'S' -> toString_short;
                case 'I' -> toString_int;
                case 'J' -> toString_long;
                case 'F' -> toString_float;
                case 'D' -> toString_double;
                case 'L', '[' -> toString_Object;
                default -> throw Assert.impossibleSwitchCase(elementType);
            };
        }

        public static final ClassMethodDesc deepEquals = ClassMethodDesc.of(CD_Arrays, "deepEquals", CD_boolean,
                CD_Object_array, CD_Object_array);
        public static final ClassMethodDesc deepHashCode = ClassMethodDesc.of(CD_Arrays, "deepHashCode", CD_int,
                CD_Object_array);
        public static final ClassMethodDesc deepToString = ClassMethodDesc.of(CD_Arrays, "deepToString", CD_String,
                CD_Object_array);
    }

    public static final class MD_Objects {
        private MD_Objects() {
        }

        public static final ClassMethodDesc equals = ClassMethodDesc.of(CD_Objects, "equals", CD_boolean, CD_Object, CD_Object);
        public static final ClassMethodDesc hashCode = ClassMethodDesc.of(CD_Objects, "hashCode", CD_int, CD_Object);
    }

    public static final class MD_Optional {
        private MD_Optional() {
        }

        public static final ClassMethodDesc of = ClassMethodDesc.of(CD_Optional, "of", CD_Optional, CD_Object);
        public static final ClassMethodDesc ofNullable = ClassMethodDesc.of(CD_Optional, "ofNullable", CD_Optional, CD_Object);
        public static final ClassMethodDesc empty = ClassMethodDesc.of(CD_Optional, "empty", CD_Optional);

        public static final ClassMethodDesc get = ClassMethodDesc.of(CD_Optional, "get", CD_Object);
        public static final ClassMethodDesc isPresent = ClassMethodDesc.of(CD_Optional, "isPresent", CD_boolean);
        public static final ClassMethodDesc isEmpty = ClassMethodDesc.of(CD_Optional, "isEmpty", CD_boolean);
        public static final ClassMethodDesc orElse = ClassMethodDesc.of(CD_Optional, "orElse", CD_Object, CD_Object);
    }

    public static final class MD_Base64 {
        private MD_Base64() {
        }

        public static final ClassMethodDesc getUrlDecoder = ClassMethodDesc.of(CD_Base64, "getUrlDecoder", CD_Base64_Decoder);

        public static final class Decoder {
            private Decoder() {
            }

            public static final ClassMethodDesc decode_1 = ClassMethodDesc.of(CD_Base64_Decoder, "decode", CD_byte_array,
                    CD_byte_array);
            public static final ClassMethodDesc decode_2 = ClassMethodDesc.of(CD_Base64_Decoder, "decode", CD_int,
                    CD_byte_array, CD_byte_array);
            public static final ClassMethodDesc decode_String = ClassMethodDesc.of(CD_Base64_Decoder, "decode", CD_byte_array,
                    CD_String);
        }
    }

    //-----------------------------------------------------
    // java.util.concurrent
    //-----------------------------------------------------

    public static final class MD_Lock {
        private MD_Lock() {
        }

        public static final InterfaceMethodDesc lock = InterfaceMethodDesc.of(CD_Lock, "lock", CD_void);
        public static final InterfaceMethodDesc unlock = InterfaceMethodDesc.of(CD_Lock, "unlock", CD_void);
    }

    //-----------------------------------------------------
}
