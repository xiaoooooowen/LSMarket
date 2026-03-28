package com.hmdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cache.local.shop")
public class LocalCacheProperties {
    private boolean enabled = true;
    private long maximumSize = 10000;
    private long expireSeconds = 60;
}
