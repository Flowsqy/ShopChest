package de.epiceric.shopchest.config.hologram;

import de.epiceric.shopchest.config.Placeholder;

import java.util.List;
import java.util.Map;

public class HologramLine {

    private final List<HologramOption> options;

    public HologramLine(List<HologramOption> options) {
        this.options = options;
    }

    public final String get(Map<HologramFormat.Requirement, Object> reqMap, Map<Placeholder, Object> plaMap) {
        final HologramOption option = getOption(reqMap);
        return option == null ? "" : option.getFormat(plaMap);
    }

    public final boolean isDynamic() {
        for (HologramOption option : options) {
            if (option.isDynamic()) {
                return true;
            }
        }
        return false;
    }

    public final boolean isDynamic(Map<HologramFormat.Requirement, Object> reqMap) {
        final HologramOption option = getOption(reqMap);
        return option != null && option.isDynamic();
    }

    private HologramOption getOption(Map<HologramFormat.Requirement, Object> reqMap) {
        for (HologramOption option : options) {
            if (option.isValid(reqMap)) {
                return option;
            }
        }
        return null;
    }

}
