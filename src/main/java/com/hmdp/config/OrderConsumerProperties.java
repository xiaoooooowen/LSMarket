package com.hmdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "order.consumer")
public class OrderConsumerProperties {
    private int redisWorkers = 1;
    private int kafkaConcurrency = 1;
    private int awaitTerminationSeconds = 30;
}
