<template>
  <div class="home-page">
    <el-carousel height="400px" class="banner">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <div class="banner-item" :style="{ backgroundImage: `url(${item.image})` }">
          <div class="banner-content">
            <h2>{{ item.title }}</h2>
            <p>{{ item.desc }}</p>
            <el-button type="primary" size="large" @click="router.push(item.link)">
              立即探索
            </el-button>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="section">
      <div class="section-header">
        <h3>热门分类</h3>
        <el-button text @click="router.push('/shop')">查看全部</el-button>
      </div>
      <div class="category-list">
        <div
          v-for="type in shopTypes"
          :key="type.id"
          class="category-item"
          @click="goToShopType(type.id)"
        >
          <el-icon :size="40"><component :is="type.icon || 'Shop'" /></el-icon>
          <span>{{ type.name }}</span>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3>热门探店笔记</h3>
        <el-button text @click="router.push('/blog')">查看全部</el-button>
      </div>
      <div class="blog-list">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="blog in hotBlogs" :key="blog.id">
            <el-card class="blog-card" shadow="hover" @click="router.push(`/blog/${blog.id}`)">
              <div class="blog-image">
                <img :src="getFirstImage(blog.images)" alt="" />
              </div>
              <div class="blog-info">
                <h4>{{ blog.title }}</h4>
                <div class="blog-meta">
                  <span><el-icon><View /></el-icon> {{ blog.thumb || 0 }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h3>限时秒杀</h3>
        <el-button text @click="router.push('/seckill')">查看全部</el-button>
      </div>
      <div class="seckill-preview">
        <el-card v-for="item in seckillItems" :key="item.id" class="seckill-card" shadow="hover">
          <div class="seckill-info">
            <h4>{{ item.title }}</h4>
            <div class="price">
              <span class="current">¥{{ item.payValue / 100 }}</span>
              <span class="original">¥{{ item.actualValue / 100 }}</span>
            </div>
            <el-progress :percentage="(item.stock || 0) / 100 * 100" :show-text="false" />
            <p class="stock">剩余 {{ item.stock }} 份</p>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useShopStore } from '@/stores/shop'
import { getBlogList } from '@/api/blog'
import type { Blog } from '@/types/blog'
import type { Voucher } from '@/types/voucher'

const router = useRouter()
const shopStore = useShopStore()

const banners = ref([
  { id: 1, title: '发现身边好店', desc: '探索本地优质商铺，享受品质生活', image: 'https://picsum.photos/1200/400?random=1', link: '/shop' },
  { id: 2, title: '分享探店心得', desc: '记录每一次美食之旅，分享你的发现', image: 'https://picsum.photos/1200/400?random=2', link: '/blog' },
  { id: 3, title: '超值优惠券', desc: '精选优惠，省钱更省心', image: 'https://picsum.photos/1200/400?random=3', link: '/voucher' },
])

const hotBlogs = ref<Blog[]>([])
const seckillItems = ref<Partial<Voucher>[]>([
  { id: 1, title: '美食套餐', payValue: 9900, actualValue: 19900, stock: 50 },
  { id: 2, title: '下午茶', payValue: 2900, actualValue: 5900, stock: 30 },
  { id: 3, title: '电影票', payValue: 1990, actualValue: 4900, stock: 100 },
])

const { shopTypes } = shopStore

function getFirstImage(images: string) {
  if (!images) return 'https://picsum.photos/300/200?random=1'
  const arr = images.split(',')
  return arr[0] || 'https://picsum.photos/300/200?random=1'
}

function goToShopType(typeId: number) {
  router.push({ path: '/shop', query: { typeId: String(typeId) } })
}

onMounted(async () => {
  await shopStore.fetchShopTypes()
  try {
    const res = await getBlogList(1)
    hotBlogs.value = res.records.slice(0, 4)
  } catch {
    console.log('Failed to load blogs')
  }
})
</script>

<style lang="scss" scoped>
.home-page {
  max-width: 1400px;
  margin: 0 auto;
}

.banner {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;

  .banner-item {
    height: 100%;
    background-size: cover;
    background-position: center;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(to right, rgba(0,0,0,0.5), transparent);
    }

    .banner-content {
      position: absolute;
      left: 60px;
      top: 50%;
      transform: translateY(-50%);
      color: #fff;
      z-index: 1;

      h2 {
        font-size: 36px;
        margin-bottom: 12px;
      }

      p {
        font-size: 18px;
        margin-bottom: 24px;
        opacity: 0.9;
      }
    }
  }
}

.section {
  margin-bottom: 40px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      font-size: 20px;
      font-weight: 600;
    }
  }
}

.category-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;

  .category-item {
    flex: 1;
    min-width: 120px;
    max-width: 150px;
    padding: 20px;
    background: #fff;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }

    .el-icon {
      color: #409eff;
      margin-bottom: 8px;
    }

    span {
      display: block;
      font-size: 14px;
      color: #333;
    }
  }
}

.blog-list {
  .blog-card {
    margin-bottom: 20px;
    cursor: pointer;

    .blog-image {
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

    .blog-info {
      h4 {
        font-size: 14px;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .blog-meta {
        display: flex;
        gap: 12px;
        font-size: 12px;
        color: #999;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }
}

.seckill-preview {
  display: flex;
  gap: 20px;
  overflow-x: auto;

  .seckill-card {
    min-width: 200px;
    cursor: pointer;

    .seckill-info {
      h4 {
        font-size: 16px;
        margin-bottom: 12px;
      }

      .price {
        margin-bottom: 12px;

        .current {
          font-size: 24px;
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

      .stock {
        font-size: 12px;
        color: #999;
        margin-top: 8px;
      }
    }
  }
}
</style>
