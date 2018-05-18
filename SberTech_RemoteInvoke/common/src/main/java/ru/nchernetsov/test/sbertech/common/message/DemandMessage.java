package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DemandMessage extends Message {

    private final String serviceName;
    private final String methodName;
    private final Object[] methodParams;

    public DemandMessage(Address from, Address to, String serviceName, String methodName, Object[] methodParams) {
        super(from, to, DemandMessage.class);
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.methodParams = methodParams;
    }

}
