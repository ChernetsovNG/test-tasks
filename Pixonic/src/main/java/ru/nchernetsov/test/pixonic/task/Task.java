package ru.nchernetsov.test.pixonic.task;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Задача для исполнения
 *
 * @param <V> тип возвращаемого значения
 */
public class Task<V> {

    private final UUID uuid = UUID.randomUUID();

    /**
     * Идентификатор клиента, создавшего данную задачу
     */
    private final UUID clientUuid;

    /**
     * Момент времени, в который нужно начать выполнение задача
     */
    private final LocalDateTime time;

    /**
     * Вычислительная задача, возвращающая значение типа V
     */
    private final Callable<V> task;

    public Task(UUID clientUuid, LocalDateTime time, Callable<V> task) {
        this.clientUuid = clientUuid;
        this.time = time;
        this.task = task;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getClientUuid() {
        return clientUuid;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Callable<V> getTask() {
        return task;
    }
}
