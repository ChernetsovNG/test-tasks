package ru.nchernetsov.test.sbertech.common.message;

import java.util.*

/**
 * Сообщения для удалённого вызова методов и получения ответов от сервера
 */
/**
 * Вызов метода. Передаём название сервиса, имя метода, список параметров метода
 */
class DemandMessage(from: Address, to: Address, val serviceName: String, val methodName: String,
                    val methodParams: Array<Any>) : Message(from, to, DemandMessage::class.java)

/**
 * Ответ: результат работы метода
 */
class AnswerMessage(from: Address, to: Address, val toMessage: UUID, val result: Any)
    : Message(from, to, AnswerMessage::class.java)
