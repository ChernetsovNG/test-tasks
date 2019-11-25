package ru.nchernetsov.test.pixonic;

public abstract class Client implements Subscriber {

    private final TaskManager taskManager;

    public Client(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
}
