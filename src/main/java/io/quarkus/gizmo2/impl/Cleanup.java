package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;

public interface Cleanup {
    void writeCleanup(CodeBuilder cb, BlockCreatorImpl block);
}
