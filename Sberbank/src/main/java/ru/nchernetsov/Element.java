package ru.nchernetsov;

public class Element<T> {

    private final int x;

    private final int y;

    private final T value;

    public Element(int x, int y, T value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public T getValue() {
        return value;
    }
}
