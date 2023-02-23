package de.epiceric.shopchest.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public abstract class ConfigFile {
    private final File file;
    private final boolean resource;

    public ConfigFile(File dataFolder, String fileName, boolean resource) {
        this.file = new File(dataFolder, fileName);
        this.resource = resource;
    }

    public void load(Plugin plugin) {
        if(!checkFile(plugin)) {
            return;
        }
        loadConfiguration(YamlConfiguration.loadConfiguration(file));
    }

    /**
     * Check if the file exist and copy it if needed
     *
     * @return {@code true} if the file exist. {@code false} otherwise
     */
    private boolean checkFile(Plugin plugin) {
        if (file.exists()) {
            return true;

        }
        if(!resource) {
            return false;
        }

        try {
            Files.copy(Objects.requireNonNull(plugin.getResource(file.getName())), file.toPath());
        } catch (IOException ignored) {
        }
        return true;
    }

    /**
     * Extract data from the {@link YamlConfiguration}
     *
     * @param configuration The {@link YamlConfiguration}
     */
    protected abstract void loadConfiguration(YamlConfiguration configuration);

}
