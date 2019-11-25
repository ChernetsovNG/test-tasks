package ru.nchernetsov.test.pixonic;

public interface Publisher {

    void addSubscriber(Subscriber subscriber);

    void removeSubscriber(Subscriber subscriber);
}
