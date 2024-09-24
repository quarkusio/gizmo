package io.quarkus.gizmo2;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.impl.GizmoImpl;

/**
 * A simplified class file writer.
 */
public sealed interface Gizmo extends ClassOutput permits GizmoImpl {
    /**
     * {@return the current instance}
     * The current instance corresponds to the instance that is currently being used to create a class.
     * @throws IllegalStateException if there is no current instance
     */
    static Gizmo current() {
        return GizmoImpl.current();
    }

    /**
     * {@return a new Gizmo which outputs to the given handler by default}
     * @param outputHandler the output handler (must not be {@code null})
     */
    static Gizmo create(BiConsumer<ClassDesc, byte[]> outputHandler) {
        return new GizmoImpl(outputHandler);
    }

    /**
     * {@return a class output using the given handler}
     * @param outputHandler the output handler (must not be {@code null})
     */
    ClassOutput classOutput(BiConsumer<ClassDesc, byte[]> outputHandler);

    /**
     * {@return a class file writer for the given path}
     * @param basePath the path into which class files should be stored (must not be {@code null})
     */
    static BiConsumer<ClassDesc, byte[]> fileWriter(Path basePath) {
        if (! Files.isDirectory(basePath)) {
            throw new IllegalArgumentException("Path does not exist or is not an accessible directory: " + basePath);
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
                    throw new IllegalArgumentException("Failed to write class", e);
                }
            } else {
                throw new IllegalStateException("Invalid class " + classDesc);
            }
        };
    }
}
