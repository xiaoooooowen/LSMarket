<template>
  <div class="blog-detail-page" v-loading="loading">
    <el-row :gutter="20" v-if="blog">
      <el-col :span="18">
        <el-card class="blog-card">
          <div class="blog-header">
            <el-avatar :size="50" :src="blog.icon || defaultAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <div class="author-info">
              <span class="author-name">{{ blog.name || '用户' }}</span>
              <span class="create-time">{{ formatDate(blog.createTime) }}</span>
            </div>
            <el-button 
              v-if="userStore.userInfo?.id !== blog.userId"
              :type="isFollowing ? 'default' : 'primary'"
              size="small"
              @click="handleFollow"
            >
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
          </div>

          <div class="blog-content">
            <h2>{{ blog.title }}</h2>
            <div class="content-text" v-html="formatContent(blog.content)"></div>
            <div class="blog-images" v-if="blog.images">
              <img 
                v-for="(img, index) in getImages(blog.images)" 
                :key="index" 
                :src="img" 
                alt="" 
                @click="previewImage(img)"
              />
            </div>
          </div>

          <div class="blog-footer">
            <div class="blog-actions">
              <span :class="{ liked: blog.isLike }" @click="handleLike">
                <el-icon><Star /></el-icon>
                {{ blog.thumb || 0 }}
              </span>
              <span>
                <el-icon><ChatDotRound /></el-icon>
                评论
              </span>
            </div>
          </div>
        </el-card>

        <el-card class="comments-card">
          <template #header>
            <span>评论 ({{ comments.length }})</span>
          </template>
          
          <div class="comment-input">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
            />
            <el-button type="primary" @click="handleComment" :loading="submitting">
              发表评论
            </el-button>
          </div>

          <div class="comments-list">
            <div class="comment-item" v-for="comment in comments" :key="comment.id">
              <el-avatar :size="40" :src="comment.icon || defaultAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.nickName || '用户' }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
                <p>{{ comment.content }}</p>
              </div>
            </div>
            <el-empty v-if="comments.length === 0" description="暂无评论" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="author-card">
          <template #header>
            <span>作者信息</span>
          </template>
          <div class="author-profile" @click="router.push(`/user/${blog.userId}`)">
            <el-avatar :size="60" :src="blog.icon || defaultAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <h4>{{ blog.name || '用户' }}</h4>
          </div>
        </el-card>

        <el-card class="related-card">
          <template #header>
            <span>相关推荐</span>
          </template>
          <el-empty description="暂无推荐" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getBlogById, likeBlog, getBlogComments, addComment } from '@/api/blog'
import { followUser, isFollow } from '@/api/follow'
import type { Blog } from '@/types/blog'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const blog = ref<Blog | null>(null)
const loading = ref(false)
const isFollowing = ref(false)
const comments = ref<any[]>([])
const commentContent = ref('')
const submitting = ref(false)

const blogId = computed(() => Number(route.params.id))

function getImages(images: string) {
  if (!images) return []
  return images.split(',')
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString()
}

function formatContent(content: string | undefined) {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

function previewImage(img: string) {
  window.open(img, '_blank')
}

async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!blog.value) return
  
  try {
    await likeBlog(blog.value.id)
    blog.value.isLike = !blog.value.isLike
    blog.value.thumb = (blog.value.thumb || 0) + (blog.value.isLike ? 1 : -1)
  } catch {
    console.log('Like failed')
  }
}

async function handleFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!blog.value) return
  
  try {
    await followUser(blog.value.userId, !isFollowing.value)
    isFollowing.value = !isFollowing.value
    ElMessage.success(isFollowing.value ? '关注成功' : '取消关注成功')
  } catch {
    console.log('Follow failed')
  }
}

async function handleComment() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  submitting.value = true
  try {
    await addComment(blogId.value, commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadComments()
  } catch {
    console.log('Comment failed')
  } finally {
    submitting.value = false
  }
}

async function loadComments() {
  try {
    const res = await getBlogComments(blogId.value, 1)
    comments.value = Array.isArray(res) ? res : []
  } catch {
    console.log('Failed to load comments')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    blog.value = await getBlogById(blogId.value)
    if (userStore.isLoggedIn && blog.value.userId !== userStore.userInfo?.id) {
      isFollowing.value = await isFollow(blog.value.userId)
    }
    loadComments()
  } catch {
    console.log('Failed to load blog')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.blog-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.blog-card {
  .blog-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 24px;

    .author-info {
      flex: 1;

      .author-name {
        display: block;
        font-weight: 500;
        font-size: 16px;
      }

      .create-time {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .blog-content {
    h2 {
      font-size: 24px;
      margin-bottom: 20px;
    }

    .content-text {
      line-height: 1.8;
      color: #333;
      margin-bottom: 24px;
    }

    .blog-images {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 24px;

      img {
        max-width: 300px;
        max-height: 300px;
        object-fit: cover;
        border-radius: 8px;
        cursor: pointer;
        transition: transform 0.3s;

        &:hover {
          transform: scale(1.02);
        }
      }
    }
  }

  .blog-footer {
    border-top: 1px solid #eee;
    padding-top: 16px;

    .blog-actions {
      display: flex;
      gap: 32px;

      span {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
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

.comments-card {
  margin-top: 20px;

  .comment-input {
    margin-bottom: 24px;

    .el-button {
      margin-top: 12px;
    }
  }

  .comments-list {
    .comment-item {
      display: flex;
      gap: 12px;
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;

      .comment-content {
        flex: 1;

        .comment-header {
          margin-bottom: 8px;

          .comment-author {
            font-weight: 500;
            margin-right: 12px;
          }

          .comment-time {
            font-size: 12px;
            color: #999;
          }
        }

        p {
          color: #333;
          line-height: 1.6;
        }
      }
    }
  }
}

.author-card {
  .author-profile {
    text-align: center;
    cursor: pointer;

    h4 {
      margin-top: 12px;
    }
  }
}

.related-card {
  margin-top: 20px;
}
</style>
