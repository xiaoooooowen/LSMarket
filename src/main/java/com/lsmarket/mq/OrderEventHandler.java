package com.lsmarket.mq;

import com.lsmarket.mq.model.OrderCreatedEvent;

@FunctionalInterface
public interface OrderEventHandler {
    void handle(OrderCreatedEvent event);
}

