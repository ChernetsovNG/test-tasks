package ru.nchernetsov.test.sbertech.common.channel;

import lombok.extern.slf4j.Slf4j;
import ru.nchernetsov.test.sbertech.common.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

/**
 * Сокетный канал для передачи сообщений
 */
@Slf4j
public class SocketClientChannel implements MessageChannel {
    private static final int WORKERS_COUNT = 2;

    /**
     * Очередь для исходящих (отправляемых в сокет) сообщений
     */
    private final BlockingQueue<Message> outputMessages = new LinkedBlockingQueue<>();
    /**
     * Очередь для входящих (прочитанных из сокета) сообщений
     */
    private final BlockingQueue<Message> inputMessages = new LinkedBlockingQueue<>();

    private final ExecutorService executor;
    private final Socket clientSocket;
    private final List<Runnable> shutdownRegistrations;

    public SocketClientChannel(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.executor = Executors.newFixedThreadPool(WORKERS_COUNT);
        this.shutdownRegistrations = new CopyOnWriteArrayList<>();
    }

    public void init() {
        executor.execute(this::sendMessage);
        executor.execute(this::receiveMessage);
    }

    // прочитать сообщение из очереди и отправить его в сокетный канал
    private void sendMessage() {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            while (clientSocket.isConnected()) {
                Message message = outputMessages.take();  // blocks here
                out.writeObject(message);
            }
        } catch (InterruptedException | IOException e) {
            log.error("sendMessage: " + e.getMessage() + "; exceptionClass: " + e.getClass());
        }
    }

    // получить сообщение из сокетного канала и записать его в очередь
    private void receiveMessage() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            Object readObject;
            while ((readObject = in.readObject()) != null) {  // blocks here
                Message message = (Message) readObject;
                inputMessages.add(message);
            }
        } catch (ClassNotFoundException | IOException e) {
            log.error("receiveMessage: " + e.getMessage() + "; exceptionClass: " + e.getClass());
        }
    }

    public void addShutdownRegistration(Runnable runnable) {
        this.shutdownRegistrations.add(runnable);
    }

    @Override
    public void send(Message message) {
        outputMessages.add(message);
    }

    @Override
    public Message poll() {
        return inputMessages.poll();
    }

    @Override
    public Message take() throws InterruptedException {
        return inputMessages.take();
    }

    public void close() throws IOException {
        shutdownRegistrations.forEach(Runnable::run);
        shutdownRegistrations.clear();
        executor.shutdownNow();
    }

}
