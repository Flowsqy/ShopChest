package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class TaxConfig extends ConfigFile {

    private final ShopChest plugin;

    public TaxConfig(File dataFolder, ShopChest plugin, String fileName, boolean resource) {
        super(dataFolder, fileName, resource);
        this.plugin = plugin;
    }

    @Override
    protected void loadConfiguration(YamlConfiguration configuration) {
        plugin.getTaxManager().load(plugin.getDebugLogger(), configuration);
    }
}
