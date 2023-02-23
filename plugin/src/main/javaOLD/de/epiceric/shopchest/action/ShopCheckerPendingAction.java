package de.epiceric.shopchest.action;

import org.bukkit.block.Block;

public abstract class ShopCheckerPendingAction extends ShopMaterialPendingAction {

    private final boolean shouldBeShop;

    public ShopCheckerPendingAction(long duration, boolean shouldBeShop) {
        super(duration);
        this.shouldBeShop = shouldBeShop;
    }

    @Override
    public boolean canApply(Block block) {
        if(!super.canApply(block)) {
            return false;
        }

        // Check if it's a shop (Shop access should be kept somewhere in cache)
        final boolean isShop = false;

        return shouldBeShop == isShop;
    }
}
