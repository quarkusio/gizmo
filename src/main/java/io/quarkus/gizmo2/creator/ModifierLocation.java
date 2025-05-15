package io.quarkus.gizmo2.creator;

/**
 * The possible locations for a modifier or access level to do.
 */
public enum ModifierLocation {
    INTERFACE_CONCRETE_METHOD,
    INTERFACE_ABSTRACT_METHOD,
    INTERFACE_STATIC_FIELD,
    INTERFACE_STATIC_METHOD,
    CLASS_CONSTRUCTOR,
    CLASS_CONCRETE_METHOD,
    CLASS_ABSTRACT_METHOD,
    CLASS_NATIVE_METHOD,
    CLASS_STATIC_METHOD,
    CLASS_INSTANCE_FIELD,
    CLASS_STATIC_FIELD,
    CLASS,
    INTERFACE,
    NESTED_CLASS,
    NESTED_INTERFACE,
    ANONYMOUS_CLASS,
    PARAMETER,
    LOCAL_VARIABLE,
    // always last
    NONE,
    ;
}
