package com.lsmarket.mq;

import com.lsmarket.mq.model.OrderCreatedEvent;

public interface OrderEventPublisher {
    void publish(OrderCreatedEvent event);
}

