package io.quarkus.gizmo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyFullAnnotation {
    boolean bool();
    char ch();
    byte b();
    short s();
    int i();
    long l();
    float f();
    double d();
    String str();
    MyEnum enumerated();
    Class<?> cls();
    MyNestedAnnotation nested();

    boolean[] boolArray();
    char[] chArray();
    byte[] bArray();
    short[] sArray();
    int[] iArray();
    long[] lArray();
    float[] fArray();
    double[] dArray();
    String[] strArray();
    MyEnum[] enumeratedArray();
    Class<?>[] clsArray();
    MyNestedAnnotation[] nestedArray();
}
