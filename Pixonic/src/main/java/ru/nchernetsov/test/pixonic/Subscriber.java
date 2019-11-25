package ru.nchernetsov.test.pixonic;

public interface Subscriber {

    <V> void onResult(Result<V> result);
}
