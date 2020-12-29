package io.quarkus.gizmo;

public class FormalType {
    private final String name;
    private final String superClass;
    private final String[] interfaces;

    public FormalType(String name, String superClass, String[] interfaces) {
        this.name = name;
        this.superClass = superClass;
        this.interfaces = interfaces;
    }

    public String getName() {
        return name;
    }

    public String getSuperClass() {
        return superClass;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    @Override
    public String toString() {
        return name + " extends " + superClass + (interfaces.length > 0 ?
                " implements " + String.join(",", interfaces)
                : "");
    }
}
