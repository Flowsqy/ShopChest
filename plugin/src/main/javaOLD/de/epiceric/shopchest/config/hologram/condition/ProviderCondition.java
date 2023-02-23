package de.epiceric.shopchest.config.hologram.condition;

import java.util.function.Function;

public class ProviderCondition<P> implements Condition<P> {

    private final Function<P, Boolean> booleanProvider;

    public ProviderCondition(Function<P, Boolean> booleanProvider) {
        this.booleanProvider = booleanProvider;
    }

    @Override
    public boolean test(P values) {
        return booleanProvider.apply(values);
    }

    @Override
    public String toString() {
        return booleanProvider.toString();
    }
}
