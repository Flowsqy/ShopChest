package de.epiceric.shopchest.config.hologram.condition;

import java.util.Objects;

public abstract class LogicCondition<P> implements Condition<P> {

    protected final Condition<P> firstCondition;
    protected final Condition<P> secondCondition;

    public LogicCondition(Condition<P> firstCondition, Condition<P> secondCondition) {
        this.firstCondition = firstCondition;
        this.secondCondition = secondCondition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogicCondition<?> that = (LogicCondition<?>) o;
        return Objects.equals(firstCondition, that.firstCondition) && Objects.equals(secondCondition, that.secondCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstCondition, secondCondition);
    }

    public final static class AndCondition<P> extends LogicCondition<P> {

        public AndCondition(Condition<P> firstCondition, Condition<P> secondCondition) {
            super(firstCondition, secondCondition);
        }

        @Override
        public boolean test(P values) {
            return firstCondition.test(values) && secondCondition.test(values);
        }

        @Override
        public String toString() {
            return "(" + firstCondition.toString() + " && " + secondCondition.toString() + ")";
        }
    }

    public final static class OrCondition<P> extends LogicCondition<P> {

        public OrCondition(Condition<P> firstCondition, Condition<P> secondCondition) {
            super(firstCondition, secondCondition);
        }

        @Override
        public boolean test(P values) {
            return firstCondition.test(values) || secondCondition.test(values);
        }

        @Override
        public String toString() {
            return "(" + firstCondition.toString() + " || " + secondCondition.toString() + ")";
        }
    }

}
