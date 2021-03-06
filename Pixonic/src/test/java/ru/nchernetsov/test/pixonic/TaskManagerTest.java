package ru.nchernetsov.test.pixonic;

import org.junit.Test;
import ru.nchernetsov.test.pixonic.manager.TaskManager;
import ru.nchernetsov.test.pixonic.manager.TaskManagerImpl;
import ru.nchernetsov.test.pixonic.task.PoisonPillTask;
import ru.nchernetsov.test.pixonic.task.Task;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        taskManager.scheduleTask(task3);
        taskManager.scheduleTask(task2);
        taskManager.scheduleTask(task1);

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

        taskManager.scheduleTask(task31);
        taskManager.scheduleTask(task2);
        taskManager.scheduleTask(task32);
        taskManager.scheduleTask(task1);
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

    @Test
    public void tasksShouldBeCompletedByTimeOrder() {
        TaskManagerImpl taskManager = new TaskManagerImpl();

        // добавляем задачи в неправильном порядке по времени
        UUID clientUUID = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();
        Task<Integer> task1 = new Task<>(clientUUID, now.plus(100L, ChronoUnit.MILLIS), () -> 100);
        Task<Integer> task2 = new Task<>(clientUUID, now.plus(200L, ChronoUnit.MILLIS), () -> 200);
        Task<Integer> task3 = new Task<>(clientUUID, now.plus(300L, ChronoUnit.MILLIS), () -> 300);

        taskManager.scheduleTask(task1);
        taskManager.scheduleTask(task2);
        taskManager.scheduleTask(task3);

        // запускаем исполнение задач
        taskManager.executionLoop();

        try {
            TimeUnit.MILLISECONDS.sleep(350);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // проверяем, что задачи выполнены в нужном порядке
        Queue<Object> results = taskManager.getResults();

        assertThat(results).hasSize(3);
        assertThat(results.poll()).isEqualTo(100);
        assertThat(results.poll()).isEqualTo(200);
        assertThat(results.poll()).isEqualTo(300);

        // останавливаем цикл выполнения задач
        taskManager.scheduleTask(new PoisonPillTask());
    }

    @Test
    public void normalTaskCountShouldBeAllScheduled() {
        TaskManager taskManager = new TaskManagerImpl(10, 10);
        UUID clientUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // планируем на выполнение 10 задач => все они должны успешно запланироваться
        List<Boolean> scheduleResult = IntStream.iterate(0, i -> ++i)
                .limit(10)
                .mapToObj(i -> {
                    Task<Void> task = new Task<>(clientUuid, now, () -> null);
                    return taskManager.scheduleTask(task);
                })
                .collect(Collectors.toList());

        assertThat(scheduleResult).hasSize(10);
        assertThat(scheduleResult).containsOnly(true);
    }

    @Test
    public void tooMuchTaskCountShouldNotBeAllScheduled() {
        TaskManager taskManager = new TaskManagerImpl(10, 10);
        UUID clientUuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // планируем на выполнение 100 задач => не все они должны успешно запланироваться
        List<Boolean> scheduleResult = IntStream.iterate(0, i -> ++i)
                .limit(100)
                .mapToObj(i -> {
                    Task<Void> task = new Task<>(clientUuid, now, () -> null);
                    return taskManager.scheduleTask(task);
                })
                .collect(Collectors.toList());

        assertThat(scheduleResult).hasSize(100);
        assertThat(scheduleResult).containsOnly(true, false);
    }
}
