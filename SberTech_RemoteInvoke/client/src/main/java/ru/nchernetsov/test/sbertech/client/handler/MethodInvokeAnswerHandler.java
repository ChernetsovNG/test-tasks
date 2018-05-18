package ru.nchernetsov.test.sbertech.client.handler;

import ru.nchernetsov.test.sbertech.common.message.AnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.DemandMessage;

public interface MethodInvokeAnswerHandler {
    void handleMessage(AnswerMessage message);

    void addDemandMessage(DemandMessage message);
}
