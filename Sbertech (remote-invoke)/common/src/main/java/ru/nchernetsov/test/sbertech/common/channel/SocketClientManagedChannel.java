package ru.nchernetsov.test.sbertech.common.channel;

import java.io.IOException;
import java.net.Socket;

public class SocketClientManagedChannel extends SocketClientChannel {

    private final Socket socket;

    public SocketClientManagedChannel(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    private SocketClientManagedChannel(Socket socket) throws IOException {
        super(socket);
        this.socket = socket;
    }

    public void close() throws IOException {
        super.close();
        socket.close();
    }
}
