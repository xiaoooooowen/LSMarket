package com.hmdp.task;

import com.github.benmanes.caffeine.cache.Cache;
import com.hmdp.config.CacheWarmupProperties;
import com.hmdp.config.LocalCacheProperties;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TTL;

@Slf4j
@Component
public class ShopCacheWarmupTask {
    @Resource
    private IShopService shopService;
    @Resource
    private CacheClient cacheClient;
    @Resource
    private Cache<Long, Shop> shopLocalCache;
    @Resource
    private LocalCacheProperties localCacheProperties;
    @Resource
    private CacheWarmupProperties cacheWarmupProperties;

    @Scheduled(cron = "${cache.warmup.shop.cron:0 */5 * * * ?}")
    public void warmupHotShops() {
        if (!cacheWarmupProperties.isEnabled()) {
            return;
        }
        int topN = cacheWarmupProperties.getTopN();
        if (topN <= 0) {
            return;
        }
        List<Shop> hotShops = shopService.query()
                .orderByDesc("sold")
                .last("limit " + topN)
                .list();
        for (Shop shop : hotShops) {
            if (shop.getId() == null) {
                continue;
            }
            cacheClient.set(CACHE_SHOP_KEY + shop.getId(), shop, CACHE_SHOP_TTL, TimeUnit.MINUTES);
            if (localCacheProperties.isEnabled()) {
                shopLocalCache.put(shop.getId(), shop);
            }
        }
        log.info("店铺缓存预热完成，数量={}", hotShops.size());
    }
}
