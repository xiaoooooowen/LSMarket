<template>
  <div class="blog-page">
    <el-row :gutter="20">
      <el-col :span="18">
        <div class="blog-list" v-loading="loading">
          <el-card v-for="blog in blogs" :key="blog.id" class="blog-card" shadow="hover">
            <div class="blog-header">
              <el-avatar :size="40" :src="blog.icon || defaultAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="author-info">
                <span class="author-name">{{ blog.name || '用户' }}</span>
                <span class="create-time">{{ formatDate(blog.createTime) }}</span>
              </div>
            </div>
            <div class="blog-content" @click="router.push(`/blog/${blog.id}`)">
              <h3>{{ blog.title }}</h3>
              <p>{{ blog.content?.slice(0, 200) }}...</p>
              <div class="blog-images" v-if="blog.images">
                <img 
                  v-for="(img, index) in getImages(blog.images).slice(0, 3)" 
                  :key="index" 
                  :src="img" 
                  alt="" 
                />
              </div>
            </div>
            <div class="blog-footer">
              <div class="blog-actions">
                <span :class="{ liked: blog.isLike }" @click="handleLike(blog)">
                  <el-icon><Star /></el-icon>
                  {{ blog.thumb || 0 }}
                </span>
                <span @click="router.push(`/blog/${blog.id}`)">
                  <el-icon><ChatDotRound /></el-icon>
                  评论
                </span>
              </div>
            </div>
          </el-card>
        </div>

        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadBlogs"
          />
        </div>
      </el-col>

      <el-col :span="6">
        <el-card class="publish-card">
          <el-button type="primary" style="width: 100%" @click="router.push('/blog/add')">
            <el-icon><Edit /></el-icon>
            发布笔记
          </el-button>
        </el-card>

        <el-card class="hot-card">
          <template #header>
            <span>热门话题</span>
          </template>
          <div class="hot-list">
            <div class="hot-item" v-for="i in 5" :key="i">
              <span class="rank" :class="{ top: i <= 3 }">{{ i }}</span>
              <span class="title">热门话题 {{ i }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBlogList, likeBlog } from '@/api/blog'
import { useUserStore } from '@/stores/user'
import type { Blog } from '@/types/blog'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const blogs = ref<Blog[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

function getImages(images: string) {
  if (!images) return []
  return images.split(',')
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString()
}

async function loadBlogs() {
  loading.value = true
  try {
    const res = await getBlogList(currentPage.value)
    blogs.value = res
    total.value = res.length * 10
  } catch {
    console.log('Failed to load blogs')
  } finally {
    loading.value = false
  }
}

async function handleLike(blog: Blog) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await likeBlog(blog.id)
    blog.isLike = !blog.isLike
    blog.thumb = (blog.thumb || 0) + (blog.isLike ? 1 : -1)
  } catch {
    console.log('Like failed')
  }
}

onMounted(() => {
  loadBlogs()
})
</script>

<style lang="scss" scoped>
.blog-page {
  max-width: 1200px;
  margin: 0 auto;
}

.blog-card {
  margin-bottom: 20px;

  .blog-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .author-info {
      .author-name {
        display: block;
        font-weight: 500;
      }

      .create-time {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .blog-content {
    cursor: pointer;

    h3 {
      font-size: 18px;
      margin-bottom: 12px;
    }

    p {
      color: #666;
      margin-bottom: 16px;
      line-height: 1.6;
    }

    .blog-images {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;

      img {
        width: 120px;
        height: 120px;
        object-fit: cover;
        border-radius: 8px;
      }
    }
  }

  .blog-footer {
    border-top: 1px solid #eee;
    padding-top: 12px;

    .blog-actions {
      display: flex;
      gap: 24px;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #666;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #409eff;
        }

        &.liked {
          color: #f56c6c;
        }
      }
    }
  }
}

.publish-card {
  margin-bottom: 20px;
}

.hot-card {
  .hot-list {
    .hot-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;

      .rank {
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0f0f0;
        border-radius: 4px;
        font-size: 12px;
        color: #999;

        &.top {
          background: #f56c6c;
          color: #fff;
        }
      }

      .title {
        font-size: 14px;
        color: #333;
        cursor: pointer;

        &:hover {
          color: #409eff;
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
