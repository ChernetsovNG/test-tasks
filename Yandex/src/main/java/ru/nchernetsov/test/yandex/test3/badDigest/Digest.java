package ru.nchernetsov.test.yandex.test3.badDigest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// У каждого объекта типа Digest будет свой кеш. Можно сделать singleton кеш
public abstract class Digest {

    // неверный тип ключа (т.к. для byte[] не определён метод equals) => кеш не работает
    // нет методов инвалидации кеша, проверки наличия значения в нём
    private Map<byte[], byte[]> cache = new HashMap<>();

    // метод non-final => мы можем его переопределить в наследнике
    public byte[] digest(byte[] input) {
        byte[] result = cache.get(input);
        if (result == null) {
            // синхронизация особо не нужна, если использовать ConcurrentHashMap
            // синхронизация на non-final поле, что не очень хорошо
            synchronized (cache) {
                result = cache.get(input);
                if (result == null) {
                    result = doDigest(input);
                    cache.put(input, result);
                }
            }
        }
        return result;
    }

    // Можно по ошибке вместо digest вызвать doDigest. Лучше сюда передавать
    // digestService, в котором определена логика преобразования массива byte[]
    // В этом смысле наследование не очень хорошо использовать, лучше применять
    // агрегацию (композицию)
    protected abstract byte[] doDigest(byte[] input);

    // просмотр состояния кеша
    public synchronized Map<byte[], byte[]> getCache() {
        return Collections.unmodifiableMap(cache);
    }
}
