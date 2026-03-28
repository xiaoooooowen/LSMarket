package com.lsmarket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cache.warmup.shop")
public class CacheWarmupProperties {
    private boolean enabled = true;
    private int topN = 100;
}

