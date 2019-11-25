package ru.nchernetsov.test.pixonic;

import org.junit.Test;
import ru.nchernetsov.test.pixonic.client.SimpleClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    @Test
    public void clientShouldReturnResultInFutureTest1() {
        long deltaMillis = 200L;

        TaskManager taskManager = new TaskManagerImpl();
        SimpleClient client = new SimpleClient(taskManager);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime performTaskTime = now.plus(deltaMillis, ChronoUnit.MILLIS);  // через 200 миллисекунд

        Task<Integer> task = new Task<>(client.getUuid(), performTaskTime, () -> 42);
        client.scheduleTask(task);

        assertThat(client.getResult()).isNull();

        try {
            TimeUnit.MILLISECONDS.sleep(deltaMillis + 50L);  // ждём 250 миллисекунд до получения результата
            Object result = client.getResult();
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(Integer.class);
            assertThat((Integer) result).isEqualTo(42);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
