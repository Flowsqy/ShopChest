package de.epiceric.shopchest.config.hologram.provider;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public abstract class MapProvider<P, T> implements Function<Map<P, Object>, T> {

    protected final P requirement;

    public MapProvider(P requirement) {
        this.requirement = requirement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapProvider<?, ?> that = (MapProvider<?, ?>) o;
        return requirement == that.requirement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirement);
    }

    @Override
    public String toString() {
        return requirement.toString();
    }

    public final static class StringMapProvider<P> extends MapProvider<P, String> {

        public StringMapProvider(P requirement) {
            super(requirement);
        }

        @Override
        public String apply(Map<P, Object> values) {
            return (String) values.get(requirement);
        }

    }

    public final static class BooleanMapProvider<P> extends MapProvider<P, Boolean> {

        public BooleanMapProvider(P requirement) {
            super(requirement);
        }

        @Override
        public Boolean apply(Map<P, Object> values) {
            return (Boolean) values.get(requirement);
        }

    }

    public final static class DoubleMapProvider<P> extends MapProvider<P, Double> {

        public DoubleMapProvider(P requirement) {
            super(requirement);
        }

        @Override
        public Double apply(Map<P, Object> values) {
            return ((Number) values.get(requirement)).doubleValue();
        }

    }

}
