# Kafka 死信重放说明

## 1. 适用范围

当前 `mq.type=kafka` 时，订单消息消费失败会按以下路径流转：

- 首次失败：投递到 `seckill-order-retry`
- 超过 `mq.kafka.max-retries`：投递到 `seckill-order-dlt`

## 2. 查看死信消息

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic seckill-order-dlt --from-beginning
```

## 3. 重放思路

- 从 `seckill-order-dlt` 读取消息
- 将消息重新发布到 `seckill-order-created`
- 建议发布前将 `retryCount` 重置为 `0`

## 4. 注意事项

- 重放前先确认故障根因已修复（如 DB 慢查询、索引缺失、下游不可用）
- 重放应分批进行，避免瞬时流量冲击
- 依赖 `tb_voucher_order(user_id, voucher_id)` 唯一索引保证幂等
