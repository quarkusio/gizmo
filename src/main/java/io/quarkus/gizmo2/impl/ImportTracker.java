package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tracks type references during source generation and resolves import statements.
 * <p>
 * Uses a first-encountered-wins strategy for simple name conflicts: the first type
 * to use a given simple name claims it, and all subsequent types with the same
 * simple name are rendered fully-qualified.
 * <p>
 * Types in {@code java.lang} (exact package, not subpackages) and in the same
 * package as the generated class are simplified without generating import statements.
 */
final class ImportTracker {

    /**
     * Maps simple name to the descriptor string of the type that claimed it.
     */
    private final Map<String, String> claimedNames = new LinkedHashMap<>();

    /**
     * FQNs of all successfully claimed types (used for import generation).
     */
    private final Set<String> importedFqns = new LinkedHashSet<>();

    /**
     * Resolves the name to use for the given type in generated source.
     * <p>
     * If the type's simple name has not been claimed, this method claims it and
     * returns the simple name. If the same type already claimed the name, returns
     * the simple name. If a different type claimed the name, returns the FQN
     * (conflict — the type cannot be imported).
     *
     * @param desc the class descriptor (must not be {@code null}, must not be primitive or array)
     * @param fqn the fully-qualified name of the type as produced by {@code typeName(desc)}
     * @return the simple name if claimed, or the FQN if there is a conflict
     */
    String resolveName(ClassDesc desc, String fqn) {
        String simpleName = extractSimpleName(fqn);
        String descStr = desc.descriptorString();
        String claimedDesc = claimedNames.get(simpleName);
        if (claimedDesc == null) {
            claimedNames.put(simpleName, descStr);
            importedFqns.add(fqn);
            return simpleName;
        } else if (claimedDesc.equals(descStr)) {
            return simpleName;
        } else {
            return fqn;
        }
    }

    /**
     * Appends import statements for the claimed types to the given string builder.
     * <p>
     * Types in {@code java.lang} (exact package, not subpackages) and types in the
     * same package as the generated class do not generate import statements, but their
     * simple names are still used in the source.
     *
     * @param b the string builder to append import statements to (must not be {@code null})
     * @param packageName the package of the generated class, or {@code null} for the default package
     * @return the string builder, for chaining
     */
    StringBuilder resolveImports(StringBuilder b, String packageName) {
        TreeSet<String> imports = new TreeSet<>();
        for (String fqn : importedFqns) {
            String pkg = extractPackage(fqn);
            if (pkg != null && !pkg.equals("java.lang") && !pkg.equals(packageName)) {
                imports.add(fqn);
            }
        }
        for (String imp : imports) {
            b.append("import ").append(imp).append(";\n");
        }
        return b;
    }

    /**
     * {@return the simple name portion of a fully-qualified type name}
     * For inner classes like {@code Outer$Inner}, returns {@code Outer$Inner}.
     *
     * @param fqn the fully-qualified name (must not be {@code null})
     */
    private static String extractSimpleName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot >= 0 ? fqn.substring(lastDot + 1) : fqn;
    }

    /**
     * {@return the package portion of a fully-qualified type name, or {@code null} for the default package}
     *
     * @param fqn the fully-qualified name (must not be {@code null})
     */
    private static String extractPackage(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        return lastDot >= 0 ? fqn.substring(0, lastDot) : null;
    }
}
