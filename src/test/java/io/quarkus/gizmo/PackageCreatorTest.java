package io.quarkus.gizmo;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.junit.Assert;
import org.junit.Test;

public class PackageCreatorTest {
    @Test
    public void testPackageAnnotationWithString() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (PackageCreator creator = PackageCreator.builder().classOutput(cl).packageName("com.myTest").build()) {
            creator.addAnnotation(AnnotationInstance.create(DotName.createSimple(MyAnnotation.class.getName()), null, new AnnotationValue[] {
                AnnotationValue.createEnumValue("enumVal", DotName.createSimple("io.quarkus.gizmo.MyEnum"), "NO")
            } ));
        }
        //supported only with java 9
        /*
        MyAnnotation annotation = cl.getDefinedPackage("com.MyTest")
                .getAnnotation(MyAnnotation.class);
        Assert.assertEquals(MyEnum.NO, annotation.enumVal());

         */
    }
}
