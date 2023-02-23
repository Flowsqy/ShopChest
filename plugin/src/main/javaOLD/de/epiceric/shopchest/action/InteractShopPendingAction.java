package de.epiceric.shopchest.action;

import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;

public class InteractShopPendingAction extends ShopCheckerPendingAction {

    public InteractShopPendingAction(long duration) {
        super(duration, true);
    }

    @Override
    public CompletableFuture<Void> execute(Block block) {
        // Get the Shop
        // - if special condition (e.g. info stick) -> apply condition
        // - if owner -> open it
        // - else start transaction
        return null;
    }
}
