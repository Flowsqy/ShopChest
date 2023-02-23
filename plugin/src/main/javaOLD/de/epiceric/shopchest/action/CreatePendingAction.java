package de.epiceric.shopchest.action;

import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;

public class CreatePendingAction extends ShopCheckerPendingAction {

    public CreatePendingAction(long duration) {
        super(duration, false);
    }

    @Override
    public CompletableFuture<Void> execute(Block block) {
        // Create a shop
        return null;
    }
}
