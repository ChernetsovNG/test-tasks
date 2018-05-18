package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;

/**
 * Запрос вызова метода
 */
@Getter
@ToString
public class MethodInvokeDemandMessage extends Message {

    private final String serviceName;
    private final String methodName;
    private final Object[] methodParams;

    public MethodInvokeDemandMessage(Address from, Address to, String serviceName, String methodName, Object[] methodParams) {
        super(from, to, MethodInvokeDemandMessage.class);
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.methodParams = methodParams;
    }

}
