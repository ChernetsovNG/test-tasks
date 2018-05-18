package ru.nchernetsov.test.sbertech.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.client.Client;

public class ConnectController {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectController.class);

    private Client model;

    public void setModel(Client model) {
        this.model = model;
    }

    public void unlockHandleMessage() {
        if (model != null) {
            model.unlockMessageHandle();
        }
    }
}
