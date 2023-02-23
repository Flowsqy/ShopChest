package de.epiceric.shopchest;

import de.epiceric.shopchest.debug.DebugLogger;
import de.epiceric.shopchest.nms.Platform;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopChestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println(DebugLogger.class);
        Platform p = null;
    }
}
