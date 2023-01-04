package io.quarkus.gizmo;

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

class StringSwitchImpl extends AbstractSwitch<String> implements Switch.StringSwitch {

    private final Map<String, BytecodeCreatorImpl> caseBlocks;

    public StringSwitchImpl(ResultHandle value, BytecodeCreatorImpl enclosing) {
        super(enclosing);
        this.caseBlocks = new LinkedHashMap<>();
        ResultHandle strHash = invokeVirtualMethod(MethodDescriptor.ofMethod(Object.class, "hashCode", int.class), value);

        Set<ResultHandle> inputHandles = new HashSet<>();
        inputHandles.add(value);
        inputHandles.add(strHash);

        operations.add(new Operation() {

            @Override
            void writeBytecode(MethodVisitor methodVisitor) {
                Map<Integer, Label> hashToLabel = new HashMap<>();
                List<BytecodeCreatorImpl> lookupBlocks = new ArrayList<>();
                Map<Integer, List<Entry<String, BytecodeCreatorImpl>>> hashToCaseBlocks = new LinkedHashMap<>();

                for (Entry<String, BytecodeCreatorImpl> e : caseBlocks.entrySet()) {
                    int hashCode = e.getKey().hashCode();
                    List<Entry<String, BytecodeCreatorImpl>> list = hashToCaseBlocks.get(hashCode);
                    if (list == null) {
                        list = new ArrayList<>();
                        hashToCaseBlocks.put(hashCode, list);
                    }
                    list.add(e);
                }

                // Initialize the case blocks and lookup blocks
                for (Entry<Integer, List<Entry<String, BytecodeCreatorImpl>>> hashEntry : hashToCaseBlocks.entrySet()) {
                    BytecodeCreatorImpl lookupBlock = new BytecodeCreatorImpl(StringSwitchImpl.this);
                    for (Iterator<Entry<String, BytecodeCreatorImpl>> it = hashEntry.getValue().iterator(); it.hasNext();) {
                        Entry<String, BytecodeCreatorImpl> caseEntry = it.next();
                        BytecodeCreatorImpl caseBlock = caseEntry.getValue();
                        if (caseBlock != null && !fallThrough) {
                            caseBlock.breakScope(StringSwitchImpl.this);
                        } else if (caseBlock == null) {
                            // Add an empty fall through block
                            caseBlock = new BytecodeCreatorImpl(StringSwitchImpl.this);
                            caseEntry.setValue(caseBlock);
                        }
                        BytecodeCreatorImpl isEqual = (BytecodeCreatorImpl) lookupBlock
                                .ifTrue(Gizmo.equals(lookupBlock, lookupBlock.load(caseEntry.getKey()), value)).trueBranch();
                        isEqual.jumpTo(caseBlock);
                        if (!it.hasNext()) {
                            // None of the labels is equal to the tested value - skip to the default block
                            lookupBlock.jumpTo(defaultBlock);
                        }
                    }
                    hashToLabel.put(hashEntry.getKey(), lookupBlock.getTop());
                    lookupBlock.findActiveResultHandles(inputHandles);
                    lookupBlocks.add(lookupBlock);
                }

                // Load the hashCode of the tested value
                loadResultHandle(methodVisitor, strHash, StringSwitchImpl.this, "I");

                // The lookupswitch keys must be sorted in increasing numerical order
                int[] keys = hashToCaseBlocks.keySet().stream().mapToInt(e -> e.intValue()).sorted().toArray();
                Label[] labels = new Label[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    labels[i] = hashToLabel.get(keys[i]);
                }
                methodVisitor.visitLookupSwitchInsn(defaultBlock.getTop(), keys, labels);

                // Write the lookup blocks
                for (BytecodeCreatorImpl lookupBlock : lookupBlocks) {
                    lookupBlock.writeOperations(methodVisitor);
                }

                // Write the case blocks
                for (BytecodeCreatorImpl caseBlock : caseBlocks.values()) {
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
    public void caseOf(String value, Consumer<BytecodeCreator> caseBlockConsumer) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(caseBlockConsumer);
        addCaseBlock(value, caseBlockConsumer);
    }

    @Override
    public void caseOf(List<String> values, Consumer<BytecodeCreator> caseBlockConsumer) {
        Objects.requireNonNull(values);
        Objects.requireNonNull(caseBlockConsumer);
        for (Iterator<String> it = values.iterator(); it.hasNext();) {
            String s = it.next();
            if (it.hasNext()) {
                addCaseBlock(s, null);
            } else {
                addCaseBlock(s, caseBlockConsumer);
            }
        }
    }

    @Override
    void findActiveResultHandles(final Set<ResultHandle> handlesToAllocate) {
        super.findActiveResultHandles(handlesToAllocate);
        for (BytecodeCreatorImpl caseBlock : caseBlocks.values()) {
            if (caseBlock != null) {
                caseBlock.findActiveResultHandles(handlesToAllocate);
            }
        }
        defaultBlock.findActiveResultHandles(handlesToAllocate);
    }

    private void addCaseBlock(String value, Consumer<BytecodeCreator> blockConsumer) {
        if (caseBlocks.containsKey(value)) {
            throw new IllegalArgumentException("A case block for the string value [" + value + "] already exists");
        }
        BytecodeCreatorImpl caseBlock = null;
        if (blockConsumer != null) {
            caseBlock = new BytecodeCreatorImpl(this);
            blockConsumer.accept(caseBlock);
        }
        caseBlocks.put(value, caseBlock);
    }

}
