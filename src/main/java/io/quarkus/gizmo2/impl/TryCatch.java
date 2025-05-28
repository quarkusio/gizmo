package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.creator.BlockCreator;

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

    BlockCreatorImpl addCatch(final ClassDesc superType, Set<ClassDesc> types, String caughtName) {
        BlockCreatorImpl bci = new BlockCreatorImpl(body.parent(), superType);
        Catch catch_ = new Catch(types, caughtName, bci);
        if (catches instanceof ArrayList<Catch> al) {
            al.add(catch_);
        } else {
            catches = Util.listWith(catches, catch_);
        }
        return bci;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Label after = cb.newLabel();
        body.writeCode(cb, block);
        if (body.mayFallThrough()) {
            cb.goto_(after);
        }
        for (Catch catch_ : catches) {
            BlockCreatorImpl catchBody = catch_.body();
            catchBody.writeCode(cb, block);
            if (catchBody.mayFallThrough()) {
                cb.goto_(after);
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
    }

    static final class Catch {
        private final Set<ClassDesc> types;
        private final String caughtName;
        private final BlockCreatorImpl body;

        Catch(final Set<ClassDesc> types, final String caughtName, final BlockCreatorImpl body) {
            this.types = types;
            this.caughtName = caughtName;
            this.body = body;
        }

        Set<ClassDesc> types() {
            return types;
        }

        BlockCreatorImpl body() {
            return body;
        }

        void accept(final BiConsumer<BlockCreator, ? super LocalVar> builder) {
            body().accept((b, e) -> {
                builder.accept(b, b.localVar(caughtName, e));
            });
        }
    }
}
