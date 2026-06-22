package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.CD_String;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;
import io.smallrye.classfile.Attributes;
import io.smallrye.classfile.ClassFile;
import io.smallrye.classfile.ClassModel;
import io.smallrye.classfile.attribute.InnerClassInfo;
import io.smallrye.classfile.attribute.InnerClassesAttribute;
import io.smallrye.classfile.attribute.NestHostAttribute;
import io.smallrye.classfile.attribute.NestMembersAttribute;
import io.smallrye.classfile.constantpool.ClassEntry;
import io.smallrye.classfile.constantpool.Utf8Entry;

/**
 * Tests for named member class and interface generation.
 */
public class MemberClassTest {

    /**
     * Functional interface for two-parameter static methods returning Object.
     */
    public interface TwoParams {
        Object apply(Object p0, Object p1);
    }

    /**
     * Test a static member class: create it, invoke a method on it.
     */
    @Test
    public void staticMemberClass() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc outerDesc = g.class_("io.quarkus.gizmo2.Outer", cc -> {
            ClassDesc innerDesc = cc.class_("Inner", ic -> {
                ic.addFlag(ModifierFlag.STATIC);
                ic.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.return_();
                    });
                });
                ic.method("greet", mc -> {
                    mc.returning(Object.class);
                    mc.body(b0 -> {
                        b0.return_(Const.of("hello from static member"));
                    });
                });
            });
            // factory method on the outer class to create and use the inner
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(b0 -> {
                    Expr inner = b0.new_(ConstructorDesc.of(innerDesc));
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(innerDesc, "greet", Object.class), inner));
                });
            });
        });
        assertEquals("hello from static member", tcm.staticMethod(outerDesc, "test", Supplier.class).get());
    }

    /**
     * Test a non-static inner class: verify the synthetic this$0 field and outer instance access.
     */
    @Test
    public void innerClass() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        // pre-compute descriptors so they can be captured in lambdas
        ClassDesc outerDesc = ClassDesc.of("io.quarkus.gizmo2.OuterForInner");
        ClassDesc innerDesc = ClassDesc.of("io.quarkus.gizmo2.OuterForInner$Inner");
        g.class_("io.quarkus.gizmo2.OuterForInner", cc -> {
            // an instance field on the outer class
            FieldDesc outerField = cc.field("value", ifc -> {
                ifc.setType(String.class);
                ifc.setAccess(AccessLevel.PRIVATE);
            });
            cc.class_("Inner", ic -> {
                // no STATIC flag → inner class
                ic.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.return_();
                    });
                });
                ic.method("getOuterValue", mc -> {
                    mc.returning(Object.class);
                    mc.body(b0 -> {
                        // access outer's value via this$0
                        FieldDesc this0 = FieldDesc.of(innerDesc, "this$0", outerDesc);
                        Expr outerRef = ic.this_().field(this0);
                        b0.return_(outerRef.field(outerField));
                    });
                });
            });
            // outer constructor: sets value
            cc.constructor(ctc -> {
                ParamVar val = ctc.parameter("val", String.class);
                ctc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    b0.set(cc.this_().field(outerField), val);
                    b0.return_();
                });
            });
            cc.method("test", mc -> {
                mc.returning(Object.class);
                mc.body(b0 -> {
                    // new Inner(this) — the synthetic outer param is the first constructor arg
                    Expr inner = b0.new_(ConstructorDesc.of(innerDesc, outerDesc), cc.this_());
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(innerDesc, "getOuterValue", Object.class), inner));
                });
            });
            // static entry point
            cc.staticMethod("run", mc -> {
                mc.returning(Object.class);
                ParamVar val = mc.parameter("val", Object.class);
                mc.body(b0 -> {
                    Expr outer = b0.new_(ConstructorDesc.of(outerDesc, String.class), val);
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(outerDesc, "test", Object.class), outer));
                });
            });
        });
        assertEquals("hello inner", tcm.staticMethod(outerDesc, "run", Function.class).apply("hello inner"));
    }

    /**
     * Test member interface: create it, implement it from the outer class.
     */
    @Test
    public void memberInterface() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        // pre-compute so it can be captured in the lambda
        ClassDesc outerDesc = ClassDesc.of("io.quarkus.gizmo2.OuterWithIface");
        g.class_("io.quarkus.gizmo2.OuterWithIface", cc -> {
            ClassDesc ifaceDesc = cc.interface_("Greeter", ic -> {
                ic.method("greet", mc -> {
                    mc.returning(Object.class);
                });
            });
            // outer implements the member interface
            cc.implements_(ifaceDesc);
            cc.constructor(ctc -> {
                ctc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    b0.return_();
                });
            });
            cc.method("greet", mc -> {
                mc.returning(Object.class);
                mc.body(b0 -> {
                    b0.return_(Const.of("hello from member interface impl"));
                });
            });
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(b0 -> {
                    Expr outer = b0.new_(ConstructorDesc.of(outerDesc));
                    // dispatch through the member interface type
                    b0.return_(b0.invokeInterface(
                            InterfaceMethodDesc.of(ifaceDesc, "greet", Object.class), outer));
                });
            });
        });
        assertEquals("hello from member interface impl", tcm.staticMethod(outerDesc, "test", Supplier.class).get());
    }

    /**
     * Test that inner class with multiple constructors: each gets the synthetic outer parameter.
     */
    @Test
    public void innerClassMultipleConstructors() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        // pre-compute descriptors so they can be captured in lambdas
        ClassDesc outerDesc = ClassDesc.of("io.quarkus.gizmo2.OuterMultiCtor");
        ClassDesc innerDesc = ClassDesc.of("io.quarkus.gizmo2.OuterMultiCtor$Inner");
        g.class_("io.quarkus.gizmo2.OuterMultiCtor", cc -> {
            FieldDesc outerField = cc.field("name", ifc -> {
                ifc.setType(String.class);
            });
            cc.class_("Inner", ic -> {
                FieldDesc innerField = ic.field("label", ifc -> {
                    ifc.setType(String.class);
                });
                // first constructor: default label
                ic.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.set(ic.this_().field(innerField), Const.of("default"));
                        b0.return_();
                    });
                });
                // second constructor: custom label
                ic.constructor(ctc -> {
                    ParamVar label = ctc.parameter("label", String.class);
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.set(ic.this_().field(innerField), label);
                        b0.return_();
                    });
                });
                ic.method("describe", mc -> {
                    mc.returning(Object.class);
                    mc.body(b0 -> {
                        FieldDesc this0 = FieldDesc.of(innerDesc, "this$0", outerDesc);
                        Expr outerRef = ic.this_().field(this0);
                        Expr outerName = outerRef.field(outerField);
                        Expr label = ic.this_().field(innerField);
                        Expr part1 = b0.withString(outerName).concat(Const.of(":"));
                        b0.return_(b0.withString(part1).concat(label));
                    });
                });
            });
            cc.constructor(ctc -> {
                ParamVar name = ctc.parameter("name", String.class);
                ctc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    b0.set(cc.this_().field(outerField), name);
                    b0.return_();
                });
            });
            cc.staticMethod("testDefault", mc -> {
                mc.returning(Object.class);
                ParamVar name = mc.parameter("name", Object.class);
                mc.body(b0 -> {
                    Expr outer = b0.new_(ConstructorDesc.of(outerDesc, String.class), name);
                    // first ctor: (OuterMultiCtor) — outer param only
                    Expr inner = b0.new_(ConstructorDesc.of(innerDesc, outerDesc), outer);
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(innerDesc, "describe", Object.class), inner));
                });
            });
            cc.staticMethod("testCustom", mc -> {
                mc.returning(Object.class);
                ParamVar name = mc.parameter("name", Object.class);
                ParamVar label = mc.parameter("label", Object.class);
                mc.body(b0 -> {
                    Expr outer = b0.new_(ConstructorDesc.of(outerDesc, String.class), name);
                    // second ctor: (OuterMultiCtor, String) — outer param + label
                    Expr inner = b0.new_(ConstructorDesc.of(innerDesc, outerDesc, CD_String), outer, label);
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(innerDesc, "describe", Object.class), inner));
                });
            });
        });
        assertEquals("Alice:default",
                tcm.staticMethod(outerDesc, "testDefault", Function.class).apply("Alice"));
        assertEquals("Bob:custom",
                tcm.staticMethod(outerDesc, "testCustom", TwoParams.class).apply("Bob", "custom"));
    }

    /**
     * Verify NestHost, NestMembers, and InnerClasses attributes on a static member class.
     */
    @Test
    public void staticMemberClassAttributes() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc outerDesc = g.class_("io.quarkus.gizmo2.AttrOuter", cc -> {
            cc.class_("StaticInner", ic -> {
                ic.addFlag(ModifierFlag.STATIC);
                ic.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.return_();
                    });
                });
            });
        });
        ClassDesc innerDesc = ClassDesc.of("io.quarkus.gizmo2.AttrOuter$StaticInner");
        ClassFile cf = ClassFile.of();

        // verify outer class attributes
        ClassModel outerModel = tcm.readClass(outerDesc, cf::parse);
        // NestMembers should list the inner class
        NestMembersAttribute nma = outerModel.findAttribute(Attributes.nestMembers()).orElse(null);
        assertNotNull(nma, "outer class must have NestMembersAttribute");
        assertTrue(nma.nestMembers().stream().anyMatch(e -> e.asSymbol().equals(innerDesc)),
                "NestMembers must include inner class");
        // InnerClasses attribute on outer
        InnerClassesAttribute ica = outerModel.findAttribute(Attributes.innerClasses()).orElse(null);
        assertNotNull(ica, "outer class must have InnerClassesAttribute");
        InnerClassInfo outerIci = ica.classes().stream()
                .filter(i -> i.innerClass().asSymbol().equals(innerDesc))
                .findFirst().orElse(null);
        assertNotNull(outerIci, "outer InnerClasses must reference inner class");
        assertEquals(Optional.of("StaticInner"), outerIci.innerName().map(Utf8Entry::stringValue));
        assertEquals(Optional.of(outerDesc), outerIci.outerClass().map(ClassEntry::asSymbol));

        // verify inner class attributes
        ClassModel innerModel = tcm.readClass(innerDesc, cf::parse);
        // NestHost should point to outer
        NestHostAttribute nha = innerModel.findAttribute(Attributes.nestHost()).orElse(null);
        assertNotNull(nha, "inner class must have NestHostAttribute");
        assertEquals(outerDesc, nha.nestHost().asSymbol());
        // InnerClasses attribute on inner
        InnerClassesAttribute innerIca = innerModel.findAttribute(Attributes.innerClasses()).orElse(null);
        assertNotNull(innerIca, "inner class must have InnerClassesAttribute");
        InnerClassInfo innerIci = innerIca.classes().stream()
                .filter(i -> i.innerClass().asSymbol().equals(innerDesc))
                .findFirst().orElse(null);
        assertNotNull(innerIci, "inner InnerClasses must reference itself");
        // verify ACC_STATIC is in InnerClassInfo flags
        assertTrue((innerIci.flagsMask() & ClassFile.ACC_STATIC) != 0,
                "inner class InnerClassInfo flags must include ACC_STATIC");
    }

    /**
     * Verify that a non-static inner class has the synthetic this$0 field.
     */
    @Test
    public void innerClassThis0Field() throws Exception {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_("io.quarkus.gizmo2.This0Outer", cc -> {
            cc.class_("Inner", ic -> {
                ic.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.return_();
                    });
                });
            });
        });
        Class<?> innerClass = tcm.loadClass("io.quarkus.gizmo2.This0Outer$Inner");
        Field this0 = innerClass.getDeclaredField("this$0");
        assertNotNull(this0);
        assertTrue(Modifier.isFinal(this0.getModifiers()), "this$0 must be final");
        assertTrue(Modifier.isPrivate(this0.getModifiers()), "this$0 must be private");
        assertTrue(this0.isSynthetic(), "this$0 must be synthetic");
    }

    /**
     * Test static boundary reset: A > S (static) > D (inner) — D should have this$0 (not this$1).
     */
    @Test
    public void staticBoundaryReset() throws Exception {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_("io.quarkus.gizmo2.ResetOuter", cc -> {
            cc.class_("Static", sc -> {
                sc.addFlag(ModifierFlag.STATIC);
                sc.class_("Deep", dc -> {
                    // no STATIC → inner class. Should have this$0, not this$1
                    dc.constructor(ctc -> {
                        ctc.body(b0 -> {
                            b0.invokeSpecial(ConstructorDesc.of(Object.class), dc.this_());
                            b0.return_();
                        });
                    });
                });
                sc.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), sc.this_());
                        b0.return_();
                    });
                });
            });
        });
        Class<?> deepClass = tcm.loadClass("io.quarkus.gizmo2.ResetOuter$Static$Deep");
        // should have this$0 (depth resets at static boundary)
        Field this0 = deepClass.getDeclaredField("this$0");
        assertNotNull(this0, "Deep should have this$0 after static boundary reset");
        // verify type is the immediately enclosing class (Static)
        assertEquals(tcm.loadClass("io.quarkus.gizmo2.ResetOuter$Static"), this0.getType());
    }

    /**
     * Test multi-level nesting: A > B (inner) > C (inner).
     * B should have this$0 (type A), C should have this$1 (type B).
     */
    @Test
    public void multiLevelNesting() throws Exception {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc outerDesc = g.class_("io.quarkus.gizmo2.MultiOuter", cc -> {
            cc.class_("B", bc -> {
                bc.class_("C", innerC -> {
                    innerC.constructor(ctc -> {
                        ctc.body(b0 -> {
                            b0.invokeSpecial(ConstructorDesc.of(Object.class), innerC.this_());
                            b0.return_();
                        });
                    });
                });
                bc.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), bc.this_());
                        b0.return_();
                    });
                });
            });
        });

        // B should have this$0 of type MultiOuter
        Class<?> bClass = tcm.loadClass("io.quarkus.gizmo2.MultiOuter$B");
        Field bThis0 = bClass.getDeclaredField("this$0");
        assertNotNull(bThis0);
        assertEquals(tcm.loadClass("io.quarkus.gizmo2.MultiOuter"), bThis0.getType());

        // C should have this$1 of type MultiOuter$B
        Class<?> cClass = tcm.loadClass("io.quarkus.gizmo2.MultiOuter$B$C");
        Field cThis1 = cClass.getDeclaredField("this$1");
        assertNotNull(cThis1);
        assertEquals(bClass, cThis1.getType());

        // verify NestMembers on MultiOuter lists both B and C
        ClassFile cf = ClassFile.of();
        ClassModel outerModel = tcm.readClass(outerDesc, cf::parse);
        NestMembersAttribute nma = outerModel.findAttribute(Attributes.nestMembers()).orElse(null);
        assertNotNull(nma);
        List<String> nestMembers = nma.nestMembers().stream().map(e -> e.asSymbol().displayName()).sorted().toList();
        assertTrue(nestMembers.contains("MultiOuter$B"));
        assertTrue(nestMembers.contains("MultiOuter$B$C"));

        // verify NestHost on B and C both point to MultiOuter
        ClassModel bModel = tcm.readClass(ClassDesc.of("io.quarkus.gizmo2.MultiOuter$B"), cf::parse);
        assertEquals(outerDesc, bModel.findAttribute(Attributes.nestHost()).orElseThrow().nestHost().asSymbol());
        ClassModel cModel = tcm.readClass(ClassDesc.of("io.quarkus.gizmo2.MultiOuter$B$C"), cf::parse);
        assertEquals(outerDesc, cModel.findAttribute(Attributes.nestHost()).orElseThrow().nestHost().asSymbol());
    }

    /**
     * Test private access on member classes.
     */
    @Test
    public void privateMemberClass() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc outerDesc = g.class_("io.quarkus.gizmo2.PrivOuter", cc -> {
            ClassDesc innerDesc = cc.class_("PrivInner", ic -> {
                ic.setAccess(AccessLevel.PRIVATE);
                ic.addFlag(ModifierFlag.STATIC);
                ic.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.return_();
                    });
                });
                ic.method("secret", mc -> {
                    mc.returning(Object.class);
                    mc.body(b0 -> {
                        b0.return_(Const.of("private"));
                    });
                });
            });
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(b0 -> {
                    Expr inner = b0.new_(ConstructorDesc.of(innerDesc));
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(innerDesc, "secret", Object.class), inner));
                });
            });
        });
        assertEquals("private", tcm.staticMethod(outerDesc, "test", Supplier.class).get());
    }

    /**
     * Test member interface attributes: NestHost, NestMembers, InnerClasses.
     */
    @Test
    public void memberInterfaceAttributes() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc outerDesc = g.class_("io.quarkus.gizmo2.IfaceAttrOuter", cc -> {
            cc.interface_("MyIface", ic -> {
                ic.method("doIt", mc -> {
                    mc.returning(String.class);
                });
            });
        });
        ClassDesc ifaceDesc = ClassDesc.of("io.quarkus.gizmo2.IfaceAttrOuter$MyIface");
        ClassFile cf = ClassFile.of();

        // outer NestMembers
        ClassModel outerModel = tcm.readClass(outerDesc, cf::parse);
        NestMembersAttribute nma = outerModel.findAttribute(Attributes.nestMembers()).orElse(null);
        assertNotNull(nma);
        assertTrue(nma.nestMembers().stream().anyMatch(e -> e.asSymbol().equals(ifaceDesc)));

        // outer InnerClasses
        InnerClassesAttribute outerIca = outerModel.findAttribute(Attributes.innerClasses()).orElse(null);
        assertNotNull(outerIca);
        assertTrue(outerIca.classes().stream().anyMatch(i -> i.innerClass().asSymbol().equals(ifaceDesc)));

        // interface NestHost
        ClassModel ifaceModel = tcm.readClass(ifaceDesc, cf::parse);
        NestHostAttribute nha = ifaceModel.findAttribute(Attributes.nestHost()).orElse(null);
        assertNotNull(nha);
        assertEquals(outerDesc, nha.nestHost().asSymbol());

        // interface InnerClasses
        InnerClassesAttribute ifaceIca = ifaceModel.findAttribute(Attributes.innerClasses()).orElse(null);
        assertNotNull(ifaceIca);
        InnerClassInfo ici = ifaceIca.classes().stream()
                .filter(i -> i.innerClass().asSymbol().equals(ifaceDesc))
                .findFirst().orElse(null);
        assertNotNull(ici);
        // member interface is implicitly static
        assertTrue((ici.flagsMask() & ClassFile.ACC_STATIC) != 0);
    }

    /**
     * Test automatic outer {@code this} resolution: inner class code uses
     * the outer class creator's {@code this_()} and the {@code this$0} field
     * access is generated automatically.
     */
    @Test
    public void autoOuterThisResolution() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc outerDesc = ClassDesc.of("io.quarkus.gizmo2.AutoOuterThis");
        g.class_("io.quarkus.gizmo2.AutoOuterThis", cc -> {
            FieldDesc outerField = cc.field("value", ifc -> {
                ifc.setType(String.class);
                ifc.setAccess(AccessLevel.PRIVATE);
            });
            cc.class_("Inner", ic -> {
                ic.constructor(ctc -> {
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.return_();
                    });
                });
                ic.method("getOuterValue", mc -> {
                    mc.returning(Object.class);
                    mc.body(b0 -> {
                        // automatic: cc.this_() resolves through this$0
                        b0.return_(cc.this_().field(outerField));
                    });
                });
            });
            cc.constructor(ctc -> {
                ParamVar val = ctc.parameter("val", String.class);
                ctc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    b0.set(cc.this_().field(outerField), val);
                    b0.return_();
                });
            });
            cc.staticMethod("run", mc -> {
                mc.returning(Object.class);
                ParamVar val = mc.parameter("val", Object.class);
                mc.body(b0 -> {
                    ClassDesc innerDesc = ClassDesc.of("io.quarkus.gizmo2.AutoOuterThis$Inner");
                    Expr outer = b0.new_(ConstructorDesc.of(outerDesc, String.class), val);
                    Expr inner = b0.new_(ConstructorDesc.of(innerDesc, outerDesc), outer);
                    b0.return_(b0.invokeVirtual(
                            ClassMethodDesc.of(innerDesc, "getOuterValue", Object.class), inner));
                });
            });
        });
        assertEquals("auto-resolved", tcm.staticMethod(outerDesc, "run", Function.class).apply("auto-resolved"));
    }

    /**
     * Test automatic outer {@code this} resolution across multiple nesting levels:
     * A > B (inner) > C (inner). Code in C uses A's {@code this_()} and the chain
     * {@code this.this$1.this$0} is generated automatically.
     */
    @Test
    public void autoOuterThisMultiLevel() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc aDesc = ClassDesc.of("io.quarkus.gizmo2.AutoMultiA");
        ClassDesc bDesc = ClassDesc.of("io.quarkus.gizmo2.AutoMultiA$B");
        ClassDesc cDesc = ClassDesc.of("io.quarkus.gizmo2.AutoMultiA$B$C");
        g.class_("io.quarkus.gizmo2.AutoMultiA", cc -> {
            FieldDesc aField = cc.field("aValue", ifc -> {
                ifc.setType(String.class);
            });
            cc.class_("B", bc -> {
                FieldDesc bField = bc.field("bValue", ifc -> {
                    ifc.setType(String.class);
                });
                bc.class_("C", innerC -> {
                    innerC.constructor(ctc -> {
                        ctc.body(b0 -> {
                            b0.invokeSpecial(ConstructorDesc.of(Object.class), innerC.this_());
                            b0.return_();
                        });
                    });
                    innerC.method("describe", mc -> {
                        mc.returning(Object.class);
                        mc.body(b0 -> {
                            // cc.this_() from C should traverse this$1 then this$0
                            Expr aVal = cc.this_().field(aField);
                            // bc.this_() from C should traverse this$1
                            Expr bVal = bc.this_().field(bField);
                            Expr result = b0.withString(aVal).concat(b0.withString(Const.of(":")).concat(bVal));
                            b0.return_(result);
                        });
                    });
                });
                bc.constructor(ctc -> {
                    ParamVar val = ctc.parameter("val", String.class);
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), bc.this_());
                        b0.set(bc.this_().field(bField), val);
                        b0.return_();
                    });
                });
            });
            cc.constructor(ctc -> {
                ParamVar val = ctc.parameter("val", String.class);
                ctc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    b0.set(cc.this_().field(aField), val);
                    b0.return_();
                });
            });
            cc.staticMethod("run", mc -> {
                mc.returning(Object.class);
                ParamVar aVal = mc.parameter("aVal", Object.class);
                ParamVar bVal = mc.parameter("bVal", Object.class);
                mc.body(b0 -> {
                    Expr a = b0.new_(ConstructorDesc.of(aDesc, CD_String), aVal);
                    Expr b = b0.new_(ConstructorDesc.of(bDesc, aDesc, CD_String), a, bVal);
                    Expr c = b0.new_(ConstructorDesc.of(cDesc, bDesc), b);
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(cDesc, "describe", Object.class), c));
                });
            });
        });
        assertEquals("hello:world",
                tcm.staticMethod(aDesc, "run", TwoParams.class).apply("hello", "world"));
    }

    /**
     * Verify that {@code this_()} on the inner class's own creator still produces
     * a simple {@code aload(0)} (no field chain).
     */
    @Test
    public void sameClassThisUnchanged() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc outerDesc = ClassDesc.of("io.quarkus.gizmo2.SameThisOuter");
        ClassDesc innerDesc = ClassDesc.of("io.quarkus.gizmo2.SameThisOuter$Inner");
        g.class_("io.quarkus.gizmo2.SameThisOuter", cc -> {
            cc.class_("Inner", ic -> {
                FieldDesc label = ic.field("label", ifc -> {
                    ifc.setType(String.class);
                });
                ic.constructor(ctc -> {
                    ParamVar val = ctc.parameter("val", String.class);
                    ctc.body(b0 -> {
                        b0.invokeSpecial(ConstructorDesc.of(Object.class), ic.this_());
                        b0.set(ic.this_().field(label), val);
                        b0.return_();
                    });
                });
                ic.method("getLabel", mc -> {
                    mc.returning(Object.class);
                    mc.body(b0 -> {
                        // ic.this_() within the same inner class — simple aload(0)
                        b0.return_(ic.this_().field(label));
                    });
                });
            });
            cc.staticMethod("run", mc -> {
                mc.returning(Object.class);
                ParamVar val = mc.parameter("val", Object.class);
                mc.body(b0 -> {
                    // Inner needs an outer instance; create a dummy outer
                    Expr outer = b0.new_(ConstructorDesc.of(outerDesc));
                    Expr inner = b0.new_(ConstructorDesc.of(innerDesc, outerDesc, CD_String), outer, val);
                    b0.return_(b0.invokeVirtual(ClassMethodDesc.of(innerDesc, "getLabel", Object.class), inner));
                });
            });
            cc.constructor(ctc -> {
                ctc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    b0.return_();
                });
            });
        });
        assertEquals("same-class", tcm.staticMethod(outerDesc, "run", Function.class).apply("same-class"));
    }
}
