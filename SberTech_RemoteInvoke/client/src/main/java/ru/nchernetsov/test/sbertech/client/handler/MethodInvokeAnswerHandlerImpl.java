package ru.nchernetsov.test.sbertech.client.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.client.Client;
import ru.nchernetsov.test.sbertech.common.message.MethodInvokeAnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.MethodInvokeDemandMessage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MethodInvokeAnswerHandlerImpl implements MethodInvokeAnswerHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MethodInvokeAnswerHandler.class);

    // сохраняем в карте запросы, чтобы понять, но что приходят ответы
    private final Map<UUID, MethodInvokeDemandMessage> methodInvokeDemandMessages = new ConcurrentHashMap<>();
    private final Client client;

    public MethodInvokeAnswerHandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public void handleMessage(MethodInvokeAnswerMessage answerMessage) {
        LOG.info("Получен ответ о вызове remote метода от сервера. Message: {}", answerMessage);
        try {
            UUID answerOnDemand = answerMessage.getToMessage();
            if (methodInvokeDemandMessages.containsKey(answerOnDemand)) {  // если это ответ для нас
                MethodInvokeDemandMessage demandMessage = methodInvokeDemandMessages.get(answerOnDemand);
                client.unblockMessageLatch(demandMessage.getUuid(), answerMessage.getMethodInvokeStatus(), answerMessage.getResult());
                methodInvokeDemandMessages.remove(answerOnDemand);  // после обработки ответа на запрос удаляем запрос
            } else {
                LOG.warn("Пришёл ответ не на наш запрос");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void addDemandMessage(MethodInvokeDemandMessage message) {
        methodInvokeDemandMessages.put(message.getUuid(), message);
    }

}
