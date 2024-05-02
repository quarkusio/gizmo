package io.quarkus.gizmo;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

class EnumSwitchImpl<E extends Enum<E>> extends AbstractSwitch<E> implements Switch.EnumSwitch<E> {

    private final Map<Integer, BytecodeCreatorImpl> ordinalToCaseBlocks;

    public EnumSwitchImpl(ResultHandle value, Class<E> enumClass, BytecodeCreatorImpl enclosing) {
        super(enclosing);
        this.ordinalToCaseBlocks = new LinkedHashMap<>();

        MethodDescriptor enumOrdinal = MethodDescriptor.ofMethod(enumClass, "ordinal", int.class);
        ResultHandle ordinal = invokeVirtualMethod(enumOrdinal, value);

        // Generate the int[] switch table needed for binary compatibility
        ResultHandle switchTable;
        MethodCreatorImpl methodCreator = findMethodCreator(enclosing);
        if (methodCreator != null) {
            // $GIZMO_SWITCH_TABLE$org$acme$MyEnum()
            StringBuilder methodName = new StringBuilder();
            methodName.append("$GIZMO_SWITCH_TABLE");
            for (String part : enumClass.getName().split("\\.")) {
                methodName.append('$').append(part);
            }

            // Call a static method that returns the switch table
            switchTable = callSwitchTableMethod(methodName.toString(), enumClass, enumOrdinal);
        } else {
            // This is suboptimal - the switch table is generated for each switch construct
            switchTable = generateSwitchTable(enumClass, enclosing, enumOrdinal);
        }
        ResultHandle effectiveOrdinal = readArrayValue(switchTable, ordinal);

        Set<ResultHandle> inputHandles = new HashSet<>();
        inputHandles.add(effectiveOrdinal);

        operations.add(new Operation() {

            @Override
            void writeBytecode(MethodVisitor methodVisitor) {
                E[] constants = enumClass.getEnumConstants();
                Map<Integer, Label> ordinalToLabel = new HashMap<>();
                List<BytecodeCreatorImpl> caseBlocks = new ArrayList<>();

                // Initialize the case blocks
                for (Entry<Integer, BytecodeCreatorImpl> caseEntry : ordinalToCaseBlocks.entrySet()) {
                    BytecodeCreatorImpl caseBlock = caseEntry.getValue();
                    if (caseBlock != null && !fallThrough) {
                        caseBlock.breakScope(EnumSwitchImpl.this);
                    } else if (caseBlock == null) {
                        // Add an empty fall through block
                        caseBlock = new BytecodeCreatorImpl(EnumSwitchImpl.this);
                        caseEntry.setValue(caseBlock);
                    }
                    caseBlocks.add(caseBlock);
                    ordinalToLabel.put(caseEntry.getKey(), caseBlock.getTop());
                }

                int min = ordinalToLabel.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);
                int max = ordinalToLabel.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

                // Add empty blocks for missing ordinals
                // This would be suboptimal for cases if there is a large number of missing ordinals
                for (int i = 0; i < constants.length; i++) {
                    if (i >= min && i <= max) {
                        if (ordinalToLabel.get(i) == null) {
                            BytecodeCreatorImpl emptyCaseBlock = new BytecodeCreatorImpl(EnumSwitchImpl.this);
                            caseBlocks.add(emptyCaseBlock);
                            ordinalToLabel.put(i, emptyCaseBlock.getTop());
                        }
                    }
                }

                // Load the ordinal of the tested value
                loadResultHandle(methodVisitor, effectiveOrdinal, EnumSwitchImpl.this, "I");

                int[] ordinals = ordinalToLabel.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
                Label[] labels = new Label[ordinals.length];
                for (int i = 0; i < ordinals.length; i++) {
                    labels[i] = ordinalToLabel.get(ordinals[i]);
                }
                methodVisitor.visitTableSwitchInsn(min, max, defaultBlock.getTop(), labels);

                // Write the case blocks
                for (BytecodeCreatorImpl caseBlock : caseBlocks) {
                    caseBlock.writeOperations(methodVisitor);
                }

                // Write the default block
                defaultBlock.writeOperations(methodVisitor);
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
            Set<ResultHandle> getInputResultHandles() {
                return inputHandles;
            }

        });
    }

