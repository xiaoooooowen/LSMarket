package com.lsmarket.mq.kafka;

import com.lsmarket.config.MqProperties;
import com.lsmarket.config.OrderConsumerProperties;
import com.lsmarket.mq.OrderEventConsumer;
import com.lsmarket.mq.OrderEventHandler;
import com.lsmarket.mq.model.OrderCreatedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "kafka")
public class KafkaOrderEventConsumer implements OrderEventConsumer {
    @Resource
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    @Resource
    private MqProperties mqProperties;
    @Resource
    private OrderConsumerProperties orderConsumerProperties;
    @Resource
    private MeterRegistry meterRegistry;

    private volatile OrderEventHandler handler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger inFlight = new AtomicInteger(0);
    private Counter retryCounter;
    private Counter dltCounter;

    @PostConstruct
    private void initMetrics() {
        Gauge.builder("lsmarket.order.consumer.inflight", inFlight, AtomicInteger::get)
                .description("Current in-flight order messages being consumed")
                .register(meterRegistry);
        retryCounter = Counter.builder("lsmarket.order.retry.total").register(meterRegistry);
        dltCounter = Counter.builder("lsmarket.order.dlt.total").register(meterRegistry);
    }

    @Override
    public void start(OrderEventHandler handler) {
        this.handler = handler;
        running.set(true);
        log.info("Kafka订单消费者已启动");
    }

    @Override
    public void stop() {
        running.set(false);
        int awaitSeconds = Math.max(1, orderConsumerProperties.getAwaitTerminationSeconds());
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(awaitSeconds);
        while (inFlight.get() > 0 && System.nanoTime() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("Kafka订单消费者已停止");
    }

    @KafkaListener(
            topics = {
                    "${mq.kafka.order-created-topic:seckill-order-created}",
                    "${mq.kafka.order-retry-topic:seckill-order-retry}"
            },
            groupId = "${spring.kafka.consumer.group-id:lsmarket-order-consumer}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        if (!running.get() || isNull(handler)) {
            log.warn("Kafka消息到达但消费者未就绪，event={}", event);
            throw new IllegalStateException("Kafka订单消费者未就绪");
        }
        inFlight.incrementAndGet();
        try {
            handler.handle(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Kafka订单消息消费失败，event={}", event, e);
            handleFailedEvent(event);
            acknowledgment.acknowledge();
        } finally {
            inFlight.decrementAndGet();
        }
    }

    private void handleFailedEvent(OrderCreatedEvent event) {
        int currentRetry = event.getRetryCount() == null ? 0 : event.getRetryCount();
        int nextRetry = currentRetry + 1;
        int maxRetries = mqProperties.getKafka().getMaxRetries();
        if (nextRetry <= maxRetries) {
            event.setRetryCount(nextRetry);
            long backoff = calculateBackoff(nextRetry);
            try {
                if (backoff > 0) {
                    Thread.sleep(backoff);
                }
                kafkaTemplate.send(
                        mqProperties.getKafka().getOrderRetryTopic(),
                        String.valueOf(event.getOrderId()),
                        event
                ).get();
                retryCounter.increment();
                log.warn("Kafka订单消息重试投递，orderId={}, retry={}", event.getOrderId(), nextRetry);
                return;
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(interruptedException);
            } catch (ExecutionException executionException) {
                throw new RuntimeException(executionException);
            }
        }

        try {
            kafkaTemplate.send(
                    mqProperties.getKafka().getOrderDltTopic(),
                    String.valueOf(event.getOrderId()),
                    event
            ).get();
            dltCounter.increment();
            log.error("Kafka订单消息进入死信队列，orderId={}, retry={}", event.getOrderId(), currentRetry);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(interruptedException);
        } catch (ExecutionException executionException) {
            throw new RuntimeException(executionException);
        }
    }

    private long calculateBackoff(int retryCount) {
        long base = mqProperties.getKafka().getBaseBackoffMillis();
        if (base <= 0) {
            return 0L;
        }
        long multiplier = 1L << Math.max(0, retryCount - 1);
        return base * multiplier;
    }
}

