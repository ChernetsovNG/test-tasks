package ru.nchernetsov.test.pixonic.task;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Задача для исполнения
 *
 * @param <V> тип возвращаемого значения
 */
public class Task<V> implements Comparable<Task<V>> {

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

    private long seqNum;

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

    public void setSeqNum(long seqNum) {
        this.seqNum = seqNum;
    }

    @Override
    public int compareTo(Task<V> other) {
        int res = this.time.compareTo(other.time);
        if (res == 0 && other != this) {
            res = seqNum < other.seqNum ? -1 : 1;
        }
        return res;
    }

    @Override
    public String toString() {
        return "Task{" +
                "uuid=" + uuid +
                ", seqNum=" + seqNum +
                ", clientUuid=" + clientUuid +
                ", time=" + time +
                '}';
    }
}
