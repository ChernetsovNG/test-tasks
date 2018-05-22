package ru.nchernetsov.test.sbertech.common.channel;

import lombok.extern.slf4j.Slf4j;
import ru.nchernetsov.test.sbertech.common.message.Message;
import ru.nchernetsov.test.sbertech.common.message.PoisonPill;

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
            while (!clientSocket.isClosed()) {
                Message message = outputMessages.take();  // blocks here
                if (message instanceof PoisonPill) {
                    break;
                }
                out.writeObject(message);
            }
        } catch (InterruptedException e) {
            log.warn("Socket was closed by client. Interrupt channel");
        } catch (IOException e) {
            log.warn("sendMessage: Socket was closed by client");
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
        } catch (ClassNotFoundException e) {
            log.error("receiveMessage: " + e.getMessage() + "; exceptionClass: " + e.getClass());
        } catch (IOException e) {
            log.warn("receiveMessage: Socket was closed by client. Interrupt channel");
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

        inputMessages.add(new PoisonPill(null, null));
        outputMessages.add(new PoisonPill(null, null));
        try {
            executor.awaitTermination(500L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.warn("Client shutdown");
        }
        executor.shutdown();

        inputMessages.clear();
        outputMessages.clear();
    }

}
