package com.hmdp.mq;

import com.hmdp.mq.model.OrderCreatedEvent;

@FunctionalInterface
public interface OrderEventHandler {
    void handle(OrderCreatedEvent event);
}
