package com.lsmarket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lsmarket.config.MqProperties;
import com.lsmarket.config.SeckillLimitProperties;
import com.lsmarket.dto.Result;
import com.lsmarket.entity.VoucherOrder;
import com.lsmarket.mapper.VoucherOrderMapper;
import com.lsmarket.mq.OrderEventConsumer;
import com.lsmarket.mq.OrderEventPublisher;
import com.lsmarket.mq.model.OrderCreatedEvent;
import com.lsmarket.service.ISeckillVoucherService;
import com.lsmarket.service.IVoucherOrderService;
import com.lsmarket.utils.RedisIdWorker;
import com.lsmarket.utils.UserHolder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private OrderEventConsumer orderEventConsumer;
    @Resource
    private OrderEventPublisher orderEventPublisher;
    @Resource
    private MqProperties mqProperties;
    @Resource
    private SeckillLimitProperties seckillLimitProperties;
    @Resource
    private MeterRegistry meterRegistry;
    private Counter seckillRequestCounter;
    private Counter seckillSuccessCounter;
    private Counter orderConsumeSuccessCounter;
    private Counter orderConsumeFailCounter;
    private Timer seckillLatencyTimer;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);

        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setLocation(new ClassPathResource("rate_limiter.lua"));
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    @PostConstruct
    private void init() {
        initMetrics();
        orderEventConsumer.start(this::handleOrderEvent);
    }

    private void initMetrics() {
        seckillRequestCounter = Counter.builder("lsmarket.seckill.request.total").register(meterRegistry);
        seckillSuccessCounter = Counter.builder("lsmarket.seckill.success.total").register(meterRegistry);
        orderConsumeSuccessCounter = Counter.builder("lsmarket.order.consume.success.total").register(meterRegistry);
        orderConsumeFailCounter = Counter.builder("lsmarket.order.consume.fail.total").register(meterRegistry);
        seckillLatencyTimer = Timer.builder("lsmarket.seckill.latency")
                .publishPercentileHistogram()
                .register(meterRegistry);
    }

    @PreDestroy
    private void destroy() {
        orderEventConsumer.stop();
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        seckillRequestCounter.increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        Long userId = UserHolder.getUser().getId();

        if (seckillLimitProperties.isEnabled()) {
            String voucherLimitKey = "seckill:rate:voucher:" + voucherId;
            String userLimitKey = "seckill:rate:voucher:" + voucherId + ":user:" + userId;
            Long limitResult = stringRedisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Arrays.asList(voucherLimitKey, userLimitKey),
                    String.valueOf(seckillLimitProperties.getVoucherCapacity()),
                    String.valueOf(seckillLimitProperties.getVoucherRefillPerSecond()),
                    String.valueOf(seckillLimitProperties.getUserCapacity()),
                    String.valueOf(seckillLimitProperties.getUserRefillPerSecond()),
                    "1"
            );
            if (limitResult == null) {
                Counter.builder("lsmarket.seckill.fail.total").tag("reason", "limiter_error").register(meterRegistry).increment();
                sample.stop(seckillLatencyTimer);
                return Result.fail("系统繁忙，请稍后重试");
            }
            if (limitResult == 1L) {
                Counter.builder("lsmarket.seckill.fail.total").tag("reason", "voucher_rate_limited").register(meterRegistry).increment();
                sample.stop(seckillLatencyTimer);
                return Result.fail("当前秒杀请求过多，请稍后重试");
            }
            if (limitResult == 2L) {
                Counter.builder("lsmarket.seckill.fail.total").tag("reason", "user_rate_limited").register(meterRegistry).increment();
                sample.stop(seckillLatencyTimer);
                return Result.fail("请求过于频繁，请稍后再试");
            }
        }

        long orderId = redisIdWorker.nextId("order");
        String mqType = mqProperties.getType();
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId),
                mqType
        );
        if (result == null) {
            Counter.builder("lsmarket.seckill.fail.total").tag("reason", "seckill_script_error").register(meterRegistry).increment();
            sample.stop(seckillLatencyTimer);
            return Result.fail("系统繁忙，请稍后重试");
        }
        int r = result.intValue();
        if (r != 0) {
            Counter.builder("lsmarket.seckill.fail.total")
                    .tag("reason", r == 1 ? "stock_not_enough" : "duplicate_order")
                    .register(meterRegistry)
                    .increment();
            sample.stop(seckillLatencyTimer);
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }

        if (!"redis".equalsIgnoreCase(mqType)) {
            orderEventPublisher.publish(new OrderCreatedEvent(orderId, userId, voucherId, 0));
        }
        seckillSuccessCounter.increment();
        sample.stop(seckillLatencyTimer);
        return Result.ok(orderId);
    }

    private void handleOrderEvent(OrderCreatedEvent event) {
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(event.getOrderId());
        voucherOrder.setUserId(event.getUserId());
        voucherOrder.setVoucherId(event.getVoucherId());
        boolean success = createVoucherOrder(voucherOrder);
        if (success) {
            orderConsumeSuccessCounter.increment();
        } else {
            orderConsumeFailCounter.increment();
        }
    }

    private boolean createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = redisLock.tryLock();
        if (!isLock) {
            log.error("不允许重复下单！");
            return false;
        }

        try {
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                log.error("不允许重复下单！");
                return false;
            }

            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId).gt("stock", 0)
                    .update();
            if (!success) {
                log.error("库存不足！");
                return false;
            }

            try {
                save(voucherOrder);
            } catch (DuplicateKeyException e) {
                log.warn("订单幂等拦截，重复下单 userId={}, voucherId={}", userId, voucherId);
                return false;
            }
            return true;
        } finally {
            redisLock.unlock();
        }
    }
}

