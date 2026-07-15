package de.epiceric.shopchest.language.item;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.epiceric.shopchest.ShopChest;

public class LocalizedItemNameManager implements ItemNameManager {

    private final static String ERROR_ITEM_NAME = "ERROR";

    private final Map<String, String> itemTranslations;

    public LocalizedItemNameManager(@NotNull Map<String, String> itemTranslations) {
        this.itemTranslations = itemTranslations;
    }

    @Override
    @Nullable
    public String getItemName(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }

        final ItemMeta meta;
        if (!stack.hasItemMeta() || (meta = stack.getItemMeta()) == null) {
            return getDefaultName(stack);
        }

        final String displayName;
        if (meta.hasDisplayName() && !(displayName = meta.getDisplayName()).isEmpty()) {
            return displayName;
        }

        if (meta instanceof BookMeta) {
            return ((BookMeta) meta).getTitle();
        }

        if (meta instanceof SkullMeta) {
            final SkullMeta skullMeta = (SkullMeta) meta;
            if (!skullMeta.hasOwner()) {
                return getDefaultName(stack);
            }
            skullMeta.getOwningPlayer();
            final String defaultName = getDefaultName(stack);
            final String ownerName = Objects.requireNonNull(skullMeta.getOwningPlayer()).getName();
            if (ownerName == null) {
                return defaultName;
            }
            return String.format(defaultName, ownerName);
        }

        return getDefaultName(stack);
    }

    @NotNull
    private String getDefaultName(@NotNull ItemStack stack) {
        try {
            return getCached(stack.getType().getTranslationKey());
        } catch (Exception e) {
            ShopChest.getInstance().getLogger().log(Level.SEVERE, e.getMessage());
            return ERROR_ITEM_NAME;
        }
    }

    @NotNull
    private String getCached(@NotNull String key) {
        final String cachedTranslation = itemTranslations.get(key);
        if (cachedTranslation == null) {
            // Keep this behavior to ensure quick fixes
            throw new RuntimeException("Could not get the translation for '" + key + "'. Report it to github");
        }
        return cachedTranslation;
    }

}
