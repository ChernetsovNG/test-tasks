package ru.nchernetsov.test.yandex.test3.notSoBadDigest;

import java.util.Map;

public class DigestService {

    private final DigestWorkerService digestWorkerService;

    public DigestService(DigestWorkerService digestWorkerService) {
        this.digestWorkerService = digestWorkerService;
    }

    public byte[] digest(byte[] input) {
        ByteArrayKey inputKey = new ByteArrayKey(input);
        Map<ByteArrayKey, byte[]> cache = DigestCache.getInstance();
        byte[] result = cache.get(inputKey);
        if (result == null) {
            result = digestWorkerService.doDigest(input);
            cache.putIfAbsent(inputKey, result);
        }
        return result;
    }
}
