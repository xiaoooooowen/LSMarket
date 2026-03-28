package com.lsmarket.mq.redis;

import cn.hutool.core.bean.BeanUtil;
import com.lsmarket.config.MqProperties;
import com.lsmarket.config.OrderConsumerProperties;
import com.lsmarket.mq.OrderEventConsumer;
import com.lsmarket.mq.OrderEventHandler;
import com.lsmarket.mq.model.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "redis", matchIfMissing = true)
public class RedisOrderEventConsumer implements OrderEventConsumer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MqProperties mqProperties;
    @Resource
    private OrderConsumerProperties orderConsumerProperties;

    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile OrderEventHandler handler;

    @Override
    public void start(OrderEventHandler handler) {
        if (running.get()) {
            return;
        }
        this.handler = handler;
        initOrderStreamGroup();
        running.set(true);
        int workerCount = Math.max(1, orderConsumerProperties.getRedisWorkers());
        executor = Executors.newFixedThreadPool(workerCount);
        String consumerBaseName = mqProperties.getRedis().getConsumer();
        for (int i = 0; i < workerCount; i++) {
            String consumerName = consumerBaseName + "-" + i;
            executor.submit(() -> consumeLoop(consumerName));
        }
        log.info("Redis订单消费者已启动，workerCount={}", workerCount);
    }

    @Override
    public void stop() {
        running.set(false);
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            int awaitSeconds = Math.max(1, orderConsumerProperties.getAwaitTerminationSeconds());
            if (!executor.awaitTermination(awaitSeconds, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
        log.info("Redis订单消费者已停止");
    }

    private void initOrderStreamGroup() {
        String stream = mqProperties.getRedis().getStream();
        String group = mqProperties.getRedis().getGroup();
        try {
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(stream))) {
                Map<String, String> initMessage = new HashMap<>(1);
                initMessage.put("init", "0");
                RecordId recordId = stringRedisTemplate.opsForStream().add(stream, initMessage);
                if (recordId != null) {
                    stringRedisTemplate.opsForStream().delete(stream, recordId);
                }
            }
            stringRedisTemplate.opsForStream().createGroup(stream, ReadOffset.from("0"), group);
        } catch (Exception e) {
            if (e.getMessage() == null || !e.getMessage().contains("BUSYGROUP")) {
                log.error("初始化Redis Stream消费组失败", e);
            }
        }
    }

    private void consumeLoop(String consumer) {
        String stream = mqProperties.getRedis().getStream();
        String group = mqProperties.getRedis().getGroup();
        while (running.get()) {
            try {
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from(group, consumer),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(stream, ReadOffset.lastConsumed())
                );
                if (list == null || list.isEmpty()) {
                    continue;
                }
                MapRecord<String, Object, Object> record = list.get(0);
                OrderCreatedEvent event = BeanUtil.fillBeanWithMap(record.getValue(), new OrderCreatedEvent(), true);
                handler.handle(event);
                stringRedisTemplate.opsForStream().acknowledge(stream, group, record.getId());
            } catch (Exception e) {
                log.error("处理订单事件异常", e);
                handlePendingList(consumer);
            }
        }
    }

    private void handlePendingList(String consumer) {
        String stream = mqProperties.getRedis().getStream();
        String group = mqProperties.getRedis().getGroup();
        while (running.get()) {
            try {
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from(group, consumer),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(stream, ReadOffset.from("0"))
                );
                if (list == null || list.isEmpty()) {
                    break;
                }
                MapRecord<String, Object, Object> record = list.get(0);
                OrderCreatedEvent event = BeanUtil.fillBeanWithMap(record.getValue(), new OrderCreatedEvent(), true);
                handler.handle(event);
                stringRedisTemplate.opsForStream().acknowledge(stream, group, record.getId());
            } catch (Exception e) {
                log.error("处理pending-list订单事件异常", e);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}

