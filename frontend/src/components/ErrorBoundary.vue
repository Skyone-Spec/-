<template>
  <div class="error-boundary">
    <el-result
      v-if="hasError"
      icon="error"
      title="页面出错了"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="handleReload">
          重新加载
        </el-button>
        <el-button @click="handleGoHome">
          返回首页
        </el-button>
      </template>
    </el-result>
    <slot v-else />
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const hasError = ref(false)
const errorMessage = ref('抱歉，页面加载时出现了问题')

onErrorCaptured((err, instance, info) => {
  console.error('Error captured:', err)
  console.error('Component:', instance)
  console.error('Error info:', info)
  
  hasError.value = true
  errorMessage.value = err.message || '抱歉，页面加载时出现了问题'
  
  return false // 阻止错误继续传播
})

function handleReload() {
  hasError.value = false
  window.location.reload()
}

function handleGoHome() {
  router.push('/student/dashboard')
}
</script>

<style scoped>
.error-boundary {
  min-height: 100%;
}
</style>
