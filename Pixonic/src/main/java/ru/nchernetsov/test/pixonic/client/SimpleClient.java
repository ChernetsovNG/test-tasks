package ru.nchernetsov.test.pixonic.client;

import ru.nchernetsov.test.pixonic.Result;
import ru.nchernetsov.test.pixonic.TaskManager;

public class SimpleClient extends Client {

    private Object result;

    public SimpleClient(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public <V> void onResult(Result<V> result) {
        this.result = result.getResult();
    }

    public Object getResult() {
        return result;
    }
}
