package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;
import ru.nchernetsov.test.sbertech.common.enums.ConnectStatus;

import java.util.UUID;

@Getter
@ToString
public class ConnectAnswerMessage extends Message {

    private final UUID toMessage;
    private final ConnectStatus connectStatus;
    private final String additionalMessage;

    public ConnectAnswerMessage(Address from, Address to, UUID toMessage, ConnectStatus connectStatus, String additionalMessage) {
        super(from, to, ConnectAnswerMessage.class);
        this.toMessage = toMessage;
        this.connectStatus = connectStatus;
        this.additionalMessage = additionalMessage;


    }
}
