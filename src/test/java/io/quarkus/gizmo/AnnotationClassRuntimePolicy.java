package io.quarkus.gizmo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface AnnotationClassRuntimePolicy {

    String value();

}
