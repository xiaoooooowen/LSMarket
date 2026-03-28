<template>
  <div class="voucher-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>优惠券中心</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="可领取" name="available">
          <div class="voucher-list">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="voucher in vouchers" :key="voucher.id">
                <el-card class="voucher-item" shadow="hover">
                  <div class="voucher-left">
                    <div class="price">
                      <span class="symbol">¥</span>
                      <span class="value">{{ voucher.payValue / 100 }}</span>
                    </div>
                    <div class="condition">满{{ voucher.actualValue / 100 }}可用</div>
                  </div>
                  <div class="voucher-right">
                    <h4>{{ voucher.title }}</h4>
                    <p class="sub-title">{{ voucher.subTitle }}</p>
                    <p class="validity">有效期: {{ voucher.beginTime }} - {{ voucher.endTime }}</p>
                    <el-button type="primary" size="small">立即领取</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="我的优惠券" name="mine">
          <el-empty description="暂无优惠券" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getVoucherList } from '@/api/voucher'
import type { Voucher } from '@/types/voucher'

const activeTab = ref('available')
const vouchers = ref<Voucher[]>([])

onMounted(async () => {
  try {
    vouchers.value = await getVoucherList(1)
  } catch {
    console.log('Failed to load vouchers')
  }
})
</script>

<style lang="scss" scoped>
.voucher-page {
  max-width: 1200px;
  margin: 0 auto;
}

.voucher-list {
  .voucher-item {
    margin-bottom: 20px;
    display: flex;
    overflow: hidden;
    border-radius: 8px;

    .voucher-left {
      width: 120px;
      background: linear-gradient(135deg, #f56c6c 0%, #ff8a8a 100%);
      color: #fff;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px;

      .price {
        .symbol {
          font-size: 16px;
        }

        .value {
          font-size: 32px;
          font-weight: 600;
        }
      }

      .condition {
        font-size: 12px;
        margin-top: 8px;
      }
    }

    .voucher-right {
      flex: 1;
      padding: 16px;

      h4 {
        font-size: 16px;
        margin-bottom: 8px;
      }

      .sub-title {
        font-size: 12px;
        color: #999;
        margin-bottom: 8px;
      }

      .validity {
        font-size: 12px;
        color: #999;
        margin-bottom: 12px;
      }
    }
  }
}
</style>
