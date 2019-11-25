package ru.nchernetsov.test.pixonic.client;

import ru.nchernetsov.test.pixonic.manager.TaskManager;
import ru.nchernetsov.test.pixonic.task.Result;

public class SimpleClient extends Client<Object> {

    private Object result;

    public SimpleClient(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void onResult(Result<Object> result) {
        this.result = result.getResult();
    }

    public Object getResult() {
        return result;
    }
}