    @Override
    public void caseOf(E value, Consumer<BytecodeCreator> caseBlockConsumer) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(caseBlockConsumer);
        addCaseBlock(value, caseBlockConsumer);
    }

    @Override
    public void caseOf(List<E> values, Consumer<BytecodeCreator> caseBlockConsumer) {
        Objects.requireNonNull(values);
        Objects.requireNonNull(caseBlockConsumer);
        for (Iterator<E> it = values.iterator(); it.hasNext();) {
            E e = it.next();
            if (it.hasNext()) {
                addCaseBlock(e, null);
            } else {
                addCaseBlock(e, caseBlockConsumer);
            }
        }
    }

    @Override
    void findActiveResultHandles(final Set<ResultHandle> handlesToAllocate) {
        super.findActiveResultHandles(handlesToAllocate);
        for (BytecodeCreatorImpl caseBlock : ordinalToCaseBlocks.values()) {
            if (caseBlock != null) {
                caseBlock.findActiveResultHandles(handlesToAllocate);
            }
        }
        defaultBlock.findActiveResultHandles(handlesToAllocate);
    }

    private void addCaseBlock(E value, Consumer<BytecodeCreator> caseBlockConsumer) {
        int ordinal = value.ordinal();
        if (ordinalToCaseBlocks.containsKey(ordinal)) {
            throw new IllegalArgumentException("A case block for the enum value [" + value + "] already exists");
        }
        BytecodeCreatorImpl caseBlock = null;
        if (caseBlockConsumer != null) {
            caseBlock = new BytecodeCreatorImpl(this);
            caseBlockConsumer.accept(caseBlock);
        }
        ordinalToCaseBlocks.put(ordinal, caseBlock);
    }

    private MethodCreatorImpl findMethodCreator(BytecodeCreatorImpl enclosing) {
        if (enclosing instanceof MethodCreatorImpl) {
            return (MethodCreatorImpl) enclosing;
        }
        if (enclosing.getOwner() != null) {
            return findMethodCreator(enclosing.getOwner());
        }
        return null;
    }

    protected ResultHandle callSwitchTableMethod(String methodName, Class<E> enumClass, MethodDescriptor enumOrdinal) {
        MethodCreatorImpl methodCreator = findMethodCreator(getOwner()); // never returns null here
        ClassCreator classCreator = methodCreator.getClassCreator();

        MethodDescriptor gizmoSwitchTableDescriptor = MethodDescriptor.ofMethod(classCreator.getClassName(),
                methodName, int[].class);
        if (!classCreator.getExistingMethods().contains(gizmoSwitchTableDescriptor)) {
            MethodCreator gizmoSwitchTable = classCreator.getMethodCreator(gizmoSwitchTableDescriptor)
                    .setModifiers(ACC_PRIVATE | ACC_STATIC);
            gizmoSwitchTable.returnValue(generateSwitchTable(enumClass, gizmoSwitchTable, enumOrdinal));
        }
        return invokeStaticMethod(gizmoSwitchTableDescriptor);
    }

    protected final ResultHandle generateSwitchTable(Class<E> enumClass, BytecodeCreator bytecodeCreator,
            MethodDescriptor enumOrdinal) {
        E[] constants = enumClass.getEnumConstants();
        ResultHandle switchTable = bytecodeCreator.newArray(int.class, constants.length);
        for (int i = 0; i < constants.length; i++) {
            ResultHandle currentConstant = bytecodeCreator
                    .readStaticField(FieldDescriptor.of(enumClass, constants[i].name(), enumClass));
            ResultHandle currentOrdinal = bytecodeCreator.invokeVirtualMethod(enumOrdinal, currentConstant);
            bytecodeCreator.writeArrayValue(switchTable, i, currentOrdinal);
        }
        return switchTable;
    }

}