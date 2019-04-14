package ru.nchernetsov.test.yandex.test3.notSoBadDigest;

public class ConcreteDigestSum implements DigestWorkerService {

    @Override
    public byte[] doDigest(byte[] input) {
        byte[] output = new byte[1];
        byte sum = 0;
        for (byte b : input) {
            sum += b;
        }
        output[0] = sum;
        return output;
    }
}
