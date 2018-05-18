package ru.nchernetsov.test.sbertech.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.client.controller.MethodInvokeController;
import ru.nchernetsov.test.sbertech.common.message.AnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.DemandMessage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MethodInvokeAnswerHandlerImpl implements MethodInvokeAnswerHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MethodInvokeAnswerHandler.class);

    // сохраняем в карте запросы, чтобы понять, но что приходят ответы
    private final Map<UUID, DemandMessage> methodInvokeDemandMessages;
    private final MethodInvokeController methodInvokeController;

    public MethodInvokeAnswerHandlerImpl(MethodInvokeController methodInvokeController) {
        methodInvokeDemandMessages = new ConcurrentHashMap<>();
        this.methodInvokeController = methodInvokeController;
    }

    @Override
    public void handleMessage(AnswerMessage answerMessage) {
        LOG.info("Получен ответ о вызове remote метода от сервера. Message: {}", answerMessage);
        try {
            UUID answerOnDemand = answerMessage.getToMessage();
            if (methodInvokeDemandMessages.containsKey(answerOnDemand)) {
                DemandMessage demandMessage = methodInvokeDemandMessages.get(answerOnDemand);
                methodInvokeController.returnResult(demandMessage, answerMessage);
                methodInvokeDemandMessages.remove(answerOnDemand);  // после обработки ответа на запрос удаляем запрос
            } else {
                LOG.warn("Пришёл ответ не на наш запрос");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void addDemandMessage(DemandMessage message) {
        methodInvokeDemandMessages.put(message.getUuid(), message);
    }

}
