<template>
  <div class="user-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-header">
            <el-avatar :size="80" :src="userStore.userInfo?.icon || defaultAvatar">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
            <h3>{{ userStore.userInfo?.nickName || '用户' }}</h3>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <span class="value">{{ userInfo?.fans || 0 }}</span>
              <span class="label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ userInfo?.followee || 0 }}</span>
              <span class="label">关注</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ userInfo?.likes || 0 }}</span>
              <span class="label">获赞</span>
            </div>
          </div>
          <div class="user-info-detail" v-if="userInfo">
            <p><el-icon><Location /></el-icon> {{ userInfo.city || '未设置城市' }}</p>
            <p><el-icon><Document /></el-icon> {{ userInfo.introduce || '这个人很懒，什么都没写~' }}</p>
          </div>
        </el-card>

        <el-card class="sign-card">
          <div class="sign-header">
            <h4>签到统计</h4>
            <el-tag>{{ signCount }} 天</el-tag>
          </div>
          <el-button 
            type="primary" 
            :disabled="hasSigned"
            @click="handleSign"
            style="width: 100%"
          >
            {{ hasSigned ? '今日已签到' : '立即签到' }}
          </el-button>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="我的笔记" name="blogs">
              <div class="blog-list" v-if="myBlogs.length > 0">
                <el-card v-for="blog in myBlogs" :key="blog.id" class="blog-item" shadow="hover">
                  <div class="blog-content" @click="router.push(`/blog/${blog.id}`)">
                    <img :src="getFirstImage(blog.images)" alt="" class="blog-image" />
                    <div class="blog-info">
                      <h4>{{ blog.title }}</h4>
                      <p>{{ blog.content?.slice(0, 100) }}...</p>
                      <div class="blog-meta">
                        <span><el-icon><View /></el-icon> {{ blog.thumb || 0 }}</span>
                        <span>{{ formatDate(blog.createTime) }}</span>
                      </div>
                    </div>
                  </div>
                </el-card>
              </div>
              <el-empty v-else description="暂无笔记" />
            </el-tab-pane>
            <el-tab-pane label="我的点赞" name="likes">
              <el-empty description="暂无点赞记录" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserInfo, sign, isSign, signCount } from '@/api/user'
import { getBlogOfUser } from '@/api/blog'
import type { UserInfo } from '@/types/user'
import type { Blog } from '@/types/blog'

const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const userInfo = ref<UserInfo | null>(null)
const hasSigned = ref(false)
const signCountValue = ref(0)
const activeTab = ref('blogs')
const myBlogs = ref<Blog[]>([])

const signCount = signCountValue

function getFirstImage(images: string) {
  if (!images) return 'https://picsum.photos/200/150?random=1'
  return images.split(',')[0]
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString()
}

async function handleSign() {
  try {
    await sign()
    hasSigned.value = true
    signCountValue.value++
    ElMessage.success('签到成功！')
  } catch {
    console.log('Sign failed')
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  
  if (userStore.userInfo?.id) {
    try {
      userInfo.value = await getUserInfo(userStore.userInfo.id)
      const blogs = await getBlogOfUser(userStore.userInfo.id, 1)
      myBlogs.value = blogs.records
    } catch {
      console.log('Failed to load user info')
    }
    
    try {
      hasSigned.value = await isSign()
      signCountValue.value = await signCount()
    } catch {
      console.log('Failed to load sign info')
    }
  }
})
</script>

<style lang="scss" scoped>
.user-page {
  max-width: 1200px;
  margin: 0 auto;
}

.user-card {
  text-align: center;
  margin-bottom: 20px;

  .user-header {
    padding: 20px 0;

    h3 {
      margin-top: 12px;
      font-size: 20px;
    }
  }

  .user-stats {
    display: flex;
    justify-content: space-around;
    padding: 20px 0;
    border-top: 1px solid #eee;
    border-bottom: 1px solid #eee;

    .stat-item {
      text-align: center;

      .value {
        display: block;
        font-size: 24px;
        font-weight: 600;
        color: #333;
      }

      .label {
        font-size: 14px;
        color: #999;
      }
    }
  }

  .user-info-detail {
    padding: 20px 0;
    text-align: left;

    p {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
      margin-bottom: 8px;
    }
  }
}

.sign-card {
  .sign-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h4 {
      font-size: 16px;
    }
  }
}

.blog-list {
  .blog-item {
    margin-bottom: 16px;
    cursor: pointer;

    .blog-content {
      display: flex;
      gap: 16px;

      .blog-image {
        width: 200px;
        height: 120px;
        object-fit: cover;
        border-radius: 8px;
      }

      .blog-info {
        flex: 1;

        h4 {
          font-size: 16px;
          margin-bottom: 8px;
        }

        p {
          color: #666;
          font-size: 14px;
          margin-bottom: 12px;
        }

        .blog-meta {
          display: flex;
          gap: 16px;
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
}
</style>
