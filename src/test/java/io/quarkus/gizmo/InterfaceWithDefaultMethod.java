package io.quarkus.gizmo;

public interface InterfaceWithDefaultMethod {
    default int whatever() {
        return 13;
    }
}
