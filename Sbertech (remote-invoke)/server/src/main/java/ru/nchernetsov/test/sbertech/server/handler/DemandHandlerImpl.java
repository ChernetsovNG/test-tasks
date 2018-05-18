package ru.nchernetsov.test.sbertech.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.message.Address;
import ru.nchernetsov.test.sbertech.common.message.AnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.DemandMessage;

import java.util.UUID;

import static ru.nchernetsov.test.sbertech.common.CommonData.SERVER_ADDRESS;

public class DemandHandlerImpl implements DemandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DemandHandler.class);

    private final ConnectDemandHandler connectDemandHandler;

    public DemandHandlerImpl(ConnectDemandHandler connectDemandHandler) {
        this.connectDemandHandler = connectDemandHandler;
    }

    @Override
    public void handleDemandMessage(Address clientAddress, MessageChannel clientChannel, DemandMessage message) {
        if (connectDemandHandler.getClientName(clientChannel).isPresent()) {
            UUID toMessage = message.getUuid();
            String serviceName = message.getServiceName();
            String methodName = message.getMethodName();
            Object[] methodParams = message.getMethodParams();

            handleDemand(clientAddress, clientChannel, serviceName, methodName, methodParams, toMessage);
        }
    }

    private void handleDemand(Address clientAddress, MessageChannel clientChannel, String serviceName,
                              String methodName, Object[] methodParams, UUID toMessage) {
        LOG.info("Запрос на выполнение метода от клиента: {}. Сервис: {}, методв: {}, параметры: {}",
            clientAddress, serviceName, methodName, methodParams);

        // выполняем remote метод
        AnswerMessage answerMessage = new AnswerMessage(SERVER_ADDRESS, clientAddress, toMessage,
            "Результат remote вызова!");

        clientChannel.send(answerMessage);
    }

}
