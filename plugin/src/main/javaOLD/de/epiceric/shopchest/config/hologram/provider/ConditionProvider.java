package de.epiceric.shopchest.config.hologram.provider;

import de.epiceric.shopchest.config.hologram.condition.Condition;

import java.util.Objects;
import java.util.function.Function;

public class ConditionProvider<P> implements Function<P, Boolean> {

    private final Condition<P> condition;

    public ConditionProvider(Condition<P> condition) {
        this.condition = condition;
    }

    @Override
    public Boolean apply(P values) {
        return condition.test(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConditionProvider<?> that = (ConditionProvider<?>) o;
        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition);
    }

    @Override
    public String toString() {
        return condition.toString();
    }
}
