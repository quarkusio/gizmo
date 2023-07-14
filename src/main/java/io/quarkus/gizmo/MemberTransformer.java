package io.quarkus.gizmo;

/**
 * Describes transformation of a class member.
 *
 * @see ClassTransformer
 */
public abstract class MemberTransformer<THIS extends MemberTransformer<THIS>> {

    int addedModifiers = 0;
    int removedModifiers = 0;
    String newName;

    @SuppressWarnings("unchecked")
    final THIS self() {
        return (THIS) this;
    }

    /**
     * Adds given {@code modifiers} to the modifiers of this method or field. It is a responsibility
     * of the caller to make sure the resulting set of modifiers is valid.
     *
     * @param modifiers the modifiers to add to this method or field
     * @return this, to allow fluent usage
     */
    public THIS addModifiers(int modifiers) {
        addedModifiers |= modifiers;
        return self();
    }

    /**
     * Removes given {@code modifiers} from the modifiers of this method or field. It is a responsibility
     * of the caller to make sure the resulting set of modifiers is valid.
     *
     * @param modifiers the modifiers to remove from this method or field
     * @return this, to allow fluent usage
     */
    public THIS removeModifiers(int modifiers) {
        removedModifiers |= modifiers;
        return self();
    }

    /**
     * Renames this method or field to given {@code name}. Other properties of the method or field,
     * such as the signature or modifiers, are left intact. If no matching method or field exists
     * in the class, this is a noop.
     *
     * @param name new name of the method or field
     * @return this, to allow fluent usage
     */
    public THIS rename(String name) {
        newName = name;
        return self();
    }

}
