package com.lsmarket.mq.kafka;

import com.lsmarket.config.MqProperties;
import com.lsmarket.mq.OrderEventPublisher;
import com.lsmarket.mq.model.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "kafka")
public class KafkaOrderEventPublisher implements OrderEventPublisher {
    @Resource
    private MqProperties mqProperties;
    @Resource
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Override
    public void publish(OrderCreatedEvent event) {
        String topic = mqProperties.getKafka().getOrderCreatedTopic();
        ListenableFuture<SendResult<String, OrderCreatedEvent>> future =
                kafkaTemplate.send(topic, String.valueOf(event.getOrderId()), event);
        future.addCallback(new ListenableFutureCallback<SendResult<String, OrderCreatedEvent>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Kafka订单事件发布失败，topic={}, event={}", topic, event, ex);
            }

            @Override
            public void onSuccess(SendResult<String, OrderCreatedEvent> result) {
                log.debug("Kafka订单事件发布成功，topic={}, offset={}", topic,
                        result == null ? null : result.getRecordMetadata().offset());
            }
        });
    }
}

