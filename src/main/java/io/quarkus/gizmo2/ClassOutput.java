package io.quarkus.gizmo2;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.nio.file.Files;
import java.nio.file.Path;

import io.smallrye.common.constraint.Assert;

/**
 * A container for created classes with a specific output strategy.
 */
public interface ClassOutput {
    /**
     * Accept and write the class bytes.
     *
     * @param desc the class descriptor (not {@code null})
     * @param bytes the class file bytes (not {@code null})
     */
    void write(ClassDesc desc, byte[] bytes);

    /**
     * {@return a class output that write the class bytes to this output as well as the given one}
     *
     * @param next the other class output to write to (must not be {@code null})
     */
    default ClassOutput andThen(ClassOutput next) {
        Assert.checkNotNullParam("next", next);
        return (desc, bytes) -> {
            write(desc, bytes);
            next.write(desc, bytes);
        };
    }

    /**
     * {@return a class output for the given path}
     *
     * @param basePath the path into which class files should be stored (must not be {@code null})
     */
    static ClassOutput fileWriter(Path basePath) {
        Assert.checkNotNullParam("basePath", basePath);
        if (!Files.isDirectory(basePath)) {
            throw new IllegalArgumentException("Path does not exist or is not an accessible directory: %s".formatted(basePath));
        }
        return (classDesc, bytes) -> {
            if (classDesc.isClassOrInterface()) {
                String ds = classDesc.descriptorString();
                String pathName = ds.substring(1, ds.length() - 1) + ".class";
                Path path = basePath;
                int idx = pathName.indexOf('/');
                if (idx == -1) {
                    path = path.resolve(pathName);
                } else {
                    path = path.resolve(pathName.substring(0, idx));
                    int start;
                    for (;;) {
                        start = idx + 1;
                        idx = pathName.indexOf('/', start);
                        if (idx == -1) {
                            path = path.resolve(pathName.substring(start));
                            break;
                        } else {
                            path = path.resolve(pathName.substring(start, idx));
                        }
                    }
                }
                try {
                    Files.createDirectories(path.getParent());
                    Files.write(path, bytes);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to write class %s".formatted(classDesc), e);
                }
            } else {
                throw new IllegalStateException("Invalid class %s".formatted(classDesc));
            }
        };
    }
}
