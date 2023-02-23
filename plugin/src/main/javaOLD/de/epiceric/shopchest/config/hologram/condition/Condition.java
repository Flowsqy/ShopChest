package de.epiceric.shopchest.config.hologram.condition;

import java.util.function.Predicate;

/**
 * Represents a hologram requirement condition
 */
@FunctionalInterface
public interface Condition<P> extends Predicate<P> {

}
