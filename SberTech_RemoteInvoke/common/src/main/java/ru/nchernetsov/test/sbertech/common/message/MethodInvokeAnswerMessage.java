package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;
import ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus;

import java.util.UUID;

/**
 * Сообщение с результатом вызова метода
 */
@Getter
@ToString
public class MethodInvokeAnswerMessage extends Message {

    private final UUID toMessage;
    private final MethodInvokeStatus methodInvokeStatus;
    private final Object result;

    public MethodInvokeAnswerMessage(Address from, Address to, UUID toMessage, MethodInvokeStatus methodInvokeStatus, Object result) {
        super(from, to, MethodInvokeAnswerMessage.class);
        this.toMessage = toMessage;
        this.methodInvokeStatus = methodInvokeStatus;
        this.result = result;
    }

}
