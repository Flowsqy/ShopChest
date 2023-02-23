package de.epiceric.shopchest.config.hologram.condition;

import java.util.Objects;
import java.util.function.Function;

public class BooleanCondition<P> implements Condition<P> {

    private final Function<P, Boolean> booleanProvider;

    public BooleanCondition(Function<P, Boolean> booleanProvider) {
        this.booleanProvider = booleanProvider;
    }

    @Override
    public boolean test(P values) {
        return booleanProvider.apply(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanCondition<?> that = (BooleanCondition<?>) o;
        return Objects.equals(booleanProvider, that.booleanProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booleanProvider);
    }

    @Override
    public String toString() {
        return booleanProvider.toString();
    }
}
