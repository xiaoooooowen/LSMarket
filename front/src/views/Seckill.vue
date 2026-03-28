<template>
  <div class="seckill-page">
    <el-card class="countdown-card">
      <div class="countdown-header">
        <h2>限时秒杀</h2>
        <div class="next-round">
          <span>距离下一场:</span>
          <div class="time-blocks">
            <span class="time-block">{{ hours }}</span>
            <span class="separator">:</span>
            <span class="time-block">{{ minutes }}</span>
            <span class="separator">:</span>
            <span class="time-block">{{ seconds }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <div class="seckill-list">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in seckillItems" :key="item.id">
          <el-card class="seckill-item" shadow="hover">
            <div class="item-image">
              <img :src="item.image" alt="" />
              <div class="seckill-tag">秒杀</div>
            </div>
            <div class="item-info">
              <h4>{{ item.title }}</h4>
              <div class="price-row">
                <span class="seckill-price">¥{{ item.seckillPrice }}</span>
                <span class="original-price">¥{{ item.originalPrice }}</span>
              </div>
              <el-progress 
                :percentage="item.percentage" 
                :show-text="false"
                :stroke-width="8"
                color="#f56c6c"
              />
              <p class="stock-info">已抢{{ item.percentage }}%，剩余{{ item.stock }}件</p>
              <el-button 
                type="danger" 
                style="width: 100%"
                @click="handleSeckill(item)"
                :loading="item.loading"
                :disabled="item.stock === 0"
              >
                {{ item.stock === 0 ? '已抢光' : '立即抢购' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { seckillVoucher } from '@/api/voucher'

interface SeckillItem {
  id: number
  title: string
  image: string
  seckillPrice: number
  originalPrice: number
  stock: number
  percentage: number
  loading: boolean
}

const hours = ref('00')
const minutes = ref('00')
const seconds = ref('00')

const seckillItems = ref<SeckillItem[]>([
  { id: 1, title: '美食套餐', image: 'https://picsum.photos/300/200?random=1', seckillPrice: 99, originalPrice: 199, stock: 50, percentage: 50, loading: false },
  { id: 2, title: '下午茶', image: 'https://picsum.photos/300/200?random=2', seckillPrice: 29, originalPrice: 59, stock: 30, percentage: 70, loading: false },
  { id: 3, title: '电影票', image: 'https://picsum.photos/300/200?random=3', seckillPrice: 19, originalPrice: 49, stock: 100, percentage: 20, loading: false },
  { id: 4, title: '健身卡', image: 'https://picsum.photos/300/200?random=4', seckillPrice: 199, originalPrice: 399, stock: 10, percentage: 90, loading: false },
])

let timer: number

function updateCountdown() {
  const now = new Date()
  const nextHour = new Date(now)
  nextHour.setHours(now.getHours() + 1, 0, 0, 0)
  
  const diff = nextHour.getTime() - now.getTime()
  
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const s = Math.floor((diff % (1000 * 60)) / 1000)
  
  hours.value = String(h).padStart(2, '0')
  minutes.value = String(m).padStart(2, '0')
  seconds.value = String(s).padStart(2, '0')
}

async function handleSeckill(item: SeckillItem) {
  item.loading = true
  try {
    await seckillVoucher(item.id)
    ElMessage.success('抢购成功！')
    item.stock--
    item.percentage = Math.min(100, item.percentage + 10)
  } catch {
    ElMessage.error('抢购失败，请重试')
  } finally {
    item.loading = false
  }
}

onMounted(() => {
  updateCountdown()
  timer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.seckill-page {
  max-width: 1200px;
  margin: 0 auto;
}

.countdown-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #f56c6c 0%, #ff8a8a 100%);
  color: #fff;

  .countdown-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      font-size: 28px;
    }

    .next-round {
      display: flex;
      align-items: center;
      gap: 12px;

      .time-blocks {
        display: flex;
        align-items: center;

        .time-block {
          background: #fff;
          color: #f56c6c;
          padding: 8px 12px;
          border-radius: 4px;
          font-size: 20px;
          font-weight: 600;
        }

        .separator {
          margin: 0 4px;
          font-size: 20px;
        }
      }
    }
  }
}

.seckill-list {
  .seckill-item {
    margin-bottom: 20px;

    .item-image {
      position: relative;
      height: 150px;
      overflow: hidden;
      border-radius: 8px;
      margin-bottom: 12px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .seckill-tag {
        position: absolute;
        top: 0;
        left: 0;
        background: #f56c6c;
        color: #fff;
        padding: 4px 12px;
        font-size: 12px;
        border-radius: 0 0 8px 0;
      }
    }

    .item-info {
      h4 {
        font-size: 16px;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .price-row {
        margin-bottom: 12px;

        .seckill-price {
          font-size: 24px;
          font-weight: 600;
          color: #f56c6c;
        }

        .original-price {
          font-size: 14px;
          color: #999;
          text-decoration: line-through;
          margin-left: 8px;
        }
      }

      .stock-info {
        font-size: 12px;
        color: #999;
        margin: 8px 0 12px;
      }
    }
  }
}
</style>
