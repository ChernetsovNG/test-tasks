package ru.nchernetsov.test.yandex.test3.notSoBadDigest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DigestCache {

    private static Map<ByteArrayKey, byte[]> cache = new ConcurrentHashMap<>();

    public static Map<ByteArrayKey, byte[]> getInstance() {
        return cache;
    }
}
