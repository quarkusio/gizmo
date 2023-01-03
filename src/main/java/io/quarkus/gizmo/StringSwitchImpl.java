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
    
    private final Map<Integer, List<Entry<String, Consumer<BytecodeCreator>>>> hashToCaseBlocks;

    public StringSwitchImpl(ResultHandle value, BytecodeCreatorImpl enclosing) {
        super(enclosing);
        this.hashToCaseBlocks = new LinkedHashMap<>();
        ResultHandle strHash = invokeVirtualMethod(MethodDescriptor.ofMethod(Object.class, "hashCode", int.class), value);

        Set<ResultHandle> inputHandles = new HashSet<>();
        inputHandles.add(value);
        inputHandles.add(strHash);

        operations.add(new Operation() {

            @Override
            void writeBytecode(MethodVisitor methodVisitor) {
                Map<Integer, Label> hashToLabel = new HashMap<>();
                List<BytecodeCreatorImpl> lookupBlocks = new ArrayList<>();
                Map<String, BytecodeCreatorImpl> caseBlocks = new LinkedHashMap<>();
                BytecodeCreatorImpl defaultBlock = new BytecodeCreatorImpl(StringSwitchImpl.this);
                if (defaultBlockConsumer != null) {
                    defaultBlockConsumer.accept(defaultBlock);
                }

                // Initialize the case blocks and lookup blocks
                for (Entry<Integer, List<Entry<String, Consumer<BytecodeCreator>>>> hashEntry : hashToCaseBlocks.entrySet()) {
                    BytecodeCreatorImpl lookupBlock = new BytecodeCreatorImpl(StringSwitchImpl.this);
                    for (Entry<String, Consumer<BytecodeCreator>> caseEntry : hashEntry.getValue()) {
                        BytecodeCreatorImpl caseBlock = new BytecodeCreatorImpl(StringSwitchImpl.this);
                        Consumer<BytecodeCreator> blockConsumer = caseEntry.getValue();
                        blockConsumer.accept(caseBlock);
                        if (blockConsumer != EMPTY_BLOCK && !fallThrough) {
                            caseBlock.breakScope(StringSwitchImpl.this);
                        }
                        caseBlock.findActiveResultHandles(inputHandles);
                        caseBlocks.put(caseEntry.getKey(), caseBlock);
                        BytecodeCreatorImpl isEqual = (BytecodeCreatorImpl) lookupBlock
                                .ifTrue(Gizmo.equals(lookupBlock, lookupBlock.load(caseEntry.getKey()), value)).trueBranch();
                        isEqual.jumpTo(caseBlock);
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
                addCaseBlock(s, EMPTY_BLOCK);
            } else {
                addCaseBlock(s, caseBlockConsumer);
            }
        }
    }

    private void addCaseBlock(String value, Consumer<BytecodeCreator> blockConsumer) {
        int hashCode = value.hashCode();
        List<Entry<String, Consumer<BytecodeCreator>>> caseBlocks = hashToCaseBlocks.get(hashCode);
        if (caseBlocks == null) {
            caseBlocks = new ArrayList<>();
            hashToCaseBlocks.put(hashCode, caseBlocks);
        } else {
            for (Entry<String, Consumer<BytecodeCreator>> e : caseBlocks) {
                if (e.getKey().equals(value)) {
                    throw new IllegalArgumentException("A case block for the string value " + value + " already exists");
                }
            }
        }
        caseBlocks.add(Map.entry(value, blockConsumer));
    }

}
