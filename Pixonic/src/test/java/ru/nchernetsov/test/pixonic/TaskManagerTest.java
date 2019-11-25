package ru.nchernetsov.test.pixonic;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Queue;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskManagerTest {

    @Test
    public void tasksShouldBeOrderedByTime() {
        TaskManager taskManager = new TaskManagerImpl();

        // добавляем задачи в неправильном порядке по времени
        UUID clientUUID = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();
        Task<Void> task1 = new Task<>(clientUUID, now.plus(100L, ChronoUnit.MILLIS), () -> null);
        Task<Void> task2 = new Task<>(clientUUID, now.plus(200L, ChronoUnit.MILLIS), () -> null);
        Task<Void> task3 = new Task<>(clientUUID, now.plus(300L, ChronoUnit.MILLIS), () -> null);

        UUID task1Uuid = task1.getUuid();
        UUID task2Uuid = task2.getUuid();
        UUID task3Uuid = task3.getUuid();

        taskManager.scheduleTask(task1);
        taskManager.scheduleTask(task2);
        taskManager.scheduleTask(task3);

        // После планирования задачи должны быть упорядочены таким образом, чтобы в конце списка
        // была задача, которая должна быть выполнена первой
        Queue<UUID> scheduledTasks = taskManager.getScheduledTasks();

        assertThat(scheduledTasks).hasSize(3);
        assertThat(scheduledTasks.poll()).isEqualTo(task1Uuid);
        assertThat(scheduledTasks.poll()).isEqualTo(task2Uuid);
        assertThat(scheduledTasks.poll()).isEqualTo(task3Uuid);
    }

    @Test
    public void tasksShouldBeOrderedByTimeAndByOrderOfAdditionForSameTime() {
        TaskManager taskManager = new TaskManagerImpl();

        // Для одинакового времени планирования задачи должны быть упорядочены по времени выполнения
        UUID clientUUID = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();
        Task<Void> task1 = new Task<>(clientUUID, now.plus(100L, ChronoUnit.MILLIS), () -> null);
        Task<Void> task2 = new Task<>(clientUUID, now.plus(200L, ChronoUnit.MILLIS), () -> null);
        Task<Void> task31 = new Task<>(clientUUID, now.plus(300L, ChronoUnit.MILLIS), () -> null);
        Task<Void> task32 = new Task<>(clientUUID, now.plus(300L, ChronoUnit.MILLIS), () -> null);
        Task<Void> task33 = new Task<>(clientUUID, now.plus(300L, ChronoUnit.MILLIS), () -> null);

        UUID task1Uuid = task1.getUuid();
        UUID task2Uuid = task2.getUuid();
        UUID task31Uuid = task31.getUuid();
        UUID task32Uuid = task32.getUuid();
        UUID task33Uuid = task33.getUuid();

        taskManager.scheduleTask(task1);
        taskManager.scheduleTask(task2);
        taskManager.scheduleTask(task31);
        taskManager.scheduleTask(task32);
        taskManager.scheduleTask(task33);

        // После планирования задачи должны быть упорядочены таким образом, чтобы в конце списка
        // была задача, которая должна быть выполнена первой. Из одинаковых по времени задач первой должна выполниться
        // задача, которая была первой добавлена
        Queue<UUID> scheduledTasks = taskManager.getScheduledTasks();

        assertThat(scheduledTasks).hasSize(5);
        assertThat(scheduledTasks.poll()).isEqualTo(task1Uuid);
        assertThat(scheduledTasks.poll()).isEqualTo(task2Uuid);
        assertThat(scheduledTasks.poll()).isEqualTo(task31Uuid);
        assertThat(scheduledTasks.poll()).isEqualTo(task32Uuid);
        assertThat(scheduledTasks.poll()).isEqualTo(task33Uuid);
    }
}
