/**
 * A simplified bytecode generator.
 */
module io.quarkus.gizmo2 {
    requires io.github.dmlloyd.classfile;
    requires io.smallrye.common.constraint;
    requires io.smallrye.common.resource;

    // for accessing serializable lambdas via ReflectionFactory
    requires jdk.unsupported;

    exports io.quarkus.gizmo2;
    exports io.quarkus.gizmo2.creator;
    exports io.quarkus.gizmo2.creator.ops;
    exports io.quarkus.gizmo2.desc;
}