package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.debug.DebugLogger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaxManager {

    private double defaultTax;
    private Map<Material, Double> taxByMaterial;

    public TaxManager() {
        defaultTax = 0.0;
        taxByMaterial = null;
    }

    /**
     * Load the {@link TaxManager}
     *
     * @param debugLogger  The {@link DebugLogger} instance to log every step of the progress
     * @param taxesSection The {@link ConfigurationSection} that is used to fill the {@link TaxManager}
     */
    public void load(DebugLogger debugLogger, ConfigurationSection taxesSection) {
        debugLogger.debug("Initialize TaxManager");

        if (taxByMaterial != null) {
            throw new IllegalStateException("This TaxManager is already loaded");
        }

        if (taxesSection == null) {
            taxByMaterial = Collections.emptyMap();
            return;
        }

        final Map<Material, Double> loadingTaxMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : taxesSection.getValues(false).entrySet()) {
            final String valueStr = String.valueOf(entry.getValue());
            final double taxValue;
            try {
                taxValue = Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                debugLogger.debug("Not a number at key '" + entry.getKey() + "' : " + valueStr);
                continue;
            }

            final double taxFactor = taxValue / 100 + 1;

            if (entry.getKey().equals("default")) {
                defaultTax = taxFactor;
                continue;
            }
            final Material material = Material.getMaterial(entry.getKey().toUpperCase(Locale.ENGLISH));
            if (material == null) {
                debugLogger.debug("Unknown material : " + entry.getKey());
                continue;
            }

            loadingTaxMap.put(material, taxFactor);
        }

        if (loadingTaxMap.isEmpty()) {
            taxByMaterial = Collections.emptyMap();
            return;
        }

        taxByMaterial = loadingTaxMap;
    }

    /**
     * Clear the {@link TaxManager}
     */
    public void clear() {
        if (taxByMaterial == null) {
            throw new IllegalStateException("This TaxManager is not loaded");
        }

        taxByMaterial.clear();
        taxByMaterial = null;
    }

    /**
     * Apply taxes
     *
     * @param material The taxed {@link Material}
     * @param price    The price without taxes
     * @return The price with taxes
     */
    public double applyTaxes(Material material, double price) {
        return price * taxByMaterial.getOrDefault(material, defaultTax);
    }

}
