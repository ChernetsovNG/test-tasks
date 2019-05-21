package ru.nchernetsov.test.yandex.test3.notSoBadDigest;

import java.util.Arrays;

public class ByteArrayKey {

    private final byte[] key;

    public ByteArrayKey(byte[] key) {
        this.key = Arrays.copyOf(key, key.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteArrayKey that = (ByteArrayKey) o;

        return Arrays.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    @Override
    public String toString() {
        return "ByteArrayKey{" +
            "key=" + Arrays.toString(key) +
            '}';
    }
}
