package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;
import ru.nchernetsov.test.sbertech.common.enums.ConnectStatus;

import java.util.UUID;

/**
 * Ответ на запрос об установлении соединения
 */
@Getter
@ToString
public class ConnectAnswerMessage extends Message {

    private final UUID toMessage;
    private final ConnectStatus connectStatus;

    public ConnectAnswerMessage(Address from, Address to, UUID toMessage, ConnectStatus connectStatus) {
        super(from, to, ConnectAnswerMessage.class);
        this.toMessage = toMessage;
        this.connectStatus = connectStatus;
    }

}
