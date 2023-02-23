package de.epiceric.shopchest.config.hologram.parser;

public class Counter {

    private int value;

    public Counter() {
        this(0);
    }

    public Counter(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }

    public Counter increment() {
        value++;
        return this;
    }

    public Counter decrement() {
        value--;
        return this;
    }

}
