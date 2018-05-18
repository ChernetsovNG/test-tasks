package ru.nchernetsov.test.sbertech.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.client.Client;
import ru.nchernetsov.test.sbertech.common.message.AnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.DemandMessage;

public class MethodInvokeController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodInvokeController.class);

    private Client model;

    public void setModel(Client model) {
        this.model = model;
    }

    public void returnResult(DemandMessage demandMessage, AnswerMessage answerMessage) {
        model.unblockLatchAndGetResult(demandMessage.getUuid(), answerMessage.getMethodInvokeStatus(), answerMessage.getResult());
    }
}
