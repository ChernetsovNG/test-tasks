package ru.nchernetsov.test.sbertech.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.message.Address;
import ru.nchernetsov.test.sbertech.common.message.AnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.DemandMessage;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

import static ru.nchernetsov.test.sbertech.common.CommonData.SERVER_ADDRESS;
import static ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus.*;
import static ru.nchernetsov.test.sbertech.server.ReflectionHelper.callMethod;

public class DemandHandlerImpl implements DemandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DemandHandler.class);

    private final ConnectDemandHandler connectDemandHandler;
    private final Map<String, Object> services;

    public DemandHandlerImpl(ConnectDemandHandler connectDemandHandler, Map<String, Object> services) {
        this.connectDemandHandler = connectDemandHandler;
        this.services = services;
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
        LOG.info("Запрос на выполнение метода от клиента: {}. Сервис: {}, метод: {}, параметры: {}",
            clientAddress, serviceName, methodName, methodParams);

        // находим требуемый сервис
        Object service = services.get(serviceName);

        AnswerMessage answerMessage;
        if (service == null) {
            answerMessage = new AnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, SERVICE_NOT_EXISTS, null);
        } else {
            try {
                Object methodResult = callMethod(service, methodName, methodParams);
                if (methodResult == null) {  // если вызывается метод с типом возвращаемого значения void
                    answerMessage = new AnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, OK_VOID, null);
                } else {
                    answerMessage = new AnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, OK_RESULT, methodResult);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                answerMessage = new AnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, METHOD_INVOKE_EXCEPTION, null);
            } catch (NoSuchMethodException e) {
                answerMessage = new AnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, NO_SUCH_METHOD, null);
            }
        }
        clientChannel.send(answerMessage);
    }

}
