package com.lsmarket.mq.redis;

import com.lsmarket.config.MqProperties;
import com.lsmarket.mq.OrderEventPublisher;
import com.lsmarket.mq.model.OrderCreatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "redis", matchIfMissing = true)
public class RedisOrderEventPublisher implements OrderEventPublisher {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MqProperties mqProperties;

    @Override
    public void publish(OrderCreatedEvent event) {
        Map<String, String> message = new HashMap<>(3);
        message.put("id", String.valueOf(event.getOrderId()));
        message.put("userId", String.valueOf(event.getUserId()));
        message.put("voucherId", String.valueOf(event.getVoucherId()));
        stringRedisTemplate.opsForStream().add(mqProperties.getRedis().getStream(), message);
    }
}

