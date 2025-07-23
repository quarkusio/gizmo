package io.quarkus.gizmo2;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.nio.file.Files;
import java.nio.file.Path;

import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;
import io.smallrye.common.resource.ResourceUtils;

/**
 * A container for created classes with a specific output strategy.
 */
@FunctionalInterface
public interface ClassOutput {
    /**
     * Accept and write the class bytes.
     *
     * @param desc the class descriptor (not {@code null})
     * @param bytes the class file bytes (not {@code null})
     */
    default void write(ClassDesc desc, byte[] bytes) {
        if (!desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Can only write classes/interfaces");
        }
        write(Util.internalName(desc) + ".class", bytes);
    }

    /**
     * Write a resource to the output.
     *
     * @param path the resource relative path (not {@code null})
     * @param bytes the resource bytes (not {@code null})
     */
    void write(String path, byte[] bytes);

    /**
     * {@return a class output that write the class bytes to this output as well as the given one}
     *
     * @param next the other class output to write to (must not be {@code null})
     */
    default ClassOutput andThen(ClassOutput next) {
        Assert.checkNotNullParam("next", next);
        return new ClassOutput() {
            public void write(final ClassDesc desc, final byte[] bytes) {
                ClassOutput.this.write(desc, bytes);
                next.write(desc, bytes);
            }

            public void write(final String path, final byte[] bytes) {
                ClassOutput.this.write(path, bytes);
                next.write(path, bytes);
            }
        };
    }

    /**
     * {@return a class output for the given path}
     *
     * @param basePath the path into which class files should be stored (must not be {@code null})
     */
    static ClassOutput fileWriter(Path basePath) {
        Assert.checkNotNullParam("basePath", basePath);
        return (name, bytes) -> {
            try {
                Path path = basePath.resolve(ResourceUtils.canonicalizeRelativePath(name));
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to write class %s".formatted(name), e);
            }
        };
    }
}
