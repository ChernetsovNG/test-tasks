package ru.nchernetsov.test.yandex.test3.badDigest;

public class ConcreteDigestSum extends Digest {

    @Override
    protected byte[] doDigest(byte[] input) {
        byte sum = 0;
        for (byte b : input) {
            sum += b;
        }
        return new byte[]{sum};
    }
}
