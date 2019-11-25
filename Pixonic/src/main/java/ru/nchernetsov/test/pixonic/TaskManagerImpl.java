package ru.nchernetsov.test.pixonic;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TaskManagerImpl implements TaskManager {

    @Override
    public <V> void scheduleTask(Task<V> task) {

    }

    @Override
    public List<UUID> getScheduledTasksList() {
        return Collections.emptyList();
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {

    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {

    }

    @Override
    public void notifySubscribers() {

    }
}
