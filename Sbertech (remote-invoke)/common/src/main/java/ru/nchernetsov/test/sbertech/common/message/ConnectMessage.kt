package ru.nchernetsov.test.sbertech.common.message;

import ru.nchernetsov.test.sbertech.common.dto.ConnectOperation
import ru.nchernetsov.test.sbertech.common.dto.ConnectStatus
import java.util.*

/**
 * Сообщения, касающиеся операций соединения с сервером
 */
class ConnectOperationMessage(from: Address, to: Address, val connectOperation: ConnectOperation, val additionalObject: Any?)
    : Message(from, to, ConnectOperationMessage::class.java)

class ConnectAnswerMessage(from: Address, to: Address, val toMessage: UUID, val connectStatus: ConnectStatus, val additionalMessage: String?)
    : Message(from, to, ConnectAnswerMessage::class.java)
