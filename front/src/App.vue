<template>
  <div class="app-container">
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo" @click="router.push('/')">
            <el-icon :size="28"><Shop /></el-icon>
            <span>凌水市集</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            :ellipsis="false"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/shop">商铺</el-menu-item>
            <el-menu-item index="/blog">探店笔记</el-menu-item>
            <el-menu-item index="/voucher">优惠券</el-menu-item>
            <el-menu-item index="/seckill">秒杀</el-menu-item>
          </el-menu>
          <div class="header-right">
            <template v-if="userStore.isLoggedIn">
              <el-dropdown @command="handleCommand">
                <div class="user-info">
                  <el-avatar :size="36" :src="userStore.userInfo?.icon || defaultAvatar">
                    <el-icon><UserFilled /></el-icon>
                  </el-avatar>
                  <span class="user-name">{{ userStore.userInfo?.nickName || '用户' }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="user">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="follow">
                      <el-icon><Star /></el-icon>我的关注
                    </el-dropdown-item>
                    <el-dropdown-item command="blog">
                      <el-icon><Edit /></el-icon>发布笔记
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" @click="router.push('/login')">登录</el-button>
            </template>
          </div>
        </div>
      </el-header>
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive :include="['Home', 'Shop', 'Blog']">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
      <el-footer class="app-footer">
        <p>© 2024 凌水市集 - 发现美好生活</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/shop')) return '/shop'
  if (path.startsWith('/blog')) return '/blog'
  if (path.startsWith('/voucher')) return '/voucher'
  if (path.startsWith('/seckill')) return '/seckill'
  return path
})

function handleMenuSelect(index: string) {
  router.push(index)
}

function handleCommand(command: string) {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'follow':
      router.push('/follow')
      break
    case 'blog':
      router.push('/blog/add')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}

onMounted(() => {
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
})
</script>

<style lang="scss" scoped>
.app-container {
  height: 100%;
  
  .el-container {
    height: 100%;
    flex-direction: column;
  }
}

.app-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: 600;
      color: #409eff;
      cursor: pointer;
      
      &:hover {
        opacity: 0.8;
      }
    }
    
    .el-menu {
      flex: 1;
      justify-content: center;
      border-bottom: none;
    }
    
    .header-right {
      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        
        .user-name {
          font-size: 14px;
          color: #333;
        }
      }
    }
  }
}

.app-main {
  flex: 1;
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}

.app-footer {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
