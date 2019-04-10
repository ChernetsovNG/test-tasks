package ru.nchernetsov.test.yandex.test3.badDigest;

public class ConcreteDigest extends Digest {

    @Override
    protected byte[] doDigest(byte[] input) {
        byte[] output = new byte[1];
        byte sum = 0;
        for (byte b : input) {
            sum += b;
        }
        output[0] = sum;
        return output;
    }
}
