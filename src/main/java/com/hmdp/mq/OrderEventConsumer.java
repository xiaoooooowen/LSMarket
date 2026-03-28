package com.hmdp.mq;

public interface OrderEventConsumer {
    void start(OrderEventHandler handler);

    void stop();
}
