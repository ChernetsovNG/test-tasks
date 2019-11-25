package ru.nchernetsov.test.pixonic.client;

import ru.nchernetsov.test.pixonic.Subscriber;
import ru.nchernetsov.test.pixonic.Task;
import ru.nchernetsov.test.pixonic.TaskManager;

import java.util.UUID;

public abstract class Client implements Subscriber {

    private final UUID uuid = UUID.randomUUID();

    private final TaskManager taskManager;

    public Client(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public <V> void scheduleTask(Task<V> task) {
        taskManager.scheduleTask(task);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }
}
