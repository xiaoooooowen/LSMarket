package com.hmdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "seckill.limit")
public class SeckillLimitProperties {
    private boolean enabled = true;
    private int voucherCapacity = 200;
    private int voucherRefillPerSecond = 200;
    private int userCapacity = 1;
    private int userRefillPerSecond = 1;
}
