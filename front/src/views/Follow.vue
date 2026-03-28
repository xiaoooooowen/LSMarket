<template>
  <div class="follow-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的关注" name="following">
        <div class="user-list" v-if="followingList.length > 0">
          <el-card v-for="user in followingList" :key="user.id" class="user-item" shadow="hover">
            <div class="user-info" @click="router.push(`/user/${user.id}`)">
              <el-avatar :size="60" :src="user.icon || defaultAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-detail">
                <h4>{{ user.nickName || '用户' }}</h4>
                <p>{{ user.introduce || '这个人很懒，什么都没写~' }}</p>
              </div>
            </div>
            <el-button type="primary" plain @click="handleUnfollow(user)">
              取消关注
            </el-button>
          </el-card>
        </div>
        <el-empty v-else description="暂无关注" />
      </el-tab-pane>
      
      <el-tab-pane label="我的粉丝" name="followers">
        <el-empty description="暂无粉丝" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFollowList, followUser } from '@/api/follow'

const router = useRouter()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const activeTab = ref('following')
const followingList = ref<any[]>([])

async function handleUnfollow(user: any) {
  try {
    await followUser(user.id, false)
    followingList.value = followingList.value.filter(u => u.id !== user.id)
    ElMessage.success('取消关注成功')
  } catch {
    console.log('Unfollow failed')
  }
}

onMounted(async () => {
  try {
    followingList.value = await getFollowList()
  } catch {
    console.log('Failed to load following list')
  }
})
</script>

<style lang="scss" scoped>
.follow-page {
  max-width: 800px;
  margin: 0 auto;
}

.user-list {
  .user-item {
    margin-bottom: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .user-info {
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      flex: 1;

      .user-detail {
        h4 {
          font-size: 16px;
          margin-bottom: 4px;
        }

        p {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }
}
</style>
