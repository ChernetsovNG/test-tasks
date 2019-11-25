package ru.nchernetsov.test.pixonic.client;

import ru.nchernetsov.test.pixonic.manager.Subscriber;
import ru.nchernetsov.test.pixonic.manager.TaskManager;
import ru.nchernetsov.test.pixonic.task.Task;

import java.util.UUID;

public abstract class Client<V> implements Subscriber<V> {

    private final UUID uuid = UUID.randomUUID();

    private final TaskManager taskManager;

    public Client(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void scheduleTask(Task<V> task) {
        taskManager.scheduleTask(task);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }
}
