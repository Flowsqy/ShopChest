package de.epiceric.shopchest.config.hologram.condition;

import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractEqualityCondition<P, T> implements Condition<P> {

    protected final Function<P, T> firstArgProvider;
    protected final Function<P, T> secondArgProvider;

    public AbstractEqualityCondition(
            Function<P, T> firstArgProvider,
            Function<P, T> secondArgProvider
    ) {
        this.firstArgProvider = firstArgProvider;
        this.secondArgProvider = secondArgProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEqualityCondition<?, ?> that = (AbstractEqualityCondition<?, ?>) o;
        return Objects.equals(firstArgProvider, that.firstArgProvider) && Objects.equals(secondArgProvider, that.secondArgProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstArgProvider, secondArgProvider);
    }

    public static class EqualityCondition<P, T> extends AbstractEqualityCondition<P, T> {

        public EqualityCondition(
                Function<P, T> firstArgProvider,
                Function<P, T> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(P values) {
            return Objects.equals(firstArgProvider.apply(values), secondArgProvider.apply(values));
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " == " + secondArgProvider.toString() + ")";
        }

    }

    public static class InequalityCondition<P, T> extends AbstractEqualityCondition<P, T> {

        public InequalityCondition(
                Function<P, T> firstArgProvider,
                Function<P, T> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(P values) {
            return !Objects.equals(firstArgProvider.apply(values), secondArgProvider.apply(values));
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " != " + secondArgProvider.toString() + ")";
        }

    }

}
