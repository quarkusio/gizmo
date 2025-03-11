package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.LambdaCreator;
import io.quarkus.gizmo2.creator.ParamCreator;

// TODO
public final class LambdaCreatorImpl implements LambdaCreator {
    public ClassDesc lambdaType() {
        return null;
    }

    public Var capture(final String name, final Expr value) {
        return null;
    }

    public void body(final Consumer<BlockCreator> builder) {

    }

    public ClassDesc type() {
        return null;
    }

    public ParamVar param(final String name, final int position) {
        return null;
    }

    public ParamVar parameter(final String name, final Consumer<ParamCreator> builder) {
        return null;
    }

    public void withFlag(final AccessFlag flag) {

    }

    public ClassDesc owner() {
        return null;
    }

    public String name() {
        return "";
    }

    public void withAnnotation(final RetentionPolicy retention, final Annotation annotation) {

    }

    @Override
    public void public_() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void packagePrivate() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void private_() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void protected_() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void final_() {
        // TODO Auto-generated method stub
        
    }
    
}
