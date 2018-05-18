package ru.nchernetsov.test.sbertech.server

import kotlinx.coroutines.experimental.async
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.geekbrains.common.CommonData.SERVER_ADDRESS
import ru.geekbrains.common.CommonData.SERVER_PORT
import ru.geekbrains.common.channel.SocketClientManagedChannel
import ru.geekbrains.common.dto.ConnectOperation
import ru.geekbrains.common.dto.ConnectStatus
import ru.geekbrains.common.dto.UserDTO
import ru.geekbrains.common.message.Address
import ru.geekbrains.common.message.ConnectAnswerMessage
import ru.geekbrains.common.message.ConnectOperationMessage
import ru.geekbrains.server.db.Database.clearDatabase
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ConnectionToServerTest {
    companion object {
        lateinit var server: Server
        val clientNumber = AtomicInteger(0)
        val REGISTER_NAME = "test_client"
        val TEST_SERVER_PORT = SERVER_PORT + 2

        @BeforeAll
        @JvmStatic
        fun startServer() {
            // запускаем в отдельном потоке, чтобы не было блокировок
            async {
                server = Server()
                server.start("src/test/resources/test-data.db", TEST_SERVER_PORT)
            }
            TimeUnit.MILLISECONDS.sleep(1000)  // wait server initialization
        }

        @AfterAll
        @JvmStatic
        fun stopServer() {
            clearDatabase()
            TimeUnit.MILLISECONDS.sleep(500)
            server.stop()
        }
    }

    lateinit var client: SocketClientManagedChannel
    var clientAddress = Address("")

    // перед каждым тестом стартуем нового клиента и подключаемся к серверу
    @BeforeEach
    fun startNewClientAndHandshake() {
        clearDatabase()  // очищаем базу данных

        client = SocketClientManagedChannel("localhost", TEST_SERVER_PORT)
        client.init()

        // перед каждым тестом делаем handshake нового клиента на сервере
        clientAddress = Address("test-client-connection-" + clientNumber.addAndGet(1))

        val handshakeDemandMessage = ConnectOperationMessage(clientAddress, SERVER_ADDRESS, ConnectOperation.HANDSHAKE, null)
        client.send(handshakeDemandMessage)

        val handshakeAnswerMessage: ConnectAnswerMessage = client.take() as ConnectAnswerMessage

        assertEquals(handshakeDemandMessage.uuid, handshakeAnswerMessage.toMessage)     // проверяем, что ответ на наше сообщение
        assertEquals(ConnectStatus.HANDSHAKE_OK, handshakeAnswerMessage.connectStatus)

        // регистрируем нового клиента (добавляя его в базу данных)
        val registerNewClientMessage = ConnectOperationMessage(clientAddress, SERVER_ADDRESS, ConnectOperation.REGISTER, UserDTO(REGISTER_NAME, "qwerty"))
        client.send(registerNewClientMessage)

        val registerAnswerMessage: ConnectAnswerMessage = client.take() as ConnectAnswerMessage

        assertEquals(registerNewClientMessage.uuid, registerAnswerMessage.toMessage)
        assertEquals(ConnectStatus.REGISTER_OK, registerAnswerMessage.connectStatus)
    }

    // Тест аутентификации на сервере
    @Test
    fun authClientTest() {
        val authDemandMessage = ConnectOperationMessage(clientAddress, SERVER_ADDRESS, ConnectOperation.AUTH, UserDTO(REGISTER_NAME, "qwerty"))
        client.send(authDemandMessage)

        val authAnswerMessage: ConnectAnswerMessage = client.take() as ConnectAnswerMessage

        assertEquals(authDemandMessage.uuid, authAnswerMessage.toMessage)     // проверяем, что ответ на наше сообщение
        assertEquals(ConnectStatus.AUTH_OK, authAnswerMessage.connectStatus)
    }

    // Тест аутентификации незарегистрированного клиента
    @Test
    fun authNotRegisterClientTest() {
        val authDemandMessage = ConnectOperationMessage(clientAddress, SERVER_ADDRESS, ConnectOperation.AUTH, UserDTO("test-client-not-register", "qwerty"))
        client.send(authDemandMessage)

        val authAnswerMessage: ConnectAnswerMessage = client.take() as ConnectAnswerMessage

        assertEquals(authDemandMessage.uuid, authAnswerMessage.toMessage)     // проверяем, что ответ на наше сообщение
        assertEquals(ConnectStatus.NOT_REGISTER, authAnswerMessage.connectStatus)
    }

    // Тест аутентификации при неверном пароле
    @Test
    fun authRegisterClientWithWrongPasswordTest() {
        val authDemandMessage = ConnectOperationMessage(clientAddress, SERVER_ADDRESS, ConnectOperation.AUTH, UserDTO(REGISTER_NAME, "wrong-password"))
        client.send(authDemandMessage)

        val authAnswerMessage: ConnectAnswerMessage = client.take() as ConnectAnswerMessage

        assertEquals(authDemandMessage.uuid, authAnswerMessage.toMessage)     // проверяем, что ответ на наше сообщение
        assertEquals(ConnectStatus.INCORRECT_PASSWORD, authAnswerMessage.connectStatus)
    }
}
