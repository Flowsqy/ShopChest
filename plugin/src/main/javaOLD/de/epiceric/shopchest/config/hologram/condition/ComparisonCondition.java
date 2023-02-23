package de.epiceric.shopchest.config.hologram.condition;

import java.util.Objects;
import java.util.function.Function;

public abstract class ComparisonCondition<P> implements Condition<P> {

    protected final Function<P, Double> firstArgProvider;
    protected final Function<P, Double> secondArgProvider;

    public ComparisonCondition(
            Function<P, Double> firstArgProvider,
            Function<P, Double> secondArgProvider
    ) {
        this.firstArgProvider = firstArgProvider;
        this.secondArgProvider = secondArgProvider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparisonCondition<?> that = (ComparisonCondition<?>) o;
        return Objects.equals(firstArgProvider, that.firstArgProvider) && Objects.equals(secondArgProvider, that.secondArgProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstArgProvider, secondArgProvider);
    }

    public static class GreaterCondition<P> extends ComparisonCondition<P> {

        public GreaterCondition(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(P values) {
            return firstArgProvider.apply(values) > secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " > " + secondArgProvider.toString() + ")";
        }

    }

    public static class LessCondition<P> extends ComparisonCondition<P> {

        public LessCondition(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(P values) {
            return firstArgProvider.apply(values) < secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " < " + secondArgProvider.toString() + ")";
        }

    }

    public static class GreaterOrEqualCondition<P> extends ComparisonCondition<P> {

        public GreaterOrEqualCondition(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(P values) {
            return firstArgProvider.apply(values) >= secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " >= " + secondArgProvider.toString() + ")";
        }

    }

    public static class LessOrEqualCondition<P> extends ComparisonCondition<P> {

        public LessOrEqualCondition(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public boolean test(P values) {
            return firstArgProvider.apply(values) <= secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " <= " + secondArgProvider.toString() + ")";
        }

    }

}
