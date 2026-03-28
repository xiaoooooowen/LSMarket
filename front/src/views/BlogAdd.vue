<template>
  <div class="blog-add-page">
    <el-card>
      <template #header>
        <span>发布探店笔记</span>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="分享你的探店体验..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="图片" prop="images">
          <el-upload
            v-model:file-list="fileList"
            action="/api/blog/image"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="关联商铺">
          <el-select
            v-model="form.shopId"
            filterable
            remote
            reserve-keyword
            placeholder="搜索商铺"
            :remote-method="searchShops"
            :loading="searching"
            clearable
          >
            <el-option
              v-for="shop in shopOptions"
              :key="shop.id"
              :label="shop.name"
              :value="shop.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            发布
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { saveBlog } from '@/api/blog'
import { getShopList } from '@/api/shop'
import type { Shop } from '@/types/shop'
import type { UploadFile } from 'element-plus'

const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const searching = ref(false)
const fileList = ref<UploadFile[]>([])
const shopOptions = ref<Shop[]>([])

const form = reactive({
  title: '',
  content: '',
  images: '',
  shopId: null as number | null
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度在 5 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 20, message: '内容至少 20 个字符', trigger: 'blur' }
  ]
}

function handleUploadSuccess(response: any, file: UploadFile) {
  if (response.success) {
    const images = form.images ? form.images.split(',') : []
    images.push(response.data)
    form.images = images.join(',')
  }
}

function handleRemove(file: UploadFile) {
  const images = form.images.split(',').filter(img => {
    return !file.url?.includes(img)
  })
  form.images = images.join(',')
}

async function searchShops(query: string) {
  if (!query) {
    shopOptions.value = []
    return
  }
  
  searching.value = true
  try {
    const res = await getShopList(undefined, 1, 20)
    shopOptions.value = res.records.filter(shop => 
      shop.name.toLowerCase().includes(query.toLowerCase())
    )
  } catch {
    console.log('Search failed')
  } finally {
    searching.value = false
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    await saveBlog({
      title: form.title,
      content: form.content,
      images: form.images,
      shopId: form.shopId || undefined
    })
    
    ElMessage.success('发布成功')
    router.push('/blog')
  } catch {
    console.log('Submit failed')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.blog-add-page {
  max-width: 800px;
  margin: 0 auto;
}
</style>
