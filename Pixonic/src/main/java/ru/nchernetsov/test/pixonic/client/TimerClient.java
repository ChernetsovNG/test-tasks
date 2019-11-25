package ru.nchernetsov.test.pixonic.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.pixonic.manager.TaskManager;
import ru.nchernetsov.test.pixonic.task.Result;

public class TimerClient extends Client<String> {

    private static final Logger log = LoggerFactory.getLogger(TimerClient.class);

    public TimerClient(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void onResult(Result<String> result) {
        log.debug("time = {}", result.getResult());
    }
}
