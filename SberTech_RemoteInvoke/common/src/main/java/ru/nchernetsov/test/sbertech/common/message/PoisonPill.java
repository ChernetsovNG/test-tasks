package ru.nchernetsov.test.sbertech.common.message;

/**
 * Message to unblock waiting BlockingQueue
 */
public class PoisonPill extends Message {
    public PoisonPill(Address from, Address to) {
        super(from, to);
    }
}
