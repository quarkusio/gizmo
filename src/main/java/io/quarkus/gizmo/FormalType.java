package io.quarkus.gizmo;

public class FormalType {
    private final String name;
    private final String superClass;
    private final String[] interfaces;

    public FormalType(String name, String superClass, String[] interfaces) {
        this.name = name;
        this.superClass = DescriptorUtils.objectToDescriptor(superClass);
        this.interfaces = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; ++i) {
            this.interfaces[i] = DescriptorUtils.objectToDescriptor(interfaces[i]);
        }
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
