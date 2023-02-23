package de.epiceric.shopchest.action;

import org.bukkit.block.Block;

public abstract class ShopMaterialPendingAction extends PendingAction {

    public ShopMaterialPendingAction(long duration) {
        super(duration);
    }

    @Override
    public boolean canApply(Block block) {
        // Check if the block can be a shop
        return false;
    }

}
