package ru.nchernetsov.test.pixonic.manager;

public interface Publisher {

    void addSubscriber(Subscriber subscriber);

    void removeSubscriber(Subscriber subscriber);
}
