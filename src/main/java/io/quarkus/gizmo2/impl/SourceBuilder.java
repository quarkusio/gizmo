package io.quarkus.gizmo2.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Accumulates pseudo-Java source text for a single class file.
 * <p>
 * Source lines are appended during the builder callback (fields and method bodies),
 * using body-relative line numbers starting from 1.
 * When the class is finalized, the class header is prepended and all tracked item
 * line numbers are adjusted by the header line count.
 */
public final class SourceBuilder {
    private final StringBuilder body = new StringBuilder(4096);
    private int bodyLineNumber = 1;
    private int indent = 1;
    private final List<Item> trackedItems = new ArrayList<>();
    private final ImportTracker importTracker = new ImportTracker();
    private static final String INDENT_UNIT = "    ";

    // label infrastructure for jump disambiguation
    private int labelCounter;
    private final IdentityHashMap<Object, String> labels = new IdentityHashMap<>();
    private final List<LabelSlot> labelSlots = new ArrayList<>();
    private final Deque<BlockCreatorImpl> blockStack = new ArrayDeque<>();
    private final Deque<SwitchCreatorImpl<?>> switchStack = new ArrayDeque<>();

    /**
     * A deferred label insertion point.
     *
     * @param target the jump target (block or switch) that may need a label
     * @param insertPosition the position in the body StringBuilder where the label should be inserted
     */
    record LabelSlot(Object target, int insertPosition) {
    }

    /**
     * {@return the import tracker for this source builder}
     */
    ImportTracker importTracker() {
        return importTracker;
    }

    /**
     * {@return the body {@link StringBuilder} for direct appending between
     * {@link #startLine()} and {@link #endLine()} calls}
     */
    StringBuilder body() {
        return body;
    }

    /**
     * {@return the line number that the next {@link #line(String)} call will produce}
     */
    int nextLine() {
        return bodyLineNumber;
    }

    /**
     * Begins a new source line at the current indentation level.
     * The caller should append text to {@link #body()} and then call {@link #endLine()}.
     *
     * @return the body-relative line number of the started line
     */
    int startLine() {
        int ln = bodyLineNumber++;
        body.append(INDENT_UNIT.repeat(indent));
        return ln;
    }

    /**
     * Ends a line previously started with {@link #startLine()} by appending a newline.
     */
    void endLine() {
        body.append('\n');
    }

    /**
     * Appends a line of source text at the current indentation level.
     *
     * @param text the source text for the line (must not be {@code null})
     * @return the body-relative line number of the appended line
     */
    int line(String text) {
        int ln = bodyLineNumber++;
        body.append(INDENT_UNIT.repeat(indent)).append(text).append('\n');
        return ln;
    }

    /**
     * Appends a blank separator line.
     */
    void blankLine() {
        bodyLineNumber++;
        body.append('\n');
    }

    /**
     * Increases the indentation level for subsequent lines.
     */
    void indent() {
        indent++;
    }

    /**
     * Decreases the indentation level for subsequent lines.
     */
    void dedent() {
        indent--;
    }

    /**
     * Records an item whose {@code sourceLine} needs offset adjustment during finalization.
     *
     * @param item the item to track (must not be {@code null})
     */
    void trackItem(Item item) {
        trackedItems.add(item);
    }

    /**
     * Assigns a label to the given target if one has not already been assigned.
     *
     * @param target the jump target (block or switch) to label
     * @return the label string (e.g. {@code "b0"}, {@code "b1"})
     */
    String ensureLabel(Object target) {
        return labels.computeIfAbsent(target, k -> "b" + labelCounter++);
    }

    /**
     * {@return the label for the given target, or {@code null} if none has been assigned}
     *
     * @param target the jump target to look up
     */
    String labelFor(Object target) {
        return labels.get(target);
    }

    /**
     * Records a deferred label insertion point at the current body position.
     * If a label is later assigned to the target via {@link #ensureLabel(Object)},
     * it will be inserted at this position when {@link #processLabelSlots()} is called.
     *
     * @param target the jump target that may need a label
     */
    void addLabelSlot(Object target) {
        labelSlots.add(new LabelSlot(target, body.length()));
    }

    /**
     * Processes all deferred label slots, inserting label prefixes for targets
     * that actually received labels. Slots are processed in reverse position order
     * to keep earlier positions valid during insertion.
     */
    void processLabelSlots() {
        // process in reverse to preserve insertion positions
        for (int i = labelSlots.size() - 1; i >= 0; i--) {
            LabelSlot slot = labelSlots.get(i);
            String label = labels.get(slot.target());
            if (label != null) {
                body.insert(slot.insertPosition(), label + ": ");
            }
        }
        labelSlots.clear();
    }

    /**
     * Pushes a block onto the enclosing-block stack.
     *
     * @param block the block being rendered
     */
    void pushBlock(BlockCreatorImpl block) {
        blockStack.push(block);
    }

    /**
     * Pops the most recently pushed block from the enclosing-block stack.
     */
    void popBlock() {
        blockStack.pop();
    }

    /**
     * {@return the currently enclosing block, or {@code null} if no block is being rendered}
     */
    BlockCreatorImpl currentBlock() {
        return blockStack.peek();
    }

    /**
     * Pushes a switch onto the enclosing-switch stack.
     *
     * @param sw the switch being rendered
     */
    void pushSwitch(SwitchCreatorImpl<?> sw) {
        switchStack.push(sw);
    }

    /**
     * Pops the most recently pushed switch from the enclosing-switch stack.
     */
    void popSwitch() {
        switchStack.pop();
    }

    /**
     * {@return the currently enclosing switch, or {@code null} if no switch is being rendered}
     */
    SwitchCreatorImpl<?> currentSwitch() {
        return switchStack.peek();
    }

    /**
     * Resets all label-related state for a new method body.
     */
    void resetLabels() {
        labelCounter = 0;
        labels.clear();
        labelSlots.clear();
        blockStack.clear();
        switchStack.clear();
    }

    /**
     * Finalizes the source text by prepending the class header and adjusting all tracked
     * item line numbers by the header offset.
     *
     * @param packageName the package name, or {@code null} for the default package
     * @param classHeader the class or interface declaration (e.g., {@code "class Foo extends Bar"})
     * @return the complete source text as a string
     */
    String finalize(String packageName, StringBuilder classHeader) {
        StringBuilder header = new StringBuilder();
        if (packageName != null && !packageName.isEmpty()) {
            header.append("package ").append(packageName).append(";\n");
            header.append('\n');
        }
        // insert import statements if any types were tracked
        int sizeBefore = header.length();
        importTracker.resolveImports(header, packageName);
        if (header.length() > sizeBefore) {
            header.append('\n');
        }
        header.append(classHeader).append(" {\n");
        header.append('\n');

        // count actual newlines in the header
        int headerLines = 0;
        for (int i = 0; i < header.length(); i++) {
            if (header.charAt(i) == '\n') {
                headerLines++;
            }
        }

        // adjust all tracked item line numbers
        for (Item item : trackedItems) {
            item.sourceLine += headerLines;
        }

        header.append(body);
        header.append("}\n");

        return header.toString();
    }
}
