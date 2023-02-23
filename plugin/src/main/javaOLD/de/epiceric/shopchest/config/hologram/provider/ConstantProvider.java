package de.epiceric.shopchest.config.hologram.provider;

import java.util.Objects;
import java.util.function.Function;

public class ConstantProvider<P, T> implements Function<P, T> {

    private final T constant;

    public ConstantProvider(T constant) {
        this.constant = constant;
    }


    @Override
    public T apply(P values) {
        return constant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantProvider<?, ?> that = (ConstantProvider<?, ?>) o;
        return Objects.equals(constant, that.constant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constant);
    }

    @Override
    public String toString() {
        return constant.toString();
    }
}
