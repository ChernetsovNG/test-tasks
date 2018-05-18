package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;
import ru.nchernetsov.test.sbertech.common.enums.ConnectOperation;

/**
 * Запрос на установление соединения с сервером
 */
@Getter
@ToString
public class ConnectOperationMessage extends Message {

    private final ConnectOperation connectOperation;
    private final Object additionalObject;

    public ConnectOperationMessage(Address from, Address to, ConnectOperation connectOperation, Object additionalObject) {
        super(from, to, ConnectOperationMessage.class);
        this.connectOperation = connectOperation;
        this.additionalObject = additionalObject;
    }

}
