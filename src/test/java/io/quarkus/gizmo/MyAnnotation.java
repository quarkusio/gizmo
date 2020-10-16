package io.quarkus.gizmo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAnnotation {

    String value();

    MyEnum enumVal();
}
