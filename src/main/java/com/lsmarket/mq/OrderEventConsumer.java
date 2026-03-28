package com.lsmarket.mq;

public interface OrderEventConsumer {
    void start(OrderEventHandler handler);

    void stop();
}

