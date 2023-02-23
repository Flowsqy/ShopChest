package de.epiceric.shopchest.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyLoader {

    /**
     * Load the Vault {@link Economy}
     *
     * @return The loaded {@link Economy} implementation
     */
    public Economy loadEconomy() {
        checkVaultPlugin();
        return getEconomy();
    }

    /**
     * Check Vault plugin.
     *
     * @throws RuntimeException if vault is not present, not enable or is not the correct 'Vault' plugin
     */
    private void checkVaultPlugin() {
        final PluginManager plManager = Bukkit.getPluginManager();
        final Plugin vaultPlugin = plManager.getPlugin("Vault");
        if (vaultPlugin == null) {
            // Supposed to be impossible as Vault is in the 'depend' section in plugin.yml
            throw new RuntimeException("'Vault' plugin is not present");
        }
        if (!plManager.isPluginEnabled(vaultPlugin)) {
            throw new RuntimeException("'Vault' plugin is not enable");
        }
        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can retrieve the 'Vault' economy interface." +
                    " You may use an unsupported version of the 'Vault' plugin or the wrong 'Vault' plugin");
        }
    }

    /**
     * Retrieve the registered {@link Economy} implementation
     *
     * @return The Vault {@link Economy} implementation
     * @throws RuntimeException if vault does not provide an {@link Economy} implementation
     */
    private Economy getEconomy() {
        final RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new RuntimeException("Could not retrieve any economy implementation. Maybe you did not install any economy plugin.");
        }
        return rsp.getProvider();
    }

}
