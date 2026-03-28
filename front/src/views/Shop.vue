<template>
  <div class="shop-page">
    <el-card class="filter-card">
      <div class="filter-header">
        <span>分类筛选</span>
      </div>
      <div class="category-list">
        <el-tag
          :type="currentTypeId === null ? 'primary' : 'info'"
          @click="selectType(null)"
          class="category-tag"
        >
          全部
        </el-tag>
        <el-tag
          v-for="type in shopTypes"
          :key="type.id"
          :type="currentTypeId === type.id ? 'primary' : 'info'"
          @click="selectType(type.id)"
          class="category-tag"
        >
          {{ type.name }}
        </el-tag>
      </div>
    </el-card>

    <div class="shop-list" v-loading="loading">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="shop in shops" :key="shop.id">
          <el-card class="shop-card" shadow="hover" @click="router.push(`/shop/${shop.id}`)">
            <div class="shop-image">
              <img :src="getFirstImage(shop.images)" alt="" />
            </div>
            <div class="shop-info">
              <h4>{{ shop.name }}</h4>
              <div class="shop-meta">
                <el-rate :model-value="shop.score / 2" disabled size="small" />
                <span>{{ shop.comments }} 条评价</span>
              </div>
              <div class="shop-address">
                <el-icon><Location /></el-icon>
                <span>{{ shop.area }} - {{ shop.address }}</span>
              </div>
              <div class="shop-price">
                <span class="price">¥{{ shop.avgPrice }}/人</span>
                <span class="sold">已售 {{ shop.sold }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadShops"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useShopStore } from '@/stores/shop'
import { getShopList } from '@/api/shop'
import type { Shop } from '@/types/shop'

const router = useRouter()
const route = useRoute()
const shopStore = useShopStore()

const { shopTypes, currentTypeId } = storeToRefs(shopStore)

const shops = ref<Shop[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

function getFirstImage(images: string) {
  if (!images) return 'https://picsum.photos/300/200?random=1'
  return images.split(',')[0]
}

function selectType(typeId: number | null) {
  shopStore.setCurrentType(typeId)
  currentPage.value = 1
  loadShops()
}

async function loadShops() {
  loading.value = true
  try {
    const typeId = currentTypeId.value === null ? undefined : currentTypeId.value
    const res = await getShopList(typeId, currentPage.value, pageSize.value)
    shops.value = res.records
    total.value = res.total
  } catch {
    console.log('Failed to load shops')
  } finally {
    loading.value = false
  }
}

watch(() => route.query.typeId, (newTypeId) => {
  if (newTypeId) {
    shopStore.setCurrentType(Number(newTypeId))
  }
  loadShops()
})

onMounted(async () => {
  await shopStore.fetchShopTypes()
  if (route.query.typeId) {
    shopStore.setCurrentType(Number(route.query.typeId))
  }
  loadShops()
})
</script>

<style lang="scss" scoped>
.shop-page {
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;

  .filter-header {
    font-weight: 600;
    margin-bottom: 12px;
  }

  .category-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .category-tag {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        opacity: 0.8;
      }
    }
  }
}

.shop-list {
  min-height: 400px;

  .shop-card {
    margin-bottom: 20px;
    cursor: pointer;

    .shop-image {
      height: 150px;
      overflow: hidden;
      border-radius: 8px;
      margin-bottom: 12px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s;
      }
    }

    &:hover img {
      transform: scale(1.05);
    }

    .shop-info {
      h4 {
        font-size: 16px;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .shop-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        font-size: 12px;
        color: #999;
      }

      .shop-address {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #666;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .shop-price {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .price {
          color: #f56c6c;
          font-weight: 600;
        }

        .sold {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
