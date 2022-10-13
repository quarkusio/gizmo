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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class BytecodeCreatorImpl implements BytecodeCreator {

    private static final MethodDescriptor THREAD_CURRENT_THREAD = MethodDescriptor.ofMethod(Thread.class, "currentThread",
            Thread.class);

    private static final MethodDescriptor THREAD_GET_TCCL = MethodDescriptor.ofMethod(Thread.class, "getContextClassLoader",
            ClassLoader.class);

    private static final MethodDescriptor CL_FOR_NAME = MethodDescriptor.ofMethod(Class.class, "forName", Class.class,
            String.class,
            boolean.class, ClassLoader.class);

    private static final boolean DEBUG_SCOPES = Boolean.getBoolean("io.quarkus.gizmo.debug-scopes");

    private static final Map<String, AtomicInteger> functionCountersByClass = new ConcurrentHashMap<>();

    private static final String FUNCTION = "$$function$$";

    protected final List<Operation> operations = new ArrayList<>();

    private final MethodCreatorImpl method;

    private final BytecodeCreatorImpl owner;

    private final Label top = new Label();
    private final Label bottom = new Label();
    private final StackTraceElement[] stack;

    private static final Map<String, String> boxingMap;
    private static final Map<String, String> boxingMethodMap;

    private ResultHandle cachedTccl;

    static {
        Map<String, String> b = new HashMap<>();
        b.put("Z", Type.getInternalName(Boolean.class));
        b.put("B", Type.getInternalName(Byte.class));
        b.put("C", Type.getInternalName(Character.class));
        b.put("S", Type.getInternalName(Short.class));
        b.put("I", Type.getInternalName(Integer.class));
        b.put("J", Type.getInternalName(Long.class));
        b.put("F", Type.getInternalName(Float.class));
        b.put("D", Type.getInternalName(Double.class));
        boxingMap = Collections.unmodifiableMap(b);

        b = new HashMap<>();
        b.put("Z", "booleanValue");
        b.put("B", "byteValue");
        b.put("C", "charValue");
        b.put("S", "shortValue");
        b.put("I", "intValue");
        b.put("J", "longValue");
        b.put("F", "floatValue");
        b.put("D", "doubleValue");
        boxingMethodMap = Collections.unmodifiableMap(b);
    }

    Label getTop() {
        return top;
    }

    Label getBottom() {
        return bottom;
    }

    BytecodeCreatorImpl(BytecodeCreatorImpl enclosing, MethodCreatorImpl methodCreator) {
        this.method = methodCreator;
        this.owner = enclosing;
        stack = DEBUG_SCOPES ? new Throwable().getStackTrace() : null;
    }

    BytecodeCreatorImpl(BytecodeCreatorImpl enclosing, boolean useThisMethod) {
        this.method = useThisMethod ? (MethodCreatorImpl) this : enclosing.getMethod();
        this.owner = enclosing;
        stack = DEBUG_SCOPES ? new Throwable().getStackTrace() : null;
    }

    BytecodeCreatorImpl(BytecodeCreatorImpl enclosing) {
        this(enclosing, enclosing.getMethod());
    }

    @Override
    public ResultHandle getThis() {
        ResultHandle resultHandle = new ResultHandle("L" + getMethod().getDeclaringClassName().replace('.', '/') + ";", this);
        resultHandle.setNo(0);
        return resultHandle;
    }

    @Override
    public ResultHandle invokeVirtualMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args) {
        Objects.requireNonNull(descriptor);
        Objects.requireNonNull(object);
        ResultHandle ret = allocateResult(descriptor.getReturnType());
        operations.add(new InvokeOperation(ret, descriptor, resolve(checkScope(object)), resolve(checkScope(args)), false, false));
        return ret;
    }

    @Override
    public ResultHandle invokeInterfaceMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args) {
        Objects.requireNonNull(descriptor);
        Objects.requireNonNull(object);
        ResultHandle ret = allocateResult(descriptor.getReturnType());
        operations.add(new InvokeOperation(ret, descriptor, resolve(checkScope(object)), resolve(checkScope(args)), true, false));
        return ret;
    }

    @Override
    public ResultHandle invokeStaticMethod(MethodDescriptor descriptor, ResultHandle... args) {
        Objects.requireNonNull(descriptor);
        ResultHandle ret = allocateResult(descriptor.getReturnType());
        operations.add(new InvokeOperation(ret, descriptor, resolve(checkScope(args)), false));
        return ret;
    }


    public ResultHandle invokeStaticInterfaceMethod(MethodDescriptor descriptor, ResultHandle... args) {
        Objects.requireNonNull(descriptor);
        ResultHandle ret = allocateResult(descriptor.getReturnType());
        operations.add(new InvokeOperation(ret, descriptor, resolve(checkScope(args)), true));
        return ret;
    }

    @Override
    public ResultHandle invokeSpecialMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args) {
        Objects.requireNonNull(descriptor);
        Objects.requireNonNull(object);
        ResultHandle ret = allocateResult(descriptor.getReturnType());
        operations.add(new InvokeOperation(ret, descriptor, resolve(checkScope(object)), resolve(checkScope(args)), false, true));
        return ret;
    }

    @Override
    public ResultHandle invokeSpecialInterfaceMethod(MethodDescriptor descriptor, ResultHandle object, ResultHandle... args) {
        Objects.requireNonNull(descriptor);
        Objects.requireNonNull(object);
        ResultHandle ret = allocateResult(descriptor.getReturnType());
        operations.add(new InvokeOperation(ret, descriptor, resolve(checkScope(object)), resolve(checkScope(args)), true, true));
        return ret;
    }

    @Override
    public ResultHandle newInstance(MethodDescriptor descriptor, ResultHandle... args) {
        Objects.requireNonNull(descriptor);
        ResultHandle ret = allocateResult("L" + descriptor.getDeclaringClass() + ";");
        operations.add(new NewInstanceOperation(ret, descriptor, resolve(checkScope(args))));
        return ret;
    }

    @Override
    public ResultHandle newArray(String type, ResultHandle length) {
        Objects.requireNonNull(length);
        String resultType;
        if (!type.startsWith("[")) {
            //assume a single dimension array
            resultType = "[" + DescriptorUtils.objectToDescriptor(type);
        } else {
            resultType = DescriptorUtils.objectToDescriptor(type);
        }
        if (resultType.startsWith("[[")) {
            throw new RuntimeException("Multidimensional arrays not supported yet");
        }
        final ResultHandle resolvedLength = resolve(checkScope(length));
        char typeChar = resultType.charAt(1);
        if (typeChar != 'L') {
            //primitive arrays
            int opcode;
            switch (typeChar) {
                case 'Z':
                    opcode = Opcodes.T_BOOLEAN;
                    break;
                case 'B':
                    opcode = Opcodes.T_BYTE;
                    break;
                case 'C':
                    opcode = Opcodes.T_CHAR;
                    break;
                case 'S':
                    opcode = Opcodes.T_SHORT;
                    break;
                case 'I':
                    opcode = Opcodes.T_INT;
                    break;
                case 'J':
                    opcode = Opcodes.T_LONG;
                    break;
                case 'F':
                    opcode = Opcodes.T_FLOAT;
                    break;
                case 'D':
                    opcode = Opcodes.T_DOUBLE;
                    break;
                default:
                    throw new RuntimeException("Unknown type " + type);
            }
            ResultHandle ret = allocateResult(resultType);
            operations.add(new Operation() {
                @Override
                public void writeBytecode(MethodVisitor methodVisitor) {
                    loadResultHandle(methodVisitor, resolvedLength, BytecodeCreatorImpl.this, "I");
                    methodVisitor.visitIntInsn(Opcodes.NEWARRAY, opcode);
                    storeResultHandle(methodVisitor, ret);
                }

                @Override
                Set<ResultHandle> getInputResultHandles() {
                    return Collections.singleton(resolvedLength);
                }

                @Override
                ResultHandle getTopResultHandle() {
                    return resolvedLength;
                }

                @Override
                ResultHandle getOutgoingResultHandle() {
                    return ret;
                }
            });
            return ret;
        } else {
            //object arrays
            String arrayType = resultType.substring(2, resultType.length() - 1);
            ResultHandle ret = allocateResult(resultType);
            operations.add(new Operation() {
                @Override
                public void writeBytecode(MethodVisitor methodVisitor) {
                    loadResultHandle(methodVisitor, resolvedLength, BytecodeCreatorImpl.this, "I");
                    methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, arrayType);
                    storeResultHandle(methodVisitor, ret);
                }

                @Override
                Set<ResultHandle> getInputResultHandles() {
                    return Collections.singleton(resolvedLength);
                }

                @Override
                ResultHandle getTopResultHandle() {
                    return resolvedLength;
                }

                @Override
                ResultHandle getOutgoingResultHandle() {
                    return ret;
                }
            });
            return ret;
        }

    }

    @Override
    public ResultHandle load(String val) {
        Objects.requireNonNull(val);
        if (val.length() > 65535) {
            //TODO: we could auto split this, but I don't think we really want strings this long
            throw new IllegalArgumentException("Cannot load strings larger than " + 65535 + " bytes");
        }
        return new ResultHandle("Ljava/lang/String;", this, val);
    }

    @Override
    public ResultHandle load(byte val) {
        return new ResultHandle("B", this, val);
    }

    @Override
    public ResultHandle load(short val) {
        return new ResultHandle("S", this, val);
    }

    @Override
    public ResultHandle load(char val) {
        return new ResultHandle("C", this, val);
    }

    @Override
    public ResultHandle load(int val) {
        return new ResultHandle("I", this, val);
    }

    @Override
    public ResultHandle load(long val) {
        return new ResultHandle("J", this, val);
    }

    @Override
    public ResultHandle load(float val) {
        return new ResultHandle("F", this, val);
    }

    @Override
    public ResultHandle load(double val) {
        return new ResultHandle("D", this, val);
    }

    @Override
    public ResultHandle load(boolean val) {
        return new ResultHandle("Z", this, val);
    }

    @Override
    public ResultHandle loadClass(String className) {
        return loadClass(className, false);
    }

    @Override
    public ResultHandle loadClassFromTCCL(String className) {
        return loadClass(className, true);
    }

    private ResultHandle loadClass(final String className, final boolean useTccl) {
        Objects.requireNonNull(className);
        final Class<?> primitiveType = matchPossiblyPrimitive(className);
        if (primitiveType == null) {
            if (useTccl) {
                if (cachedTccl == null) {
                    ResultHandle currentThread = invokeStaticMethod(THREAD_CURRENT_THREAD);
                    cachedTccl = invokeVirtualMethod(THREAD_GET_TCCL, currentThread);
                }
                return invokeStaticMethod(CL_FOR_NAME,
                        load(className), load(false), cachedTccl);
            } else {
                return new ResultHandle("Ljava/lang/Class;", this, Type.getObjectType(className.replace('.', '/')));
            }
        } else {
            Class<?> pt = primitiveType;
            ResultHandle ret = new ResultHandle("Ljava/lang/Class;", this);
            operations.add(new Operation() {
                @Override
                void writeBytecode(MethodVisitor methodVisitor) {
                    methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(pt), "TYPE", "Ljava/lang/Class;");
                    storeResultHandle(methodVisitor, ret);
                }

                @Override
                Set<ResultHandle> getInputResultHandles() {
                    return Collections.emptySet();
                }

                @Override
                ResultHandle getTopResultHandle() {
                    return null;
                }

                @Override
                ResultHandle getOutgoingResultHandle() {
                    return ret;
                }
            });
            return ret;
        }
    }

    private Class<?> matchPossiblyPrimitive(final String className) {
        switch (className) {
            case "boolean" : return Boolean.class;
            case "byte" : return Byte.class;
            case "char" : return Character.class;
            case "short" : return Short.class;
            case "int" : return Integer.class;
            case "long" : return Long.class;
            case "float" : return Float.class;
            case "double" : return Double.class;
            case "void" : return Void.class;
            default: return null;
        }
    }

    @Override
    public ResultHandle loadNull() {
        return ResultHandle.NULL;
    }

    @Override
    public void writeInstanceField(FieldDescriptor fieldDescriptor, ResultHandle instance, ResultHandle value) {
        Objects.requireNonNull(instance);
        Objects.requireNonNull(value);
        final ResultHandle resolvedInstance = resolve(checkScope(instance));
        final ResultHandle resolvedValue = resolve(checkScope(value));
        operations.add(new Operation() {
            @Override
            void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedInstance, BytecodeCreatorImpl.this, "L" + fieldDescriptor.getDeclaringClass() + ";");
                loadResultHandle(methodVisitor, resolvedValue, BytecodeCreatorImpl.this, fieldDescriptor.getType());
                methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, fieldDescriptor.getDeclaringClass(), fieldDescriptor.getName(), fieldDescriptor.getType());
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return new LinkedHashSet<>(Arrays.asList(resolvedInstance, resolvedValue));
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedInstance;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return null;
            }
        });
    }

    @Override
    public ResultHandle readInstanceField(FieldDescriptor fieldDescriptor, ResultHandle instance) {
        Objects.requireNonNull(fieldDescriptor);
        Objects.requireNonNull(instance);
        ResultHandle resultHandle = allocateResult(fieldDescriptor.getType());
        ResultHandle resolvedInstance = resolve(checkScope(instance));
        operations.add(new Operation() {
            @Override
            void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedInstance, BytecodeCreatorImpl.this, "L" + fieldDescriptor.getDeclaringClass() + ";");
                methodVisitor.visitFieldInsn(Opcodes.GETFIELD, fieldDescriptor.getDeclaringClass(), fieldDescriptor.getName(), fieldDescriptor.getType());
                storeResultHandle(methodVisitor, resultHandle);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.singleton(resolvedInstance);
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedInstance;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return resultHandle;
            }
        });
        return resultHandle;
    }

    @Override
    public void writeStaticField(FieldDescriptor fieldDescriptor, ResultHandle value) {
        Objects.requireNonNull(fieldDescriptor);
        Objects.requireNonNull(value);
        ResultHandle resolvedValue = resolve(checkScope(value));
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedValue, BytecodeCreatorImpl.this, fieldDescriptor.getType());
                methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, fieldDescriptor.getDeclaringClass(), fieldDescriptor.getName(), fieldDescriptor.getType());
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.singleton(resolvedValue);
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedValue;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return null;
            }
        });
    }

    @Override
    public ResultHandle readStaticField(FieldDescriptor fieldDescriptor) {
        Objects.requireNonNull(fieldDescriptor);
        ResultHandle result = allocateResult(fieldDescriptor.getType());
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, fieldDescriptor.getDeclaringClass(), fieldDescriptor.getName(), fieldDescriptor.getType());
                storeResultHandle(methodVisitor, result);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.emptySet();
            }

            @Override
            ResultHandle getTopResultHandle() {
                return null;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return result;
            }
        });
        return result;
    }

    @Override
    public ResultHandle arrayLength(ResultHandle array) {
        ResultHandle result = new ResultHandle("I", this);
        ResultHandle resolvedArray = resolve(checkScope(array));
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedArray, BytecodeCreatorImpl.this, resolvedArray.getType());
                methodVisitor.visitInsn(Opcodes.ARRAYLENGTH);
                storeResultHandle(methodVisitor, result);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.singleton(resolvedArray);
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedArray;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return result;
            }
        });
        return result;
    }

    @Override
    public ResultHandle readArrayValue(ResultHandle array, ResultHandle index) {
        if (!array.getType().startsWith("[")) {
            throw new IllegalArgumentException("Not array type: " + array.getType());
        }
        ResultHandle result = allocateResult(array.getType().substring(1));
        ResultHandle resolvedArray = resolve(checkScope(array));
        ResultHandle resolvedIndex = resolve(checkScope(index));
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedArray, BytecodeCreatorImpl.this, resolvedArray.getType());
                loadResultHandle(methodVisitor, resolvedIndex, BytecodeCreatorImpl.this, "I");
                switch (result.getType()) {
                    case "I":
                        methodVisitor.visitInsn(Opcodes.IALOAD);
                        break;
                    case "J":
                        methodVisitor.visitInsn(Opcodes.LALOAD);
                        break;
                    case "F":
                        methodVisitor.visitInsn(Opcodes.FALOAD);
                        break;
                    case "D":
                        methodVisitor.visitInsn(Opcodes.DALOAD);
                        break;
                    case "B":
                    case "Z":
                        methodVisitor.visitInsn(Opcodes.BALOAD);
                        break;
                    case "C":
                        methodVisitor.visitInsn(Opcodes.CALOAD);
                        break;
                    case "S":
                        methodVisitor.visitInsn(Opcodes.SALOAD);
                        break;
                    default:
                        methodVisitor.visitInsn(Opcodes.AALOAD);
                }
                storeResultHandle(methodVisitor, result);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return new LinkedHashSet<>(Arrays.asList(resolvedArray, resolvedIndex));
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedArray;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return result;
            }
        });
        return result;
    }

    @Override
    public void writeArrayValue(ResultHandle array, ResultHandle index, ResultHandle value) {
        ResultHandle resolvedArray = resolve(checkScope(array));
        ResultHandle resolvedIndex = resolve(checkScope(index));
        ResultHandle resolvedValue = resolve(checkScope(value));
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedArray, BytecodeCreatorImpl.this, resolvedArray.getType());
                loadResultHandle(methodVisitor, resolvedIndex, BytecodeCreatorImpl.this, "I");
                String arrayType = resolvedArray.getType().substring(1);
                loadResultHandle(methodVisitor, resolvedValue, BytecodeCreatorImpl.this, arrayType);
                if (arrayType.equals("Z") || arrayType.equals("B")) {
                    methodVisitor.visitInsn(Opcodes.BASTORE);
                } else if (arrayType.equals("S")) {
                    methodVisitor.visitInsn(Opcodes.SASTORE);
                } else if (arrayType.equals("I")) {
                    methodVisitor.visitInsn(Opcodes.IASTORE);
                } else if (arrayType.equals("C")) {
                    methodVisitor.visitInsn(Opcodes.CASTORE);
                } else if (arrayType.equals("J")) {
                    methodVisitor.visitInsn(Opcodes.LASTORE);
                } else if (arrayType.equals("F")) {
                    methodVisitor.visitInsn(Opcodes.FASTORE);
                } else if (arrayType.equals("D")) {
                    methodVisitor.visitInsn(Opcodes.DASTORE);
                } else {
                    methodVisitor.visitInsn(Opcodes.AASTORE);
                }
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return new LinkedHashSet<>(Arrays.asList(resolvedArray, resolvedIndex, resolvedValue));
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedArray;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return null;
            }
        });
    }

    @Override
    public AssignableResultHandle createVariable(final String typeDescr) {
        Objects.requireNonNull(typeDescr);
        return new AssignableResultHandle(typeDescr, this);
    }

    @Override
    public void assign(final AssignableResultHandle target, final ResultHandle value) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(value);
        ResultHandle resolvedTarget = resolve(checkScope(target));
        ResultHandle resolvedValue = resolve(checkScope(value));
        if (resolvedTarget instanceof AssignableResultHandle) {
            operations.add(new AssignOperation(resolvedValue, resolvedTarget));
        } else {
            throw new IllegalArgumentException("Cannot assign to captured variables");
        }
    }

    @Override
    public ResultHandle checkCast(final ResultHandle resultHandle, final String castTarget) {
        Objects.requireNonNull(resultHandle);
        Objects.requireNonNull(castTarget);
        final ResultHandle result;
        String intName = castTarget.replace('.', '/');
        if (intName.startsWith("[") || intName.endsWith(";")) {
            result = allocateResult(intName);
        } else {
            result = allocateResult("L" + intName + ";");
        }
        // seems like a waste of local vars but it's the safest approach since result type can't be mutated
        final ResultHandle resolvedResultHandle = resolve(checkScope(resultHandle));
        assert result != null;
        operations.add(new Operation() {
            @Override
            void writeBytecode(final MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedResultHandle, BytecodeCreatorImpl.this, result.getType());
                storeResultHandle(methodVisitor, result);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.singleton(resolvedResultHandle);
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedResultHandle;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return result;
            }
        });
        return result;
    }

    @Override
    public boolean isScopedWithin(final BytecodeCreator other) {
        return other == this || owner != null && owner.isScopedWithin(other);
    }

    @Override
    public void continueScope(final BytecodeCreator scope) {
        if (!isScopedWithin(scope)) {
            throw new IllegalArgumentException("Cannot continue non-enclosing scope");
        }
        operations.add(new JumpOperation(((BytecodeCreatorImpl) scope).top));
    }

    @Override
    public void breakScope(final BytecodeCreator scope) {
        if (!isScopedWithin(scope)) {
            throw new IllegalArgumentException("Cannot break non-enclosing scope");
        }
        operations.add(new JumpOperation(((BytecodeCreatorImpl) scope).bottom));
    }

    @Override
    public BytecodeCreator createScope() {
        final BytecodeCreatorImpl enclosed = new BytecodeCreatorImpl(this);
        operations.add(new BlockOperation(enclosed));
        return enclosed;
    }

    static void storeResultHandle(MethodVisitor methodVisitor, ResultHandle handle) {
        if (handle.getResultType() == ResultHandle.ResultType.UNUSED) {
            if (handle.getType().equals("J") || handle.getType().equals("D")) {
                methodVisitor.visitInsn(Opcodes.POP2);
            } else {
                methodVisitor.visitInsn(Opcodes.POP);
            }
        } else if (handle.getResultType() == ResultHandle.ResultType.LOCAL_VARIABLE) {
            if (handle.getType().equals("S") || handle.getType().equals("Z") || handle.getType().equals("I") || handle.getType().equals("B") || handle.getType().equals("C")) {
                methodVisitor.visitVarInsn(Opcodes.ISTORE, handle.getNo());
            } else if (handle.getType().equals("J")) {
                methodVisitor.visitVarInsn(Opcodes.LSTORE, handle.getNo());
            } else if (handle.getType().equals("F")) {
                methodVisitor.visitVarInsn(Opcodes.FSTORE, handle.getNo());
            } else if (handle.getType().equals("D")) {
                methodVisitor.visitVarInsn(Opcodes.DSTORE, handle.getNo());
            } else {
                methodVisitor.visitVarInsn(Opcodes.ASTORE, handle.getNo());
            }
        }
    }

    void loadResultHandle(MethodVisitor methodVisitor, ResultHandle handle, BytecodeCreatorImpl bc, String expectedType) {
        loadResultHandle(methodVisitor, handle, bc, expectedType, false);
    }

    void loadResultHandle(MethodVisitor methodVisitor, ResultHandle handle, BytecodeCreatorImpl bc, String expectedType, boolean dontCast) {
        if (handle.getResultType() == ResultHandle.ResultType.CONSTANT) {
            if (handle.getConstant() == null) {
                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
            } else {
                emitLoadConstant(methodVisitor, handle.getConstant());
                if (!dontCast && !expectedType.equals(handle.getType())) {
                    if (expectedType.length() > 1 && handle.getType().length() > 1) {
                        // both objects, we just do a checkcast
                        if (!expectedType.equals("Ljava/lang/Object;")) {
                            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, DescriptorUtils.getTypeStringFromDescriptorFormat(expectedType));
                        }
                    } else if (expectedType.length() == 1 && handle.getType().length() == 1) {
                        // both primitives, ignore
                    } else if (expectedType.length() == 1) {
                        // expected primitive, must auto-unbox
                        String type = boxingMap.get(expectedType);
                        if (type == null) {
                            throw new RuntimeException("Unknown primitive type " + expectedType);
                        }
                        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, type);
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type, boxingMethodMap.get(expectedType), "()" + expectedType, false);
                    } else {
                        // expected primitive wrapper, must auto-box
                        String type = boxingMap.get(handle.getType());
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, type, "valueOf", "(" + handle.getType() + ")L" + type + ";", false);
                    }
                }
            }
            return;
        }
        if (!isScopedWithin(handle.getOwner())) {
//            throw new IllegalStateException("Wrong owner for ResultHandle " + handle);
        }
        if (handle.getResultType() != ResultHandle.ResultType.SINGLE_USE) {
            if (handle.getType().equals("S") || handle.getType().equals("Z") || handle.getType().equals("I") || handle.getType().equals("B") || handle.getType().equals("C")) {
                methodVisitor.visitVarInsn(Opcodes.ILOAD, handle.getNo());
            } else if (handle.getType().equals("J")) {
                methodVisitor.visitVarInsn(Opcodes.LLOAD, handle.getNo());
            } else if (handle.getType().equals("F")) {
                methodVisitor.visitVarInsn(Opcodes.FLOAD, handle.getNo());
            } else if (handle.getType().equals("D")) {
                methodVisitor.visitVarInsn(Opcodes.DLOAD, handle.getNo());
            } else {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, handle.getNo());
            }
        }
        if (!dontCast && !expectedType.equals(handle.getType())) {
            if (expectedType.length() > 1 && handle.getType().length() > 1) {
                // both objects, we just do a checkcast
                if (!expectedType.equals("Ljava/lang/Object;")) {
                    methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, DescriptorUtils.getTypeStringFromDescriptorFormat(expectedType));
                }
            } else if (expectedType.length() == 1 && handle.getType().length() == 1) {
                // both primitives, ignore
            } else if (expectedType.length() == 1) {
                // expected primitive, must auto-unbox
                String type = boxingMap.get(expectedType);
                if (type == null) {
                    throw new RuntimeException("Unknown primitive type " + expectedType);
                }
                methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, type);
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, type, boxingMethodMap.get(expectedType), "()" + expectedType, false);
            } else {
                // expected primitive wrapper, must auto-box
                String type = boxingMap.get(handle.getType());
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, type, "valueOf", "(" + handle.getType() + ")L" + type + ";", false);
            }
        }
    }

    private static void emitLoadConstant(MethodVisitor methodVisitor, Object constant) {
        if (constant instanceof Byte || constant instanceof Short) {
            constant = ((Number) constant).intValue();
        }

        if (Boolean.FALSE.equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_0);
        } else if (Boolean.TRUE.equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_1);
        } else if (Integer.valueOf(-1).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_M1);
        } else if (Integer.valueOf(0).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_0);
        } else if (Integer.valueOf(1).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_1);
        } else if (Integer.valueOf(2).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_2);
        } else if (Integer.valueOf(3).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_3);
        } else if (Integer.valueOf(4).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_4);
        } else if (Integer.valueOf(5).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.ICONST_5);
        } else if (Long.valueOf(0L).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.LCONST_0);
        } else if (Long.valueOf(1L).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.LCONST_1);
        } else if (Float.valueOf(0.0F).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.FCONST_0);
        } else if (Float.valueOf(1.0F).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.FCONST_1);
        } else if (Double.valueOf(0.0).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.DCONST_0);
        } else if (Double.valueOf(1.0).equals(constant)) {
            methodVisitor.visitInsn(Opcodes.DCONST_1);
        } else {
            // if original constant was `byte` or `short`, it is `int` now,
            // but that makes no difference
            methodVisitor.visitLdcInsn(constant);
        }
    }

    @Override
    public TryBlock tryBlock() {
        final TryBlockImpl tryBlock = new TryBlockImpl(this);
        operations.add(new BlockOperation(tryBlock));
        return tryBlock;
    }

    @Override
    public ResultHandle compareLong(ResultHandle value1, ResultHandle value2) {
        return emitCompare(Opcodes.LCMP, "J", value1, value2);
    }

    @Override
    public ResultHandle compareFloat(ResultHandle value1, ResultHandle value2, boolean nanComparesAsGreater) {
        return emitCompare(nanComparesAsGreater ? Opcodes.FCMPG : Opcodes.FCMPL, "F", value1, value2);
    }

    @Override
    public ResultHandle compareDouble(ResultHandle value1, ResultHandle value2, boolean nanComparesAsGreater) {
        return emitCompare(nanComparesAsGreater ? Opcodes.DCMPG : Opcodes.DCMPL, "D", value1, value2);
    }

    private ResultHandle emitCompare(int opcode, String expectedType, ResultHandle value1, ResultHandle value2) {
        Objects.requireNonNull(value1);
        Objects.requireNonNull(value2);
        if (!expectedType.equals(value1.getType()) || !expectedType.equals(value2.getType())) {
            throw new IllegalArgumentException("Arguments must both be of type " + expectedType);
        }
        final ResultHandle resolved1 = resolve(checkScope(value1));
        final ResultHandle resolved2 = resolve(checkScope(value2));
        final ResultHandle result = allocateResult("I");
        assert result != null;
        operations.add(new Operation() {
            @Override
            void writeBytecode(final MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolved1, BytecodeCreatorImpl.this, resolved1.getType(), true);
                loadResultHandle(methodVisitor, resolved2, BytecodeCreatorImpl.this, resolved2.getType(), true);
                methodVisitor.visitInsn(opcode);
                storeResultHandle(methodVisitor, result);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return new LinkedHashSet<>(Arrays.asList(resolved1, resolved2));
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolved1;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return result;
            }
        });
        return result;
    }

    @Override
    public BranchResult ifNonZero(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFNE, "I");
    }

    @Override
    public BranchResult ifNull(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFNULL, "Ljava/lang/Object;");
    }

    @Override
    public BranchResult ifZero(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFEQ, "I");
    }

    @Override
    public BranchResult ifTrue(ResultHandle resultHandle) {
        return ifNonZero(resultHandle);
    }

    @Override
    public BranchResult ifFalse(ResultHandle resultHandle) {
        return ifZero(resultHandle);
    }

    @Override
    public BranchResult ifNotNull(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFNONNULL, "Ljava/lang/Object;");
    }

    @Override
    public BranchResult ifGreaterThanZero(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFGT, "I");
    }

    @Override
    public BranchResult ifGreaterEqualZero(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFGE, "I");
    }

    @Override
    public BranchResult ifLessThanZero(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFLT, "I");
    }

    @Override
    public BranchResult ifLessEqualZero(ResultHandle resultHandle) {
        return ifValue(resultHandle, Opcodes.IFLE, "I");
    }

    @Override
    public BranchResult ifIntegerEqual(ResultHandle value1, ResultHandle value2) {
        return ifValues(value1, value2, Opcodes.IF_ICMPEQ, "I");
    }

    @Override
    public BranchResult ifIntegerGreaterThan(ResultHandle value1, ResultHandle value2) {
        return ifValues(value1, value2, Opcodes.IF_ICMPGT, "I");
    }

    @Override
    public BranchResult ifIntegerGreaterEqual(ResultHandle value1, ResultHandle value2) {
        return ifValues(value1, value2, Opcodes.IF_ICMPGE, "I");
    }

    @Override
    public BranchResult ifIntegerLessThan(ResultHandle value1, ResultHandle value2) {
        return ifValues(value1, value2, Opcodes.IF_ICMPLT, "I");
    }

    @Override
    public ResultHandle instanceOf(ResultHandle resultHandle, String castTarget) {
        Objects.requireNonNull(resultHandle);
        Objects.requireNonNull(castTarget);
        final ResultHandle result = allocateResult("I");
        // seems like a waste of local vars but it's the safest approach since result type can't be mutated
        final ResultHandle resolvedResultHandle = resolve(checkScope(resultHandle));
        assert result != null;
        String intName = castTarget.replace('.', '/');
        operations.add(new Operation() {
            @Override
            void writeBytecode(final MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedResultHandle, BytecodeCreatorImpl.this, resultHandle.getType(), true);
                methodVisitor.visitTypeInsn(Opcodes.INSTANCEOF, intName);
                storeResultHandle(methodVisitor, result);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.singleton(resolvedResultHandle);
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedResultHandle;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return result;
            }
        });
        return result;
    }

    @Override
    public BranchResult ifIntegerLessEqual(ResultHandle value1, ResultHandle value2) {
        return ifValues(value1, value2, Opcodes.IF_ICMPLE, "I");
    }

    @Override
    public BranchResult ifReferencesEqual(ResultHandle ref1, ResultHandle ref2) {
        return ifValues(ref1, ref2, Opcodes.IF_ACMPEQ, "Ljava/lang/Object;");
    }
    
    @Override
    public IfThenElse ifThenElse(ResultHandle value) {
        IfThenElseImpl ifThen = new IfThenElseImpl(this, Objects.requireNonNull(value));
        operations.add(new BlockOperation(ifThen));
        return ifThen;
    }

    @Override
    public ResultHandle getMethodParam(int methodNo) {
        int count = (method.getModifiers() & Modifier.STATIC) != 0 ? 0 : 1;
        for (int i = 0; i < methodNo; ++i) {
            String s = getMethod().getMethodDescriptor().getParameterTypes()[i];
            if (s.equals("J") || s.equals("D")) {
                count += 2;
            } else {
                count++;
            }
        }
        ResultHandle resultHandle = new ResultHandle(getMethod().getMethodDescriptor().getParameterTypes()[methodNo], this);
        resultHandle.setNo(count);
        return resultHandle;
    }

    @Override
    public FunctionCreator createFunction(Class<?> functionalInterface) {
        if (!functionalInterface.isInterface()) {
            throw new IllegalArgumentException("Not an interface " + functionalInterface);
        }
        Method functionMethod = null;
        for (Method m : functionalInterface.getMethods()) {
            if (m.isDefault() || Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            if (functionMethod != null) {
                throw new IllegalArgumentException("Not a functional interface " + functionalInterface);
            } else {
                functionMethod = m;
            }
        }
        if (functionMethod == null) {
            throw new IllegalArgumentException("Could not find function method " + functionalInterface);
        }


        final String declaringClassName = getMethod().getDeclaringClassName();
        final AtomicInteger counter = functionCountersByClass.computeIfAbsent(declaringClassName, k -> new AtomicInteger());
        final String functionName = declaringClassName + FUNCTION + counter.incrementAndGet();
        ResultHandle ret = new ResultHandle("L" + functionName.replace('.', '/') + ";", this);
        ClassCreator cc = ClassCreator.builder().enclosing(this).classOutput(getMethod().getClassOutput()).className(functionName).interfaces(functionalInterface).build();
        MethodCreatorImpl mc = (MethodCreatorImpl) cc.getMethodCreator(functionMethod.getName(), functionMethod.getReturnType(), functionMethod.getParameterTypes());

        FunctionCreatorImpl fc = mc.addFunctionBody(ret, cc, mc, this);
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                fc.writeCreateInstance(methodVisitor);
                cc.close();
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.emptySet();
            }

            @Override
            ResultHandle getTopResultHandle() {
                return null;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return fc.getInstance();
            }

            @Override
            public void findResultHandles(Set<ResultHandle> vc) {
                vc.addAll(fc.getCapturedResultHandles());
            }
        });
        return fc;
    }

    @Override
    public void returnValue(ResultHandle returnValue) {
        ResultHandle resolvedReturnValue = resolve(checkScope(returnValue));
        final MethodDescriptor methodDescriptor = getMethod().getMethodDescriptor();
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                if (resolvedReturnValue == null
                        || methodDescriptor.getReturnType().equals("V")) { //ignore value for void methods, makes client code simpler
                    methodVisitor.visitInsn(Opcodes.RETURN);
                } else {
                    loadResultHandle(methodVisitor, resolvedReturnValue, BytecodeCreatorImpl.this, methodDescriptor.getReturnType());
                    if (methodDescriptor.getReturnType().equals("S") || methodDescriptor.getReturnType().equals("Z")
                            || methodDescriptor.getReturnType().equals("I") || methodDescriptor.getReturnType().equals("B")
                            || methodDescriptor.getReturnType().equals("C")) {
                        methodVisitor.visitInsn(Opcodes.IRETURN);
                    } else if (methodDescriptor.getReturnType().equals("J")) {
                        methodVisitor.visitInsn(Opcodes.LRETURN);
                    } else if (methodDescriptor.getReturnType().equals("F")) {
                        methodVisitor.visitInsn(Opcodes.FRETURN);
                    } else if (methodDescriptor.getReturnType().equals("D")) {
                        methodVisitor.visitInsn(Opcodes.DRETURN);
                    } else {
                        methodVisitor.visitInsn(Opcodes.ARETURN);
                    }
                }
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                if (resolvedReturnValue == null) {
                    return Collections.emptySet();
                }
                return Collections.singleton(resolvedReturnValue);
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedReturnValue;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return null;
            }
        });
    }

    @Override
    public void throwException(ResultHandle exception) {
        Objects.requireNonNull(exception);
        ResultHandle resolvedException = resolve(checkScope(exception));
        operations.add(new Operation() {
            @Override
            public void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, resolvedException, BytecodeCreatorImpl.this, "Ljava/lang/Throwable;");
                methodVisitor.visitInsn(Opcodes.ATHROW);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.singleton(resolvedException);
            }

            @Override
            ResultHandle getTopResultHandle() {
                return resolvedException;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return null;
            }
        });
    }

    @Override
    public WhileLoop whileLoop(Function<BytecodeCreator, BranchResult> conditionFun) {
        Objects.requireNonNull(conditionFun);
        WhileLoopImpl loop = new WhileLoopImpl(this, conditionFun);
        operations.add(new BlockOperation(loop));
        return loop;
    }
    
    @Override
    public ForEachLoop forEach(ResultHandle iterable) {
        ForEachLoopImpl loop = new ForEachLoopImpl(this, Objects.requireNonNull(iterable));
        operations.add(new BlockOperation(loop));
        return loop;
    }

    @Override
    public ResultHandle add(ResultHandle a1, ResultHandle a2) {
        return emitArithmetic(Opcodes.IADD, a1, a2);
    }

    @Override
    public ResultHandle multiply(ResultHandle a1, ResultHandle a2) {
        return emitArithmetic(Opcodes.IMUL, a1, a2);
    }

    private ResultHandle emitArithmetic(int intOpcode, ResultHandle a1, ResultHandle a2) {
        Objects.requireNonNull(a1);
        Objects.requireNonNull(a2);
        if (!a1.getType().equals(a2.getType())) {
            //TODO: relax this somewhat
            throw new RuntimeException("ResultHandles must be of the same type");
        }
        ResultHandle ret = new ResultHandle(a1.getType(), this);
        operations.add(new Operation() {
            @Override
            void writeBytecode(MethodVisitor methodVisitor) {
                loadResultHandle(methodVisitor, a1, BytecodeCreatorImpl.this, a1.getType());
                loadResultHandle(methodVisitor, a2, BytecodeCreatorImpl.this, a2.getType());
                if (a1.getType().equals("J")) {
                    methodVisitor.visitInsn(longArithmeticOpcode(intOpcode));
                } else if (a1.getType().equals("D")) {
                    methodVisitor.visitInsn(doubleArithmeticOpcode(intOpcode));
                } else if (a1.getType().equals("F")) {
                    methodVisitor.visitInsn(floatArithmeticOpcode(intOpcode));
                } else {
                    methodVisitor.visitInsn(intOpcode);
                }
                storeResultHandle(methodVisitor, ret);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return new LinkedHashSet<>(Arrays.asList(a1, a2));
            }

            @Override
            ResultHandle getTopResultHandle() {
                return a1;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return ret;
            }
        });
        return ret;
    }

    private int longArithmeticOpcode(int intOpcode) {
        switch (intOpcode) {
            case Opcodes.IADD:
                return Opcodes.LADD;
            case Opcodes.ISUB:
                return Opcodes.LSUB;
            case Opcodes.IMUL:
                return Opcodes.LMUL;
            case Opcodes.IDIV:
                return Opcodes.LDIV;
            case Opcodes.IREM:
                return Opcodes.LREM;
            case Opcodes.INEG:
                return Opcodes.LNEG;
            case Opcodes.ISHL:
                return Opcodes.LSHL;
            case Opcodes.ISHR:
                return Opcodes.LSHR;
            case Opcodes.IUSHR:
                return Opcodes.LUSHR;
            case Opcodes.IAND:
                return Opcodes.LAND;
            case Opcodes.IOR:
                return Opcodes.LOR;
            case Opcodes.IXOR:
                return Opcodes.LXOR;
            default:
                throw new IllegalArgumentException("Invalid opcode for long arithmetic: " + intOpcode);
        }
    }

    private int floatArithmeticOpcode(int intOpcode) {
        switch (intOpcode) {
            case Opcodes.IADD:
                return Opcodes.FADD;
            case Opcodes.ISUB:
                return Opcodes.FSUB;
            case Opcodes.IMUL:
                return Opcodes.FMUL;
            case Opcodes.IDIV:
                return Opcodes.FDIV;
            case Opcodes.IREM:
                return Opcodes.FREM;
            case Opcodes.INEG:
                return Opcodes.FNEG;
            default:
                throw new IllegalArgumentException("Invalid opcode for float arithmetic: " + intOpcode);
        }
    }

    private int doubleArithmeticOpcode(int intOpcode) {
        switch (intOpcode) {
            case Opcodes.IADD:
                return Opcodes.DADD;
            case Opcodes.ISUB:
                return Opcodes.DSUB;
            case Opcodes.IMUL:
                return Opcodes.DMUL;
            case Opcodes.IDIV:
                return Opcodes.DDIV;
            case Opcodes.IREM:
                return Opcodes.DREM;
            case Opcodes.INEG:
                return Opcodes.DNEG;
            default:
                throw new IllegalArgumentException("Invalid opcode for double arithmetic: " + intOpcode);
        }
    }

    private ResultHandle allocateResult(String returnType) {
        if (returnType.equals("V")) {
            return null;
        }
        return new ResultHandle(returnType, this);
    }

    protected int allocateLocalVariables(int localVarCount) {

        Set<ResultHandle> handlesToAllocate = new LinkedHashSet<>();
        findActiveResultHandles(handlesToAllocate);
        int vc = localVarCount;
        for (ResultHandle handle : handlesToAllocate) {
            if (handle.getResultType() == ResultHandle.ResultType.CONSTANT || handle.getResultType() == ResultHandle.ResultType.LOCAL_VARIABLE) {
                continue;
            }
            handle.setNo(vc);
            if (handle.getType().equals("J") || handle.getType().equals("D")) {
                vc += 2;
            } else {
                vc++;
            }
        }
        return vc;
    }

    void findActiveResultHandles(Set<ResultHandle> handlesToAllocate) {
        Operation prev = null;
        for (int i = 0; i < operations.size(); ++i) {
            Operation op = operations.get(i);
            Set<ResultHandle> toAdd = new LinkedHashSet<>(op.getInputResultHandles());
            if (prev != null &&
                    prev.getOutgoingResultHandle() != null &&
                    prev.getOutgoingResultHandle() == op.getTopResultHandle()) {
                toAdd.remove(op.getTopResultHandle());
                if (op.getTopResultHandle().getResultType() == ResultHandle.ResultType.UNUSED) {
                    op.getTopResultHandle().markSingleUse();
                }
            }
            handlesToAllocate.addAll(toAdd);
            op.findResultHandles(handlesToAllocate);
            prev = op;
        }
    }

    protected void writeOperations(MethodVisitor visitor) {
        visitor.visitLabel(top);
        writeInteriorOperations(visitor);
        visitor.visitLabel(bottom);
    }

    protected void writeInteriorOperations(MethodVisitor visitor) {
        for (Operation op : operations) {
            op.doProcess(visitor);
        }
    }

    <R extends ResultHandle> R checkScope(R handle) {
        if (handle != null) {
            final BytecodeCreatorImpl handleOwner = handle.getOwner();
            if (handleOwner != null && !isScopedWithin(handleOwner)) {
                final StringBuilder trace = new StringBuilder();
                trace.append("Result handle ").append(handle).append(" used outside of its scope\n");
                trace.append("The handle's scope is:\n");
                handleOwner.dumpScope(trace);
                trace.append("The usage scope is:\n");
                dumpScope(trace);
                throw new IllegalArgumentException(trace.toString());
            }
        }
        return handle;
    }

    private void dumpScope(final StringBuilder builder) {
        builder.append("\tat ").append(this).append('\n');
        final StackTraceElement[] stack = this.stack;
        if (stack != null) {
            final int length = stack.length;
            for (int i = 0; i < 8 && i < length; i++) {
                builder.append("\t\tat ").append(stack[i]).append('\n');
            }
            if (length > 8) {
                builder.append("\t\t...\n");
            }
        }
        if (owner != null) owner.dumpScope(builder);
    }

    ResultHandle[] checkScope(ResultHandle[] handles) {
        for (ResultHandle resultHandle : handles) {
            checkScope(resultHandle);
        }
        return handles;
    }

    final ResultHandle resolve(ResultHandle handle) {
        return resolve(handle, this);
    }

    ResultHandle resolve(ResultHandle handle, BytecodeCreator creator) {
        return owner.resolve(handle, creator);
    }

    ResultHandle[] resolve(BytecodeCreator creator, ResultHandle... handles) {
        return owner.resolve(handles);
    }

    final ResultHandle[] resolve(ResultHandle... handles) {
        return resolve(this, handles);
    }

    MethodCreatorImpl getMethod() {
        return method;
    }

    BytecodeCreatorImpl getOwner() {
        return owner;
    }

    private BranchResult ifValue(ResultHandle resultHandle, int opcode, String opType) {
        Objects.requireNonNull(resultHandle);
        Objects.requireNonNull(opType);
        ResultHandle resolvedResultHandle = resolve(checkScope(resultHandle));
        BytecodeCreatorImpl trueBranch = new BytecodeCreatorImpl(this);
        BytecodeCreatorImpl falseBranch = new BytecodeCreatorImpl(this);
        operations.add(new IfOperation(opcode, opType, resolvedResultHandle, trueBranch, falseBranch));
        return new BranchResultImpl(trueBranch, falseBranch);
    }

    private BranchResult ifValues(ResultHandle resultHandle1, ResultHandle resultHandle2, int opcode, String opType) {
        Objects.requireNonNull(resultHandle1);
        Objects.requireNonNull(resultHandle2);
        Objects.requireNonNull(opType);
        ResultHandle resolvedResultHandle1 = resolve(checkScope(resultHandle1));
        ResultHandle resolvedResultHandle2 = resolve(checkScope(resultHandle2));
        BytecodeCreatorImpl trueBranch = new BytecodeCreatorImpl(this);
        BytecodeCreatorImpl falseBranch = new BytecodeCreatorImpl(this);
        operations.add(new IfOperation(opcode, opType, resolvedResultHandle1, resolvedResultHandle2, trueBranch, falseBranch));
        return new BranchResultImpl(trueBranch, falseBranch);
    }

    static abstract class Operation {


        private final Throwable errorPoint;

        Operation() {
            if (Boolean.getBoolean("arc.debug")) {
                errorPoint = new RuntimeException("Error location");
            } else {
                errorPoint = null;
            }
        }


        public void doProcess(MethodVisitor visitor) {
            try {
                writeBytecode(visitor);
            } catch (Throwable e) {
                if (errorPoint == null) {
                    throw new RuntimeException(e);
                }
                RuntimeException ex = new RuntimeException("Exception generating bytecode", errorPoint);
                ex.addSuppressed(e);
                throw ex;
            }
        }

        abstract void writeBytecode(MethodVisitor methodVisitor);

        /**
         * Gets all result handles that are used as input to this operation
         *
         * @return The result handles
         */
        abstract Set<ResultHandle> getInputResultHandles();

        /**
         * @return The incoming result handle that is first loaded into the stack, or null if this is not applicable
         */
        abstract ResultHandle getTopResultHandle();

        /**
         * @return The result handle that is created as a result of this operation
         */
        abstract ResultHandle getOutgoingResultHandle();

        public void findResultHandles(Set<ResultHandle> vc) {
        }
    }

    static class JumpOperation extends Operation {
        private final Label target;

        JumpOperation(final Label target) {
            this.target = target;
        }

        @Override
        void writeBytecode(final MethodVisitor methodVisitor) {
            methodVisitor.visitJumpInsn(Opcodes.GOTO, target);
        }

        @Override
        Set<ResultHandle> getInputResultHandles() {
            return Collections.emptySet();
        }

        @Override
        ResultHandle getTopResultHandle() {
            return null;
        }

        @Override
        ResultHandle getOutgoingResultHandle() {
            return null;
        }
    }

    class InvokeOperation extends Operation {
        final ResultHandle resultHandle;
        final MethodDescriptor descriptor;
        final ResultHandle object;
        final ResultHandle[] args;
        final boolean staticMethod;
        final boolean interfaceMethod;
        final boolean specialMethod;

        InvokeOperation(ResultHandle resultHandle, MethodDescriptor descriptor, ResultHandle object, ResultHandle[] args, boolean interfaceMethod, boolean specialMethod) {
            if (args.length != descriptor.getParameterTypes().length) {
                throw new RuntimeException("Wrong number of params " + Arrays.toString(descriptor.getParameterTypes()) + " vs " + Arrays.toString(args));
            }
            this.resultHandle = resultHandle;
            this.descriptor = descriptor;
            this.object = object;
            this.args = args.clone();
            this.interfaceMethod = interfaceMethod;
            this.specialMethod = specialMethod;
            this.staticMethod = false;
        }

        InvokeOperation(ResultHandle resultHandle, MethodDescriptor descriptor, ResultHandle[] args, boolean isInterface) {
            if (args.length != descriptor.getParameterTypes().length) {
                throw new RuntimeException("Wrong number of params " + Arrays.toString(descriptor.getParameterTypes()) + " vs " + Arrays.toString(args));
            }
            this.resultHandle = resultHandle;
            this.descriptor = descriptor;
            this.object = null;
            this.args = args.clone();
            this.staticMethod = true;
            this.interfaceMethod = isInterface;
            this.specialMethod = false;
        }

        @Override
        public void writeBytecode(MethodVisitor methodVisitor) {
            if (object != null) {
                loadResultHandle(methodVisitor, object, BytecodeCreatorImpl.this, "L" + descriptor.getDeclaringClass() + ";", specialMethod);
            }
            for (int i = 0; i < args.length; ++i) {
                ResultHandle arg = args[i];
                loadResultHandle(methodVisitor, arg, BytecodeCreatorImpl.this, descriptor.getParameterTypes()[i]);
            }
            if (staticMethod) {
                methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, descriptor.getDeclaringClass(), descriptor.getName(), descriptor.getDescriptor(), interfaceMethod);
            } else if (specialMethod) {
                methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, descriptor.getDeclaringClass(), descriptor.getName(), descriptor.getDescriptor(), interfaceMethod);
            } else if (interfaceMethod) {
                methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, descriptor.getDeclaringClass(), descriptor.getName(), descriptor.getDescriptor(), true);
            } else {
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, descriptor.getDeclaringClass(), descriptor.getName(), descriptor.getDescriptor(), false);
            }
            if (resultHandle != null) {
                storeResultHandle(methodVisitor, resultHandle);
            }
        }

        @Override
        Set<ResultHandle> getInputResultHandles() {
            Set<ResultHandle> ret = new LinkedHashSet<>();
            if (object != null) {
                ret.add(object);
            }
            ret.addAll(Arrays.asList(args));
            return ret;
        }

        @Override
        ResultHandle getTopResultHandle() {
            if (object != null) {
                return object;
            }
            if (args.length > 0) {
                return args[0];
            }
            return null;
        }

        @Override
        ResultHandle getOutgoingResultHandle() {
            return resultHandle;
        }
    }

    Operation createNewInstanceOp(ResultHandle handle, MethodDescriptor descriptor, ResultHandle[] args) {
        return new NewInstanceOperation(handle, descriptor, args);
    }

    class NewInstanceOperation extends Operation {
        final ResultHandle resultHandle;
        final MethodDescriptor descriptor;
        final ResultHandle[] args;

        NewInstanceOperation(ResultHandle resultHandle, MethodDescriptor descriptor, ResultHandle[] args) {
            if (args.length != descriptor.getParameterTypes().length) {
                throw new RuntimeException("Wrong number of params " + Arrays.toString(descriptor.getParameterTypes()) + " vs " + Arrays.toString(args));
            }
            this.resultHandle = resultHandle;
            this.descriptor = descriptor;
            this.args = new ResultHandle[args.length];
            System.arraycopy(args, 0, this.args, 0, args.length);
        }

        @Override
        public void writeBytecode(MethodVisitor methodVisitor) {

            methodVisitor.visitTypeInsn(Opcodes.NEW, descriptor.getDeclaringClass());
            methodVisitor.visitInsn(Opcodes.DUP);
            for (int i = 0; i < args.length; ++i) {
                ResultHandle arg = args[i];
                loadResultHandle(methodVisitor, arg, BytecodeCreatorImpl.this, descriptor.getParameterTypes()[i]);
            }
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, descriptor.getDeclaringClass(), descriptor.getName(), descriptor.getDescriptor(), false);
            storeResultHandle(methodVisitor, resultHandle);
        }

        @Override
        Set<ResultHandle> getInputResultHandles() {
            return new LinkedHashSet<>(Arrays.asList(args));
        }

        @Override
        ResultHandle getTopResultHandle() {
            return null;
        }

        @Override
        ResultHandle getOutgoingResultHandle() {
            return resultHandle;
        }
    }

    class IfOperation extends Operation {
        private final int opcode;
        private final String opType;
        private final ResultHandle value1;
        private final ResultHandle value2;
        private final BytecodeCreatorImpl trueBranch;
        private final BytecodeCreatorImpl falseBranch;

        IfOperation(final int opcode, final String opType, final ResultHandle value, final BytecodeCreatorImpl trueBranch, final BytecodeCreatorImpl falseBranch) {
            this(opcode, opType, value, null, trueBranch, falseBranch);
        }

        IfOperation(final int opcode, final String opType, final ResultHandle value1, final ResultHandle value2, final BytecodeCreatorImpl trueBranch, final BytecodeCreatorImpl falseBranch) {
            this.opcode = opcode;
            this.opType = opType;
            this.value1 = value1;
            this.value2 = value2;
            this.trueBranch = trueBranch;
            this.falseBranch = falseBranch;
        }

        @Override
        public void writeBytecode(MethodVisitor methodVisitor) {
            loadResultHandle(methodVisitor, value1, BytecodeCreatorImpl.this, opType);
            if (value2 != null) {
                loadResultHandle(methodVisitor, value2, BytecodeCreatorImpl.this, opType);
            }
            methodVisitor.visitJumpInsn(opcode, trueBranch.getTop());
            falseBranch.writeOperations(methodVisitor);
            methodVisitor.visitJumpInsn(Opcodes.GOTO, trueBranch.getBottom());
            trueBranch.writeOperations(methodVisitor);
        }

        @Override
        Set<ResultHandle> getInputResultHandles() {
            if (value2 != null) {
                Set<ResultHandle> ret = new LinkedHashSet<>(2);
                ret.add(value1);
                ret.add(value2);
                return ret;
            }
            return Collections.singleton(value1);
        }

        @Override
        ResultHandle getTopResultHandle() {
            return value1;
        }

        @Override
        ResultHandle getOutgoingResultHandle() {
            return null;
        }

        @Override
        public void findResultHandles(Set<ResultHandle> vc) {
            trueBranch.findActiveResultHandles(vc);
            falseBranch.findActiveResultHandles(vc);
        }
    }

    static class BlockOperation extends Operation {
        private final BytecodeCreatorImpl block;

        BlockOperation(final BytecodeCreatorImpl block) {
            this.block = block;
        }

        @Override
        void writeBytecode(final MethodVisitor methodVisitor) {
            block.writeOperations(methodVisitor);
        }

        @Override
        Set<ResultHandle> getInputResultHandles() {
            return Collections.emptySet();
        }

        @Override
        ResultHandle getTopResultHandle() {
            return null;
        }

        @Override
        ResultHandle getOutgoingResultHandle() {
            return null;
        }

        @Override
        public void findResultHandles(final Set<ResultHandle> vc) {
            block.findActiveResultHandles(vc);
        }
    }

    class AssignOperation extends Operation {
        private final ResultHandle resolvedValue;
        private final ResultHandle resolvedTarget;

        public AssignOperation(final ResultHandle resolvedValue, final ResultHandle resolvedTarget) {
            this.resolvedValue = resolvedValue;
            this.resolvedTarget = resolvedTarget;
        }

        @Override
        void writeBytecode(MethodVisitor methodVisitor) {
            loadResultHandle(methodVisitor, resolvedValue, BytecodeCreatorImpl.this, resolvedTarget.getType());
            storeResultHandle(methodVisitor, resolvedTarget);
        }

        @Override
        Set<ResultHandle> getInputResultHandles() {
            return Collections.singleton(resolvedValue);
        }

        @Override
        ResultHandle getTopResultHandle() {
            return resolvedValue;
        }

        @Override
        ResultHandle getOutgoingResultHandle() {
            return resolvedTarget;
        }
    }
}
