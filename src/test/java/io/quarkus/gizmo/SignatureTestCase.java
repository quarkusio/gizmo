package io.quarkus.gizmo;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class SignatureTestCase {

    @Test
    public void testSimpleClassSignatureUtils() throws Exception {
        SignatureUtils.ClassSignature clsSignature = new SignatureUtils.ClassSignature();
        clsSignature.superClass("Ljava/lang/Object;");
        clsSignature.interfaces(Collections.singletonList("Lio/quarkus/gizmo/MyInterface;"));
        clsSignature.formalType("K", "Ljava/lang/Object;", "Lio/quarkus/gizmo/MyInterface;");
        Assert.assertEquals("<K:Ljava/lang/Object;:Lio/quarkus/gizmo/MyInterface;>Ljava/lang/Object;Lio/quarkus/gizmo/MyInterface;", clsSignature.generate());
    }

    @Test
    public void testSimpleMethodSignatureUtils() throws Exception {
        //public <K> String transform(K thing) { return K.toString());
        // signature must be <K:Ljava/lang/Object;>(TK;)Ljava/lang/String;"
        SignatureUtils.MethodSignature metSignature = new SignatureUtils.MethodSignature();
        metSignature.returnType("Ljava/lang/String;");
        metSignature.formalType("K");
        metSignature.paramTypes("K");
        Assert.assertEquals("<K:Ljava/lang/Object;>(TK;)Ljava/lang/String;", metSignature.generate());
    }

    @Test
    public void testMethodSignatureUtilsWithException() throws Exception {
        //public <K> String transform(K thing) { return K.toString());
        // signature must be <K:Ljava/lang/Object;>(TK;)Ljava/lang/String;"
        SignatureUtils.MethodSignature metSignature = new SignatureUtils.MethodSignature();
        metSignature.returnType("Ljava/lang/String;");
        metSignature.exceptionTypes(Collections.singletonList("Ljava/lang/Exception;"));
        Assert.assertEquals("()Ljava/lang/String;^Ljava/lang/Exception;", metSignature.generate());
    }

    @Test
    public void testSimpleClassSignature() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").formalType("K").interfaces(MyInterface.class).build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle message = method.invokeStaticMethod(MethodDescriptor.ofMethod(MessageClass.class.getName(), "getMessage", "Ljava/lang/String;"));
            method.returnValue(message);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Assert.assertTrue(clazz.isSynthetic());
        MyInterface myInterface = (MyInterface) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals("MESSAGE", myInterface.transform("ignored"));
    }
    @Test
    public void testSimpleMethodSignature() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        //public <K> String transform(K thing) { return K.toString());
        // signature must be <K:Ljava/lang/Object;>(TK;)Ljava/lang/String;"
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").formalType("K").build()) {
            MethodCreator method = creator.getMethodCreator("transform", "java.lang.String", "K").formalType("K");
            ResultHandle message = method.invokeStaticMethod(MethodDescriptor.ofMethod(Object.class.getName(), "toString", "Ljava/lang/String;"));
            method.returnValue(message);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Assert.assertTrue(clazz.isSynthetic());
    }
}
