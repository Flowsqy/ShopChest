package de.epiceric.shopchest.config.hologram;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.config.hologram.condition.Condition;
import de.epiceric.shopchest.config.hologram.line.FormatReplacer;
import de.epiceric.shopchest.config.hologram.line.FormattedLine;
import de.epiceric.shopchest.config.hologram.parser.FormatParser;
import de.epiceric.shopchest.config.hologram.parser.ParserResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HologramFormat {

    private final ShopChest plugin;
    private HologramLine[] lines;

    public HologramFormat(ShopChest plugin) {
        this.plugin = plugin;
    }

    public void load() {
        // Load file
        final File configFile = new File(plugin.getDataFolder(), "hologram-format.yml");
        final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        // Get lines
        final ConfigurationSection linesSection = config.getConfigurationSection("lines");
        if (linesSection == null) {
            plugin.getDebugLogger().debug("Can not the hologram format, there is no 'lines' section");
            return;
        }
        // Get options
        final Map<String, ConfigurationSection> optionSections = new HashMap<>();
        for (String linesId : linesSection.getKeys(false)) {
            final ConfigurationSection lineSection = linesSection.getConfigurationSection(linesId);
            if (lineSection == null) {
                plugin.getDebugLogger().debug("'" + linesId + "' is not a line section, skip it in hologram format");
                continue;
            }
            final ConfigurationSection optionSection = lineSection.getConfigurationSection("options");
            if (optionSection == null) {
                plugin.getDebugLogger().debug("The line '" + linesId + "' does not contain 'options' section, skip it in hologram format");
                continue;
            }
            optionSections.put(linesId, optionSection);
        }

        // Sort lines by id
        final List<ConfigurationSection> orderedOptionSections = optionSections.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // Prepare formatter
        final FormatData data = new FormatData();
        final FormatParser parser = new FormatParser();

        // Deserialize each option
        final List<HologramLine> lines = new ArrayList<>();
        // For every line
        for (ConfigurationSection optionsSection : orderedOptionSections) {
            final List<HologramOption> options = new LinkedList<>();
            // For every option of the line
            for (final String optionKey : optionsSection.getKeys(false)) {
                final ConfigurationSection optionSection = optionsSection.getConfigurationSection(optionKey);
                if (optionSection == null) {
                    plugin.getDebugLogger().debug("'" + optionKey + "' is not an option section, skip it in hologram format");
                    continue;
                }
                // Get the requirements
                final List<Condition<Map<Requirement, Object>>> requirementConditions = new LinkedList<>();

                boolean dynamic = false;
                for (String requirement : optionSection.getStringList("requirements")) {
                    if (requirement == null) {
                        continue;
                    }
                    final ParserResult<Requirement> result;
                    try {
                        result = parser.parse(
                                requirement,
                                data.getRequirements(),
                                data.getRequirementsTypes()
                        );
                    } catch (Exception e) {
                        plugin.getDebugLogger().debug("Failed to parse the requirement '" + requirement + "'");
                        plugin.getDebugLogger().debug(e);
                        continue;
                    }

                    // If it's a condition, add a condition requirement and check if it's dynamic
                    if (result.isCondition()) {
                        requirementConditions.add(result.getCondition());
                        if (!dynamic) {
                            dynamic = isRequirementDynamic(requirement);
                        }
                        continue;
                    }
                    plugin.getDebugLogger().debug("The requirement '" + requirement + "' does not represent a condition");
                }

                // Get the format
                final String format = optionSection.getString("format");
                if (format == null) {
                    plugin.getDebugLogger().debug("The option '" + optionKey + "' does not contains format. Skip it in hologram format");
                    continue;
                }

                // Parse the format and check if it's dynamic
                final FormattedLine<Placeholder> formattedString = evaluateFormat(format, parser, data);
                if (!dynamic) {
                    dynamic = isPlaceholderDynamic(format);
                }

                // Add the option
                options.add(new HologramOption(
                        formattedString,
                        requirementConditions.isEmpty() ? null : requirementConditions,
                        dynamic
                ));

                // There is no requirement for this option, so it's the last
                // (it will always be picked so the next options are skipped)
                if (requirementConditions.isEmpty()) {
                    break;
                }
            }
            if (options.isEmpty()) {
                plugin.getDebugLogger().debug("The line does not contain any options, skip it in hologram format");
                continue;
            }

            // Add the line
            lines.add(new HologramLine(new ArrayList<>(options)));
        }

        this.lines = lines.toArray(new HologramLine[0]);
    }

    private boolean isRequirementDynamic(String requirement) {
        return requirement.contains(Requirement.IN_STOCK.name()) || requirement.contains(Requirement.CHEST_SPACE.name());
    }

    private boolean isPlaceholderDynamic(String format) {
        return format.contains(Placeholder.STOCK.toString()) || format.contains(Placeholder.CHEST_SPACE.toString());
    }

    private FormattedLine<Placeholder> evaluateFormat(String format, FormatParser parser, FormatData data) {
        final FormatReplacer<Placeholder> formatReplacer = new FormatReplacer<>(format);

        // Detect and evaluate accolade inner parts
        final Map<String, ParserResult<Placeholder>> parsedScripts = new HashMap<>();
        final Matcher matcher = Pattern.compile("\\{([^}]+)}").matcher(format);

        while (matcher.find()) {
            final String withBrackets = matcher.group();
            final String script = withBrackets.substring(1, withBrackets.length() - 1);

            final ParserResult<Placeholder> result;
            try {
                result = parser.parse(script, data.getPlaceholders(), data.getPlaceholderTypes());
            } catch (Exception e) {
                parsedScripts.put(withBrackets, new ParserResult<>(null, null, null, null));
                plugin.getDebugLogger().debug("Failed to evaluate the script '" + script + "'");
                plugin.getDebugLogger().debug(e);
                continue;
            }
            parsedScripts.put(withBrackets, result);
        }

        // Replace accolade inner parts
        for (Map.Entry<String, ParserResult<Placeholder>> entry : parsedScripts.entrySet()) {
            final String regex = entry.getKey();
            final ParserResult<Placeholder> result = entry.getValue();
            if (result.isConstant()) {
                formatReplacer.replace(regex, String.valueOf(result.getConstant()));
            } else if (result.isValue()) {
                formatReplacer.replace(regex, new FormattedLine.ProviderToString<>(result.getValue()));
            } else if (result.isCondition()) {
                formatReplacer.replace(regex, new FormattedLine.ConditionToString<>(result.getCondition()));
            } else if (result.isCalculation()) {
                formatReplacer.replace(regex, new FormattedLine.CalculationToString<>(result.getCalculation()));
            } else {
                formatReplacer.replace(regex, "");
            }
        }

        // Replace classics placeholders
        for (Map.Entry<String, Placeholder> entry : data.getPlaceholders().entrySet()) {
            formatReplacer.replace(entry.getKey(), new FormattedLine.MapToString<>(entry.getValue()));
        }

        return formatReplacer.create();
    }

    /**
     * Get the format for the given line of the hologram
     *
     * @param line   Line of the hologram
     * @param reqMap Values of the requirements that might be needed by the format (contains {@code null} if not comparable)
     * @param plaMap Values of the placeholders that might be needed by the format
     * @return The format of the first working option, or an empty String if no option is working
     * because of not fulfilled requirements
     */
    public String getFormat(int line, Map<Requirement, Object> reqMap, Map<Placeholder, Object> plaMap) {
        return lines[line].get(reqMap, plaMap);
    }

    public void reload() {
        lines = null;
        load();
    }

    /**
     * Whether a hologram can be dynamic
     *
     * @return Whether the hologram text has to change dynamically without reloading
     */
    public boolean isDynamic() {
        for (HologramLine line : lines) {
            if (line.isDynamic()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The dynamic state of a specific shop's hologram
     *
     * @param reqValues The shop values
     * @return Whether the hologram text has to change dynamically without reloading
     */
    public boolean isDynamic(Map<Requirement, Object> reqValues) {
        for (HologramLine line : lines) {
            if (line.isDynamic(reqValues)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The dynamic static of a line of a specific shop's hologram
     *
     * @param line      The line to check
     * @param reqValues The shop values
     * @return Whether the hologram text has to change dynamically without reloading
     */
    public boolean isDynamic(int line, Map<Requirement, Object> reqValues) {
        return lines[line].isDynamic(reqValues);
    }

    /**
     * @return Amount of lines in a hologram
     */
    public int getLineCount() {
        if (lines == null) {
            throw new IllegalStateException("The hologram format is not loaded");
        }
        return lines.length;
    }

    /**
     * @return Configuration of the "hologram-format.yml" file
     * @deprecated The configuration is not used during runtime.
     * If you invoke this method, you will load the configuration from the disk.
     */
    @Deprecated
    public YamlConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "hologram-format.yml"));
    }

    public enum Requirement {
        VENDOR, AMOUNT, ITEM_TYPE, ITEM_NAME, HAS_ENCHANTMENT, BUY_PRICE,
        SELL_PRICE, HAS_POTION_EFFECT, IS_MUSIC_DISC, IS_POTION_EXTENDED, IS_BANNER_PATTERN,
        IS_WRITTEN_BOOK, ADMIN_SHOP, NORMAL_SHOP, IN_STOCK, MAX_STACK, CHEST_SPACE, DURABILITY
    }
}
