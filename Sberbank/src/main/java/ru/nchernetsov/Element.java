package ru.nchernetsov;

public class Element {

    /**
     * координата столбца элемента в массиве, считая от 0 слева-направо
     */
    private final int x;

    /**
     * координата строки элементв в массиве, считая от 0 сверху-вниз
     */
    private final int y;

    public Element(int x, int y) {
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Element element = (Element) o;

        if (x != element.x) return false;
        return y == element.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
