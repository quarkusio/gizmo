package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;

import java.util.List;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link List}.
 */
public class ListOps extends CollectionOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the list instance (must not be {@code null})
     */
    public ListOps(final BlockCreator bc, final Expr obj) {
        super(bc, obj);
    }

    /**
     * Generate a call to {@link List#get(int)}.
     *
     * @param index the index of the element to return (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr get(Expr index) {
        return bc.invokeInterface(MD_List.get, obj, index);
    }

    /**
     * Generate a call to {@link List#get(int)}.
     *
     * @param index the index of the element to return
     * @return the expression of the result (not {@code null})
     */
    public Expr get(int index) {
        return get(Const.of(index));
    }

}
