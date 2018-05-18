package ru.nchernetsov.test.sbertech.server.handler;

import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.message.Address;
import ru.nchernetsov.test.sbertech.common.message.ConnectOperationMessage;

import java.util.Map;
import java.util.Optional;

public interface ConnectDemandHandler {
    void handleConnectDemand(Address clientAddress, MessageChannel clientChannel, ConnectOperationMessage message);

    Map<MessageChannel, Address> getClientAddressMap();

    Optional<String> getClientName(MessageChannel clientChannel);

    void addNewClientChannel(MessageChannel clientChannel);

    void removeClientChannel(MessageChannel clientChannel);
}
