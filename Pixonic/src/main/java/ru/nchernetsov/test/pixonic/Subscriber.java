package ru.nchernetsov.test.pixonic;

import java.util.UUID;

public interface Subscriber {

    UUID getUuid();

    <V> void onResult(Result<V> result);
}
