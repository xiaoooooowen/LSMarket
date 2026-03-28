package com.lsmarket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mq")
public class MqProperties {
    private String type = "redis";
    private Redis redis = new Redis();
    private Kafka kafka = new Kafka();

    @Data
    public static class Redis {
        private String stream = "stream.orders";
        private String group = "g1";
        private String consumer = "c1";
    }

    @Data
    public static class Kafka {
        private String orderCreatedTopic = "seckill-order-created";
        private String orderRetryTopic = "seckill-order-retry";
        private String orderDltTopic = "seckill-order-dlt";
        private int maxRetries = 3;
        private long baseBackoffMillis = 200L;
    }
}

