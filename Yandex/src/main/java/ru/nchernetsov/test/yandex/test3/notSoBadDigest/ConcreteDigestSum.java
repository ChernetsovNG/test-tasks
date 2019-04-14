package ru.nchernetsov.test.yandex.test3.notSoBadDigest;

public class ConcreteDigestSum implements DigestWorkerService {

    @Override
    public byte[] doDigest(byte[] input) {
        byte sum = 0;
        for (byte b : input) {
            sum += b;
        }
        return new byte[]{sum};
    }
}
