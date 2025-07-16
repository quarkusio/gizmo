package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;

final class LineNumber extends Item {
    private final int lineNumber;

    LineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.lineNumber(lineNumber);
    }
}
