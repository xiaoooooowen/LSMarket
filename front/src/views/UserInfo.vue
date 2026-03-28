<template>
  <div class="user-info-page">
    <el-card v-if="userInfo">
      <div class="user-header">
        <el-avatar :size="100" :src="userIcon">
          <el-icon :size="50"><UserFilled /></el-icon>
        </el-avatar>
        <div class="user-basic">
          <h2>{{ userIcon ? '' : '用户' }}</h2>
          <div class="user-stats">
            <span>粉丝: {{ userInfo.fans }}</span>
            <span>关注: {{ userInfo.followee }}</span>
            <span>获赞: {{ userInfo.likes }}</span>
          </div>
        </div>
        <el-button 
          v-if="userStore.userInfo?.id !== userId"
          :type="isFollowing ? 'default' : 'primary'"
          @click="handleFollow"
        >
          {{ isFollowing ? '取消关注' : '关注' }}
        </el-button>
      </div>
      
      <div class="user-detail">
        <p><el-icon><Location /></el-icon> {{ userInfo.city || '未设置城市' }}</p>
        <p><el-icon><Document /></el-icon> {{ userInfo.introduce || '这个人很懒，什么都没写~' }}</p>
      </div>
    </el-card>

    <el-card class="blogs-card">
      <template #header>
        <span>TA的笔记</span>
      </template>
      <div class="blog-list" v-if="blogs.length > 0">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" v-for="blog in blogs" :key="blog.id">
            <el-card class="blog-item" shadow="hover" @click="router.push(`/blog/${blog.id}`)">
              <img :src="getFirstImage(blog.images)" alt="" class="blog-image" />
              <h4>{{ blog.title }}</h4>
              <div class="blog-meta">
                <span><el-icon><View /></el-icon> {{ blog.thumb || 0 }}</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      <el-empty v-else description="暂无笔记" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo } from '@/api/user'
import { getBlogOfUser } from '@/api/blog'
import { followUser, isFollow } from '@/api/follow'
import type { UserInfo } from '@/types/user'
import type { Blog } from '@/types/blog'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => Number(route.params.id))
const userInfo = ref<UserInfo | null>(null)
const blogs = ref<Blog[]>([])
const isFollowing = ref(false)

const userIcon = computed(() => {
  return userInfo.value ? '' : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
})

function getFirstImage(images: string) {
  if (!images) return 'https://picsum.photos/300/200?random=1'
  return images.split(',')[0]
}

async function handleFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await followUser(userId.value, !isFollowing.value)
    isFollowing.value = !isFollowing.value
    ElMessage.success(isFollowing.value ? '关注成功' : '取消关注成功')
  } catch {
    console.log('Follow failed')
  }
}

onMounted(async () => {
  try {
    userInfo.value = await getUserInfo(userId.value)
    const res = await getBlogOfUser(userId.value, 1)
    blogs.value = res.records
    
    if (userStore.isLoggedIn && userStore.userInfo?.id !== userId.value) {
      isFollowing.value = await isFollow(userId.value)
    }
  } catch {
    console.log('Failed to load user info')
  }
})
</script>

<style lang="scss" scoped>
.user-info-page {
  max-width: 1000px;
  margin: 0 auto;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;

  .user-basic {
    flex: 1;

    h2 {
      font-size: 24px;
      margin-bottom: 12px;
    }

    .user-stats {
      display: flex;
      gap: 24px;
      color: #666;
    }
  }
}

.user-detail {
  padding: 20px 0;

  p {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #666;
    margin-bottom: 8px;
  }
}

.blogs-card {
  margin-top: 20px;
}

.blog-list {
  .blog-item {
    margin-bottom: 20px;
    cursor: pointer;

    .blog-image {
      width: 100%;
      height: 150px;
      object-fit: cover;
      border-radius: 8px;
      margin-bottom: 12px;
    }

    h4 {
      font-size: 14px;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .blog-meta {
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
</style>
