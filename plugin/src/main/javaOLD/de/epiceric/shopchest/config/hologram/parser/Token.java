package de.epiceric.shopchest.config.hologram.parser;

import de.epiceric.shopchest.config.hologram.calculation.Calculation;
import de.epiceric.shopchest.config.hologram.condition.Condition;

import java.util.List;

public class Token<T> {

    public final static TokenType<String> STRING = new TokenType<>("String");
    public final static TokenType<Double> DOUBLE = new TokenType<>("Double");
    public final static TokenType<String> VALUE = new TokenType<>("Value");
    public final static TokenType<Void> BEGIN_UNIT = new TokenType<>("Begin unit");
    public final static TokenType<Void> END_UNIT = new TokenType<>("End unit");
    public final static TokenType<Void> REVERSE = new TokenType<>("Reverse");
    public final static TokenType<ConditionOperator> CONDITION_OPERATOR = new TokenType<>("Condition operator");
    public final static TokenType<LogicOperator> LOGIC_OPERATOR = new TokenType<>("Logic operator");
    public final static TokenType<CalculationOperator> CALCULATION_OPERATOR = new TokenType<>("Calculation operator");
    public final static TokenType<List<Token<?>>> NODE = new TokenType<>("Node");
    public final static TokenType<Calculation<?>> CALCULATION = new TokenType<>("Calculation");
    public final static TokenType<Condition<?>> CONDITION = new TokenType<>("Condition");

    private final TokenType<T> type;
    private final T value;

    public Token(TokenType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public TokenType<T> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }

    public enum ConditionOperator {
        EQUAL, NOT_EQUAL, GREATER, GREATER_OR_EQUAL, LESS, LESS_OR_EQUAL
    }

    public enum LogicOperator {
        OR, AND
    }

    public enum CalculationOperator {
        ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MODULO
    }

    public final static class TokenType<T> {

        private final String id;

        public TokenType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "TokenType{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
