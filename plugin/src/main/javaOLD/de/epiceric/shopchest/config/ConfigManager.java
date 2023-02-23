package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;

import java.io.File;

public class ConfigManager {

    private final ShopChest plugin;
    private final File dataFolder;

    public ConfigManager(ShopChest plugin, File dataFolder) {
        this.plugin = plugin;
        this.dataFolder = dataFolder;
    }

    /**
     * Check if the plugin data folder is created and writable
     *
     * @return {@code true} if the plugin can write in the folder. {@code false} otherwise
     */
    public boolean checkDataFolder() {
        if (dataFolder.exists())
            return dataFolder.canWrite();
        return dataFolder.mkdirs();
    }

    /**
     * Load every configuration file
     */
    public void load() {
        final GlobalConfig globalConfig = new GlobalConfig(dataFolder, plugin, "config.yml", true);
        globalConfig.load(plugin);

        final TaxConfig taxConfig = new TaxConfig(dataFolder, plugin, "taxes.yml", true);
        taxConfig.load(plugin);

        // TODO Load language configuration
    }

    /**
     * Unload the data loaded by configurations
     */
    public void unload() {
        plugin.getTaxManager().clear();
    }

    /**
     * Reload the manager
     * It performs an unload and a reload
     */
    public void reload() {
        unload();
        load();
    }

}
