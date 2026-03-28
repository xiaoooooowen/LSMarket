<template>
  <div class="not-found-page">
    <div class="content">
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;

  .content {
    text-align: center;

    h1 {
      font-size: 120px;
      color: #409eff;
      margin-bottom: 20px;
    }

    p {
      font-size: 24px;
      color: #666;
      margin-bottom: 30px;
    }
  }
}
</style>
