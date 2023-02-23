package de.epiceric.shopchest.listeners;

import java.util.ArrayList;

import de.epiceric.shopchest.utils.ShopUtils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import de.epiceric.shopchest.ShopChest;

// https://github.com/Flowsqy/ShopChest/commit/9a9f95eec23746f8316e9245b2c4e6f1c7595593
// :')

public class BlockExplodeListener implements Listener {

    private ShopChest plugin;

    public BlockExplodeListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        ArrayList<Block> bl = new ArrayList<>(e.blockList());
        for (Block b : bl) {
            if (ShopUtils.isShopMaterial(b.getType())) {
                if (plugin.getShopUtils().isShop(b.getLocation())) e.blockList().remove(b);
            }
        }
    }

}
