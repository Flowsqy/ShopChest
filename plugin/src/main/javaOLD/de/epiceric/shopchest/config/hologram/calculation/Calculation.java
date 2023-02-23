package de.epiceric.shopchest.config.hologram.calculation;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a hologram calculation
 */
@FunctionalInterface
public interface Calculation<P> extends Function<P, Double> {

    Double apply(P provider);

    abstract class AbstractCalculation<P> implements Calculation<P> {

        protected final Function<P, Double> firstArgProvider;
        protected final Function<P, Double> secondArgProvider;

        public AbstractCalculation(
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
            AbstractCalculation<?> that = (AbstractCalculation<?>) o;
            return Objects.equals(firstArgProvider, that.firstArgProvider) && Objects.equals(secondArgProvider, that.secondArgProvider);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstArgProvider, secondArgProvider);
        }

    }

    class Addition<P> extends AbstractCalculation<P> {

        public Addition(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public Double apply(P values) {
            return this.firstArgProvider.apply(values) + secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " + " + secondArgProvider.toString() + ")";
        }
    }

    class Subtraction<P> extends AbstractCalculation<P> {

        public Subtraction(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public Double apply(P values) {
            return this.firstArgProvider.apply(values) - secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " - " + secondArgProvider.toString() + ")";
        }
    }

    class Multiplication<P> extends AbstractCalculation<P> {

        public Multiplication(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public Double apply(P values) {
            return this.firstArgProvider.apply(values) * secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " * " + secondArgProvider.toString() + ")";
        }
    }

    class Division<P> extends AbstractCalculation<P> {

        public Division(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public Double apply(P values) {
            return this.firstArgProvider.apply(values) / secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " / " + secondArgProvider.toString() + ")";
        }
    }

    class Modulo<P> extends AbstractCalculation<P> {

        public Modulo(
                Function<P, Double> firstArgProvider,
                Function<P, Double> secondArgProvider
        ) {
            super(firstArgProvider, secondArgProvider);
        }

        @Override
        public Double apply(P values) {
            return this.firstArgProvider.apply(values) % secondArgProvider.apply(values);
        }

        @Override
        public String toString() {
            return "(" + firstArgProvider.toString() + " % " + secondArgProvider.toString() + ")";
        }
    }

}
