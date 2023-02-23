package de.epiceric.shopchest.config.hologram.condition;

import java.util.Objects;

public class ReverseCondition<P> implements Condition<P> {

    private final Condition<P> condition;

    public ReverseCondition(Condition<P> condition) {
        this.condition = condition;
    }

    @Override
    public boolean test(P values) {
        return !condition.test(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReverseCondition<?> that = (ReverseCondition<?>) o;
        return Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition);
    }

    @Override
    public String toString() {
        return "(!" + condition.toString() + ")";
    }
}
