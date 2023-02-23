package de.epiceric.shopchest.config.hologram;

import de.epiceric.shopchest.config.Placeholder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class FormatData {

    private final Map<String, HologramFormat.Requirement> requirements;
    private final Map<HologramFormat.Requirement, Class<?>> requirementsTypes;
    private final Map<String, Placeholder> placeholders;
    private final Map<Placeholder, Class<?>> placeholderTypes;

    public FormatData() {
        requirements = new HashMap<>();
        requirementsTypes = new EnumMap<>(HologramFormat.Requirement.class);
        placeholders = new HashMap<>();
        placeholderTypes = new EnumMap<>(Placeholder.class);
        initRequirements();
        initPlaceholders();
    }

    public Map<String, HologramFormat.Requirement> getRequirements() {
        return requirements;
    }

    public Map<HologramFormat.Requirement, Class<?>> getRequirementsTypes() {
        return requirementsTypes;
    }

    public Map<String, Placeholder> getPlaceholders() {
        return placeholders;
    }

    public Map<Placeholder, Class<?>> getPlaceholderTypes() {
        return placeholderTypes;
    }

    private void initRequirements() {
        initRequirement(HologramFormat.Requirement.VENDOR, String.class);
        initRequirement(HologramFormat.Requirement.AMOUNT, Double.class);
        initRequirement(HologramFormat.Requirement.ITEM_TYPE, String.class);
        initRequirement(HologramFormat.Requirement.ITEM_NAME, String.class);
        initRequirement(HologramFormat.Requirement.HAS_ENCHANTMENT, Boolean.class);
        initRequirement(HologramFormat.Requirement.BUY_PRICE, Double.class);
        initRequirement(HologramFormat.Requirement.SELL_PRICE, Double.class);
        initRequirement(HologramFormat.Requirement.HAS_POTION_EFFECT, Boolean.class);
        initRequirement(HologramFormat.Requirement.IS_MUSIC_DISC, Boolean.class);
        initRequirement(HologramFormat.Requirement.IS_POTION_EXTENDED, Boolean.class);
        initRequirement(HologramFormat.Requirement.IS_BANNER_PATTERN, Boolean.class);
        initRequirement(HologramFormat.Requirement.IS_WRITTEN_BOOK, Boolean.class);
        initRequirement(HologramFormat.Requirement.ADMIN_SHOP, Boolean.class);
        initRequirement(HologramFormat.Requirement.NORMAL_SHOP, Boolean.class);
        initRequirement(HologramFormat.Requirement.IN_STOCK, Double.class);
        initRequirement(HologramFormat.Requirement.MAX_STACK, Double.class);
        initRequirement(HologramFormat.Requirement.CHEST_SPACE, Double.class);
        initRequirement(HologramFormat.Requirement.DURABILITY, Double.class);
    }

    private void initRequirement(HologramFormat.Requirement requirement, Class<?> type) {
        requirements.put(requirement.name(), requirement);
        requirementsTypes.put(requirement, type);
    }


    private void initPlaceholders() {
        initPlaceholder(Placeholder.VENDOR, String.class);
        initPlaceholder(Placeholder.AMOUNT, Double.class);
        initPlaceholder(Placeholder.ITEM_NAME, String.class);
        initPlaceholder(Placeholder.ENCHANTMENT, String.class);
        initPlaceholder(Placeholder.BUY_PRICE, Double.class);
        initPlaceholder(Placeholder.SELL_PRICE, Double.class);
        initPlaceholder(Placeholder.POTION_EFFECT, String.class);
        initPlaceholder(Placeholder.MUSIC_TITLE, String.class);
        initPlaceholder(Placeholder.BANNER_PATTERN_NAME, String.class);
        initPlaceholder(Placeholder.GENERATION, String.class);
        initPlaceholder(Placeholder.STOCK, Double.class);
        initPlaceholder(Placeholder.MAX_STACK, Double.class);
        initPlaceholder(Placeholder.CHEST_SPACE, Double.class);
        initPlaceholder(Placeholder.DURABILITY, Double.class);
    }

    private void initPlaceholder(Placeholder placeholder, Class<?> type) {
        placeholders.put(placeholder.toString(), placeholder);
        placeholderTypes.put(placeholder, type);
    }


}
