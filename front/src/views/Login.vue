<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>凌水市集</h2>
      <p class="subtitle">发现美好生活</p>
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="phone">
          <el-input 
            v-model="loginForm.phone" 
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="code">
          <el-input 
            v-model="loginForm.code" 
            placeholder="请输入验证码"
            :prefix-icon="Lock"
            size="large"
          >
            <template #append>
              <el-button 
                @click="sendCode" 
                :disabled="countdown > 0"
                :loading="sendingCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleLogin" 
            :loading="loading"
            size="large"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const loginForm = reactive({
  phone: '',
  code: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

async function sendCode() {
  try {
    await formRef.value.validateField('phone')
    sendingCode.value = true
    await userStore.sendCode(loginForm.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch {
    console.log('Validation failed')
  } finally {
    sendingCode.value = false
  }
}

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true
    await userStore.login(loginForm.phone, loginForm.code)
    await userStore.fetchUserInfo()
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    console.log('Login failed')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 140px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: -20px;
  padding: 20px;
}

.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 16px;

  h2 {
    text-align: center;
    margin-bottom: 8px;
    color: #333;
    font-size: 28px;
  }

  .subtitle {
    text-align: center;
    color: #999;
    margin-bottom: 30px;
  }
}
</style>
