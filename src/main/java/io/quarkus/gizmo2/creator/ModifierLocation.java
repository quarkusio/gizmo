package io.quarkus.gizmo2.creator;

/**
 * The possible locations for a modifier or access level to do.
 */
public enum ModifierLocation {
    /**
     * A concrete (private or default) interface method.
     */
    INTERFACE_CONCRETE_METHOD,
    /**
     * A regular (abstract) interface method.
     */
    INTERFACE_ABSTRACT_METHOD,
    /**
     * A static field on an interface.
     */
    INTERFACE_STATIC_FIELD,
    /**
     * A static method on an interface.
     */
    INTERFACE_STATIC_METHOD,
    /**
     * A constructor of a class.
     */
    CLASS_CONSTRUCTOR,
    /**
     * A concrete instance method of a class.
     */
    CLASS_CONCRETE_METHOD,
    /**
     * An abstract method of a class.
     */
    CLASS_ABSTRACT_METHOD,
    /**
     * A native method of a class.
     */
    CLASS_NATIVE_METHOD,
    /**
     * A non-native static method of a class.
     */
    CLASS_STATIC_METHOD,
    /**
     * An instance field of a class.
     */
    CLASS_INSTANCE_FIELD,
    /**
     * A static field of a class.
     */
    CLASS_STATIC_FIELD,
    /**
     * A top-level class.
     */
    CLASS,
    /**
     * A top-level interface.
     */
    INTERFACE,
    /**
     * A nested class.
     */
    NESTED_CLASS,
    /**
     * A nested interface.
     */
    NESTED_INTERFACE,
    /**
     * An anonymous class.
     */
    ANONYMOUS_CLASS,
    /**
     * A parameter of a method or constructor.
     */
    PARAMETER,
    /**
     * A local variable.
     */
    LOCAL_VARIABLE,
    /**
     * No location.
     */
    // always last
    NONE,
    ;
}
