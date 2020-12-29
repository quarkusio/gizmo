package io.quarkus.gizmo;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.signature.SignatureWriter;

public class SignatureTestCase {

    @Test
    public void testSimpleClassSignatureUtils() throws Exception {
        SignatureUtils.ClassSignature clsSignature = new SignatureUtils.ClassSignature();
        clsSignature.superClass("java.lang.Object");
        clsSignature.interfaces(Collections.singletonList("io.quarkus.gizmo.MyInterface"));
        clsSignature.formalType("K", "java.lang.Object", "io.quarkus.gizmo.MyInterface");
        Assert.assertEquals("<K:Ljava/lang/Object;:Lio/quarkus/gizmo/MyInterface;>Ljava/lang/Object;Lio/quarkus/gizmo/MyInterface;", clsSignature.generate());
    }
    @Test
    public void testVisitTypeString() throws Exception {
        SignatureWriter writer = new SignatureWriter();
        SignatureUtils.visitType(writer, "java.lang.String", 0);
        Assert.assertEquals("Ljava/lang/String;", writer.toString());
    }
    @Test
    public void testVisitTypePrimitive() throws Exception {
        SignatureWriter writer = new SignatureWriter();
        SignatureUtils.visitType(writer, "float", 0);
        Assert.assertEquals("F", writer.toString());
    }

    @Test
    public void testVisitTypeArray() throws Exception {
        SignatureWriter writer = new SignatureWriter();
        SignatureUtils.visitType(writer, "int[]", 0);
        Assert.assertEquals("[I", writer.toString());
    }
    @Test
    public void testVisitTypeList() throws Exception {
        SignatureWriter writer = new SignatureWriter();
        SignatureUtils.visitType(writer, "java.util.List<java.lang.String>", 0);
        Assert.assertEquals("Ljava/util/List<Ljava/lang/String;>;", writer.toString());
    }
    @Test
    public void testVisitTypeMap() throws Exception {
        SignatureWriter writer = new SignatureWriter();
        SignatureUtils.visitType(writer, "java.util.Map<java.lang.String, int>", 0);
        Assert.assertEquals("Ljava/util/Map<Ljava/lang/String;I>;", writer.toString());
    }
    @Test
    public void testSuperGenericClassSignatureUtils() throws Exception {
        SignatureUtils.ClassSignature clsSignature = new SignatureUtils.ClassSignature();
        clsSignature.superClass("java.util.List<java.lang.String>");
        clsSignature.interfaces(Collections.singletonList("io.quarkus.gizmo.MyInterface"));
        clsSignature.formalType("K", "java.lang.Object", "io.quarkus.gizmo.MyInterface");
        Assert.assertEquals("<K:Ljava/lang/Object;:Lio/quarkus/gizmo/MyInterface;>Ljava/util/List<Ljava/lang/String;>;Lio/quarkus/gizmo/MyInterface;", clsSignature.generate());
    }

    @Test
    public void testSimpleMethodSignatureUtils() throws Exception {
        //public <K> String transform(K thing) { return K.toString());
        // signature must be <K:Ljava/lang/Object;>(TK;)Ljava/lang/String;"
        SignatureUtils.MethodSignature metSignature = new SignatureUtils.MethodSignature();
        metSignature.returnType("java.lang.String");
        metSignature.formalType("K");
        metSignature.paramTypes("K");
        Assert.assertEquals("<K:Ljava/lang/Object;>(TK;)Ljava/lang/String;", metSignature.generate());
    }

    @Test
    public void testMethodSignatureUtilsWithException() throws Exception {
        //public <K> String transform(K thing) { return K.toString());
        // signature must be <K:Ljava/lang/Object;>(TK;)Ljava/lang/String;"
        SignatureUtils.MethodSignature metSignature = new SignatureUtils.MethodSignature();
        metSignature.returnType("java.lang.String");
        metSignature.exceptionTypes(Collections.singletonList("java.lang.Exception"));
        Assert.assertEquals("()Ljava/lang/String;^Ljava/lang/Exception;", metSignature.generate());
    }
}
