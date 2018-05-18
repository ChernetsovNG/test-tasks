package ru.nchernetsov.test.sbertech.server

import kotlinx.coroutines.experimental.async
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import ru.geekbrains.common.CommonData.*
import ru.geekbrains.common.channel.SocketClientManagedChannel
import ru.geekbrains.common.dto.*
import ru.geekbrains.common.message.*
import ru.geekbrains.common.utils.FileUtils
import ru.geekbrains.common.utils.FileUtils.*
import ru.geekbrains.server.db.Database
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class FileOperationsTest {
    companion object {
        lateinit var server: Server
        val clientNumber = AtomicInteger(0)
        val TEST_SERVER_PORT = SERVER_PORT + 1
        val REGISTER_NAME = "test_client"
        val CLIENT_FOLDER = CLIENTS_FOLDERS_PATH + FILE_SEPARATOR + REGISTER_NAME
        val ROOT_FOLDER = ""

        @BeforeAll
        @JvmStatic
        fun startServer() {
            // запускаем в отдельном потоке, чтобы не было блокировок
            async {
                server = Server()
                server.start("src/test/resources/test-data.db", TEST_SERVER_PORT)
            }
            TimeUnit.MILLISECONDS.sleep(1000)  // wait server initialization
            if (isFolderExists(CLIENT_FOLDER)) {
                deleteDirectory(CLIENT_FOLDER)
            }
        }

        @AfterAll
        @JvmStatic
        fun stopServer() {
            Database.clearDatabase()
            TimeUnit.MILLISECONDS.sleep(500)
            server.stop()
        }
    }

    lateinit var client: SocketClientManagedChannel
    var clientAddress = Address("")

    // перед каждым тестом стартуем нового клиента и подключаемся к серверу
    @BeforeEach
    fun startNewClientAndHandshake() {
        Database.clearDatabase()  // очищаем базу данных

        client = SocketClientManagedChannel("localhost", TEST_SERVER_PORT)
        client.init()

        // перед каждым тестом делаем handshake нового клиента на сервере
        clientAddress = Address("test-client-file-" + clientNumber.addAndGet(1))

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

        // аутентифицируем клиента на сервере
        val authDemandMessage = ConnectOperationMessage(clientAddress, SERVER_ADDRESS, ConnectOperation.AUTH, UserDTO(ConnectionToServerTest.REGISTER_NAME, "qwerty"))
        client.send(authDemandMessage)

        val authAnswerMessage: ConnectAnswerMessage = client.take() as ConnectAnswerMessage

        assertEquals(authDemandMessage.uuid, authAnswerMessage.toMessage)     // проверяем, что ответ на наше сообщение
        assertEquals(ConnectStatus.AUTH_OK, authAnswerMessage.connectStatus)

        // создаём для клиента новую папку
        val createFolderMessage = FileMessage(clientAddress, SERVER_ADDRESS, FileObjectToOperate.FOLDER, FileOperation.CREATE, null, false)
        client.send(createFolderMessage)

        val createFolderAnswer: FileAnswer = client.take() as FileAnswer

        assertEquals(createFolderMessage.uuid, createFolderAnswer.toMessage)
        assertEquals(FileStatus.OK, createFolderAnswer.fileStatus)
    }

    @AfterEach
    fun afterTest() {
        // после каждого теста удаляем папку клиента
        FileUtils.deleteDirectory(CLIENT_FOLDER)
    }

    @Test
    fun getFileListFromRootFolderTest() {
        // create some files and directories
        createNewFile(CLIENT_FOLDER, "file1.txt", byteArrayOf(1, 2, 3))
        createNewFile(CLIENT_FOLDER, "file2.txt", byteArrayOf(5, 7, 11))
        createNewFile(CLIENT_FOLDER, "file3.txt", byteArrayOf(13, 17, 19))
        createNewDirectory(CLIENT_FOLDER, "dir1")
        createNewDirectory(CLIENT_FOLDER, "dir2")

        val activeFolder = ROOT_FOLDER

        val getFileListMessage = FileMessage(clientAddress, SERVER_ADDRESS, FileObjectToOperate.FILE, FileOperation.GET_LIST, FileDTO(activeFolder, null, null), false)
        client.send(getFileListMessage)

        val getFileListAnswer: FileAnswer = client.take() as FileAnswer

        assertEquals(getFileListMessage.uuid, getFileListAnswer.toMessage)
        assertEquals(FileStatus.OK, getFileListAnswer.fileStatus)

        val fileInfoList = getFileListAnswer.additionalObject as List<FileInfo>
        val fileNameTypeMap = fileInfoList.map { it -> Pair(it.fileName, it.isDirectory) }.toMap()

        assertEquals(5, fileInfoList.size)
        assertTrue(fileNameTypeMap.containsKey("file1.txt")); assertEquals(false, fileNameTypeMap["file1.txt"])
        assertTrue(fileNameTypeMap.containsKey("file2.txt")); assertEquals(false, fileNameTypeMap["file2.txt"])
        assertTrue(fileNameTypeMap.containsKey("file3.txt")); assertEquals(false, fileNameTypeMap["file3.txt"])
        assertTrue(fileNameTypeMap.containsKey("dir1")); assertEquals(true, fileNameTypeMap["dir1"])
        assertTrue(fileNameTypeMap.containsKey("dir2")); assertEquals(true, fileNameTypeMap["dir2"])
    }

    @Test
    fun createNewFolderInRootFolderTest() {
        val activeFolder = ROOT_FOLDER

        val createFolderMessage = FileMessage(clientAddress, SERVER_ADDRESS, FileObjectToOperate.FOLDER, FileOperation.CREATE, FileDTO(activeFolder, "new-folder", null), false)
        client.send(createFolderMessage)

        val createFolderAnswer: FileAnswer = client.take() as FileAnswer

        assertEquals(createFolderMessage.uuid, createFolderAnswer.toMessage)
        assertEquals(FileStatus.OK, createFolderAnswer.fileStatus)

        // проверяем, что папка действительно была создана
        isFolderExists(CLIENT_FOLDER + FILE_SEPARATOR + "new-folder")
    }

    @Test
    fun createNewFileTest() {
        val activeFolder = ROOT_FOLDER

        val fileName = "test-file.txt"
        val fileContent = byteArrayOf(1, 2, 3, 5, 7, 11, 13, 17, 19, 23)
        val fileDTO = FileDTO(activeFolder, fileName, fileContent)

        val createNewFileMessage = FileMessage(clientAddress, SERVER_ADDRESS, FileObjectToOperate.FILE, FileOperation.CREATE, fileDTO, false)
        client.send(createNewFileMessage)

        val createNewFileAnswer: FileAnswer = client.take() as FileAnswer

        assertEquals(createNewFileMessage.uuid, createNewFileAnswer.toMessage)
        assertEquals(FileStatus.OK, createNewFileAnswer.fileStatus)

        // проверяем, что файл действительно создан
        assertTrue(isFileExists(CLIENT_FOLDER, fileName))
        // проверяем содержимое файла
        val filePath = Paths.get(CLIENT_FOLDER + FILE_SEPARATOR + fileName)
        assertArrayEquals(fileContent, Files.readAllBytes(filePath))
    }

    @Test
    fun deleteFileTest() {
        // create file
        val fileName = "file1.txt"
        createNewFile(CLIENT_FOLDER, fileName, byteArrayOf(1, 2, 3))
        isFileExists(CLIENT_FOLDER, fileName)

        val activeFolder = ROOT_FOLDER

        val fileDTO = FileDTO(activeFolder, fileName, null)
        val deleteFileMessage = FileMessage(clientAddress, SERVER_ADDRESS, FileObjectToOperate.FILE, FileOperation.DELETE, fileDTO, false)
        client.send(deleteFileMessage)

        val deleteFileAnswer: FileAnswer = client.take() as FileAnswer

        assertEquals(deleteFileMessage.uuid, deleteFileAnswer.toMessage)
        assertEquals(FileStatus.OK, deleteFileAnswer.fileStatus)
        // проверяем, что файл действительно удалён
        assertFalse(isFileExists(CLIENT_FOLDER, fileName))
    }

    @Test
    fun renameFileTest() {
        // create file
        val fileName = "file2.txt"
        val fileContent = byteArrayOf(5, 7, 11)
        createNewFile(CLIENT_FOLDER, fileName, fileContent)
        isFileExists(CLIENT_FOLDER, fileName)

        val activeFolder = ROOT_FOLDER

        val newFileName = "new-file2.txt"
        val renameFileDTO = ChangeFileDTO(FileDTO(activeFolder, fileName, null), FileDTO(activeFolder, newFileName, null))
        val renameFileMessage = FileMessage(clientAddress, SERVER_ADDRESS, FileObjectToOperate.FILE, FileOperation.RENAME, renameFileDTO, false)
        client.send(renameFileMessage)

        val renameFileAnswer: FileAnswer = client.take() as FileAnswer

        assertEquals(renameFileMessage.uuid, renameFileAnswer.toMessage)
        assertEquals(FileStatus.OK, renameFileAnswer.fileStatus)
        // проверяем, что файл действительно переименован
        assertFalse(isFileExists(CLIENT_FOLDER, fileName))
        assertTrue(isFileExists(CLIENT_FOLDER, newFileName))
        // проверяем содержимое файла
        val filePath = Paths.get(CLIENT_FOLDER + FILE_SEPARATOR + newFileName)
        assertArrayEquals(fileContent, Files.readAllBytes(filePath))
    }

    @Test
    fun downloadFileTest() {
        // create file
        val fileName = "file3.txt"
        val fileContent = byteArrayOf(13, 17, 19)
        createNewFile(CLIENT_FOLDER, fileName, fileContent)
        isFileExists(CLIENT_FOLDER, fileName)

        val activeFolder = ROOT_FOLDER

        val fileDTO = FileDTO(activeFolder, fileName, null)
        val downloadFileMessage = FileMessage(clientAddress, SERVER_ADDRESS, FileObjectToOperate.FILE, FileOperation.READ, fileDTO, false)
        client.send(downloadFileMessage)

        val downloadFileAnswer: FileAnswer = client.take() as FileAnswer

        assertEquals(downloadFileMessage.uuid, downloadFileAnswer.toMessage)
        assertEquals(FileStatus.OK, downloadFileAnswer.fileStatus)
        val downloadFileContent = downloadFileAnswer.additionalObject as ByteArray
        assertArrayEquals(fileContent, downloadFileContent)
    }
}
