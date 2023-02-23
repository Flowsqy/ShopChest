package de.epiceric.shopchest.config.hologram.line;

import de.epiceric.shopchest.config.hologram.calculation.Calculation;
import de.epiceric.shopchest.config.hologram.condition.Condition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FormattedLine<P> {

    private final List<Function<Map<P, Object>, String>> tokens;

    public FormattedLine(List<Function<Map<P, Object>, String>> tokens) {
        this.tokens = tokens;
    }

    public String get(Map<P, Object> values) {
        final StringBuilder output = new StringBuilder();
        for (Function<Map<P, Object>, String> token : tokens) {
            output.append(token.apply(values));
        }
        return output.toString();
    }

    public final static class MapToString<P> implements Function<Map<P, Object>, String> {

        private final P key;

        public MapToString(P key) {
            this.key = key;
        }

        @Override
        public String apply(Map<P, Object> values) {
            final Object o = values.get(key);
            return o == null ? "" : o.toString();
        }
    }

    public final static class ProviderToString<P> implements Function<Map<P, Object>, String> {

        private final Function<Map<P, Object>, ?> provider;

        public ProviderToString(Function<Map<P, Object>, ?> provider) {
            this.provider = provider;
        }

        @Override
        public String apply(Map<P, Object> pObjectMap) {
            return String.valueOf(provider);
        }
    }

    public final static class ConditionToString<P> implements Function<Map<P, Object>, String> {

        private final Condition<Map<P, Object>> condition;

        public ConditionToString(Condition<Map<P, Object>> condition) {
            this.condition = condition;
        }

        @Override
        public String apply(Map<P, Object> values) {
            return String.valueOf(condition.test(values));
        }

    }

    public final static class CalculationToString<P> implements Function<Map<P, Object>, String> {

        private final Calculation<Map<P, Object>> calculation;

        public CalculationToString(Calculation<Map<P, Object>> calculation) {
            this.calculation = calculation;
        }

        @Override
        public String apply(Map<P, Object> values) {
            return String.valueOf(calculation.apply(values));
        }

    }

}
