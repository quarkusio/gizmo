package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;

/**
 * A {@code try} block with exception catches.
 */
final class TryCatch extends Item {
    private final BlockCreatorImpl body;
    private List<Catch> catches = List.of();

    TryCatch(final BlockCreatorImpl body) {
        this.body = body;
    }

    public boolean mayFallThrough() {
        return body.mayFallThrough() || catches.stream().map(Catch::body).anyMatch(BlockCreatorImpl::mayFallThrough);
    }

    BlockCreatorImpl addCatch(final ClassDesc superType, Set<ClassDesc> types) {
        BlockCreatorImpl bci = new BlockCreatorImpl(body.parent(), superType);
        bci.branchTarget();
        Catch catch_ = new Catch(superType, types, bci);
        if (catches instanceof ArrayList<Catch> al) {
            al.add(catch_);
        } else {
            catches = Util.listWith(catches, catch_);
        }
        return bci;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        Label after = cb.newLabel();
        StackMapBuilder.Saved saved = smb.save();
        body.writeCode(cb, block, smb);
        smb.restore(saved);
        boolean afterFrameInfo = false;
        if (body.mayFallThrough()) {
            cb.goto_(after);
            smb.wroteCode();
            afterFrameInfo = true;
        }
        for (Catch catch_ : catches) {
            BlockCreatorImpl catchBody = catch_.body();
            smb.push(catch_.superType());
            catchBody.writeCode(cb, block, smb);
            smb.restore(saved);
            if (catchBody.mayFallThrough()) {
                cb.goto_(after);
                afterFrameInfo = true;
                smb.wroteCode();
            }
            for (ClassDesc type : catch_.types()) {
                if (type.equals(CD_Throwable)) {
                    cb.exceptionCatchAll(body.startLabel(), body.endLabel(), catchBody.startLabel());
                } else {
                    cb.exceptionCatch(body.startLabel(), body.endLabel(), catchBody.startLabel(), type);
                }
            }
        }
        cb.labelBinding(after);
        if (afterFrameInfo) {
            smb.addFrameInfo(cb);
        }
    }

    record Catch(ClassDesc superType, Set<ClassDesc> types, BlockCreatorImpl body) {
    }
}
