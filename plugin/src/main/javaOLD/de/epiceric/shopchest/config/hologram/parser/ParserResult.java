package de.epiceric.shopchest.config.hologram.parser;

import de.epiceric.shopchest.config.hologram.calculation.Calculation;
import de.epiceric.shopchest.config.hologram.condition.Condition;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ParserResult<P> {

    private final Condition<Map<P, Object>> condition;
    private final Calculation<Map<P, Object>> calculation;
    private final Function<Map<P, Object>, ?> value;
    private final Object constant;

    public ParserResult(Condition<Map<P, Object>> condition, Calculation<Map<P, Object>> calculation, Function<Map<P, Object>, ?> value, Object constant) {
        this.condition = condition;
        this.calculation = calculation;
        this.value = value;
        this.constant = constant;
    }

    public Condition<Map<P, Object>> getCondition() {
        return condition;
    }

    public Calculation<Map<P, Object>> getCalculation() {
        return calculation;
    }

    public Function<Map<P, Object>, ?> getValue() {
        return value;
    }

    public Object getConstant() {
        return constant;
    }

    public boolean isCondition() {
        return condition != null;
    }

    public boolean isCalculation() {
        return calculation != null;
    }

    public boolean isValue() {
        return value != null;
    }

    public boolean isConstant() {
        return constant != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParserResult<?> that = (ParserResult<?>) o;
        return Objects.equals(condition, that.condition) && Objects.equals(calculation, that.calculation) && Objects.equals(value, that.value) && Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, calculation, value, constant);
    }

    @Override
    public String toString() {
        return "ParserResult{" +
                "condition=" + condition +
                ", calculation=" + calculation +
                ", value=" + value +
                ", constant=" + constant +
                '}';
    }

}
