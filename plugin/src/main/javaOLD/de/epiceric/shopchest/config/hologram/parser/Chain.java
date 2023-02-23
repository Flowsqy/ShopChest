package de.epiceric.shopchest.config.hologram.parser;

import java.util.Iterator;

public class Chain<T> {

    private Chain<T> before;
    private Chain<T> after;
    private T value;

    public Chain(T value) {
        this.value = value;
    }

    public Chain(Chain<T> before, Chain<T> after, T value) {
        this.before = before;
        this.after = after;
        this.value = value;
    }

    public static <T> Chain<T> getChain(Iterable<T> iterable) {
        Chain<T> first = null;
        Chain<T> previous = null;
        final Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            first = previous = new Chain<>(iterator.next());
        }
        while (iterator.hasNext()) {
            final Chain<T> chain = new Chain<>(previous, null, iterator.next());
            previous.setAfter(chain);
            previous = chain;
        }
        return first;
    }

    public Chain<T> getBefore() {
        return before;
    }

    public void setBefore(Chain<T> before) {
        this.before = before;
    }

    public Chain<T> getAfter() {
        return after;
    }

    public void setAfter(Chain<T> after) {
        this.after = after;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}
