package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.AnnotationElement;
import io.github.dmlloyd.classfile.AnnotationValue;
import io.quarkus.gizmo2.creator.AnnotationCreator;
import org.junit.jupiter.api.Test;

public class TestAnnotationCreator {

    @Test
    public void testBasic() {
        Annotation res = AnnotationCreator.makeAnnotation(Deprecated.class, m -> {});
        assertEquals(Deprecated.class.descriptorString(), res.classSymbol().descriptorString());
        assertTrue(res.elements().isEmpty());
    }

    @Test
    public void testProperties() {
        Annotation res = AnnotationCreator.makeAnnotation(Deprecated.class, m -> {
            m.with(Deprecated::since, "1234");
            m.with(Deprecated::forRemoval, true);
        });
        assertEquals(Deprecated.class.descriptorString(), res.classSymbol().descriptorString());
        assertEquals(AnnotationValue.ofString("1234"), res.elements().stream().filter(e -> e.name().equalsString("since")).map(AnnotationElement::value).findFirst().get());
        assertEquals(AnnotationValue.ofBoolean(true), res.elements().stream().filter(e -> e.name().equalsString("forRemoval")).map(AnnotationElement::value).findFirst().get());
    }

    @Test
    public void testMulti() {
        Annotation res = AnnotationCreator.makeAnnotation(Test1.class, m -> {
            m.with(Test1::strings, List.of("one", "two", "three"));
            m.with(Test1::ints, 12, 34, 56);
        });
        assertEquals(Test1.class.descriptorString(), res.classSymbol().descriptorString());
        assertEquals(AnnotationValue.ofArray(
            AnnotationValue.ofString("one"),
            AnnotationValue.ofString("two"),
            AnnotationValue.ofString("three")
        ), res.elements().stream().filter(e -> e.name().equalsString("strings")).map(AnnotationElement::value).findFirst().get());
        assertEquals(AnnotationValue.ofArray(
            AnnotationValue.ofInt(12),
            AnnotationValue.ofInt(34),
            AnnotationValue.ofInt(56)
        ), res.elements().stream().filter(e -> e.name().equalsString("ints")).map(AnnotationElement::value).findFirst().get());
    }

    public @interface Test1 {
        String[] strings();

        int[] ints();
    }
}
