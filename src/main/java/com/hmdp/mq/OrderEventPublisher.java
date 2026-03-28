package com.hmdp.mq;

import com.hmdp.mq.model.OrderCreatedEvent;

public interface OrderEventPublisher {
    void publish(OrderCreatedEvent event);
}
