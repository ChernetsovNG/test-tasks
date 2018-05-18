package ru.nchernetsov.test.sbertech.client.handler;

import ru.nchernetsov.test.sbertech.common.message.MethodInvokeAnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.MethodInvokeDemandMessage;

public interface MethodInvokeAnswerHandler {
    void handleMessage(MethodInvokeAnswerMessage message);

    void addDemandMessage(MethodInvokeDemandMessage message);
}
