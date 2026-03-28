<template>
  <div class="shop-detail-page" v-loading="loading">
    <el-row :gutter="20" v-if="shop">
      <el-col :span="16">
        <el-card class="shop-main">
          <div class="shop-header">
            <div class="shop-images">
              <el-carousel height="400px" indicator-position="outside">
                <el-carousel-item v-for="(img, index) in shopImages" :key="index">
                  <img :src="img" alt="" class="carousel-image" />
                </el-carousel-item>
              </el-carousel>
            </div>
            <div class="shop-basic">
              <h2>{{ shop.name }}</h2>
              <div class="shop-meta">
                <el-rate :model-value="shop.score / 2" disabled />
                <span>{{ shop.score }} 分</span>
                <span>{{ shop.comments }} 条评价</span>
              </div>
              <div class="shop-info-item">
                <el-icon><Location /></el-icon>
                <span>{{ shop.area }} - {{ shop.address }}</span>
              </div>
              <div class="shop-info-item">
                <el-icon><Clock /></el-icon>
                <span>营业时间: {{ shop.openHours || '暂无' }}</span>
              </div>
              <div class="shop-info-item">
                <el-icon><Money /></el-icon>
                <span>人均: ¥{{ shop.avgPrice }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="voucher-section">
          <template #header>
            <span>优惠套餐</span>
          </template>
          <div class="voucher-list" v-if="vouchers.length > 0">
            <el-card v-for="voucher in vouchers" :key="voucher.id" class="voucher-item" shadow="hover">
              <div class="voucher-info">
                <div class="voucher-title">
                  <h4>{{ voucher.title }}</h4>
                  <span class="sub-title">{{ voucher.subTitle }}</span>
                </div>
                <div class="voucher-price">
                  <span class="current">¥{{ voucher.payValue / 100 }}</span>
                  <span class="original">¥{{ voucher.actualValue / 100 }}</span>
                </div>
              </div>
              <el-button type="primary" size="small">立即购买</el-button>
            </el-card>
          </div>
          <el-empty v-else description="暂无优惠" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="map-card">
          <template #header>
            <span>商铺位置</span>
          </template>
          <div id="map-container" class="map-container"></div>
          <div class="location-info">
            <p><el-icon><Location /></el-icon> {{ shop.address }}</p>
          </div>
        </el-card>

        <el-card class="related-blogs">
          <template #header>
            <span>相关笔记</span>
          </template>
          <el-empty description="暂无相关笔记" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShopById } from '@/api/shop'
import { getVoucherList } from '@/api/voucher'
import type { Shop } from '@/types/shop'
import type { Voucher } from '@/types/voucher'

const route = useRoute()
const router = useRouter()

const shop = ref<Shop | null>(null)
const vouchers = ref<Voucher[]>([])
const loading = ref(false)

const shopId = computed(() => Number(route.params.id))

const shopImages = computed(() => {
  if (!shop.value?.images) return ['https://picsum.photos/800/400?random=1']
  return shop.value.images.split(',')
})

async function loadShopDetail() {
  loading.value = true
  try {
    shop.value = await getShopById(shopId.value)
    vouchers.value = await getVoucherList(shopId.value)
  } catch {
    console.log('Failed to load shop detail')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadShopDetail()
})
</script>

<style lang="scss" scoped>
.shop-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.shop-main {
  .shop-header {
    .shop-images {
      margin-bottom: 20px;

      .carousel-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 8px;
      }
    }

    .shop-basic {
      h2 {
        font-size: 24px;
        margin-bottom: 16px;
      }

      .shop-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;
        color: #666;
      }

      .shop-info-item {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        color: #666;
      }
    }
  }
}

.voucher-section {
  margin-top: 20px;

  .voucher-list {
    .voucher-item {
      margin-bottom: 12px;

      .voucher-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .voucher-title {
          h4 {
            font-size: 16px;
            margin-bottom: 4px;
          }

          .sub-title {
            font-size: 12px;
            color: #999;
          }
        }

        .voucher-price {
          .current {
            font-size: 20px;
            font-weight: 600;
            color: #f56c6c;
          }

          .original {
            font-size: 14px;
            color: #999;
            text-decoration: line-through;
            margin-left: 8px;
          }
        }
      }
    }
  }
}

.map-card {
  .map-container {
    height: 300px;
    background: #f5f7fa;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
  }

  .location-info {
    margin-top: 12px;
    p {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
    }
  }
}

.related-blogs {
  margin-top: 20px;
}
</style>
