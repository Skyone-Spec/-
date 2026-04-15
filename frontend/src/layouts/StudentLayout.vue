<template>
  <el-container class="student-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">学生服务</span>
        <span v-else>学</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/student/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-sub-menu index="qa">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>智能问答</span>
          </template>
          <el-menu-item index="/student/qa/search">问答搜索</el-menu-item>
          <el-menu-item index="/student/qa/tickets">我的工单</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/student/policies">
          <el-icon><Document /></el-icon>
          <template #title>政策知识库</template>
        </el-menu-item>
        
        <el-sub-menu index="party">
          <template #title>
            <el-icon><Flag /></el-icon>
            <span>党团流程</span>
          </template>
          <el-menu-item index="/student/party">我的流程</el-menu-item>
          <el-menu-item index="/student/party/progress">进度详情</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/student/notices">
          <el-icon><Bell /></el-icon>
          <template #title>通知公告</template>
        </el-menu-item>
        
        <el-sub-menu index="affairs">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>办事申请</span>
          </template>
          <el-menu-item index="/student/affairs/apply">新建申请</el-menu-item>
          <el-menu-item index="/student/affairs">我的申请</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="cert">
          <template #title>
            <el-icon><Medal /></el-icon>
            <span>电子证明</span>
          </template>
          <el-menu-item index="/student/cert/apply">申请证明</el-menu-item>
          <el-menu-item index="/student/cert/records">申请记录</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/student/academic">
          <el-icon><TrendCharts /></el-icon>
          <template #title>学业分析</template>
        </el-menu-item>
        
        <el-menu-item index="/student/profile">
          <el-icon><User /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        
        <div class="header-right">
          <!-- 未读通知数 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notice-badge">
            <el-icon class="header-icon"><Bell /></el-icon>
          </el-badge>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.currentUser?.fullName?.charAt(0) || 'S' }}
              </el-avatar>
              <span class="user-name">{{ userStore.currentUser?.fullName || '学生' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/student'
import { ElMessage } from 'element-plus'
import {
  HomeFilled,
  ChatDotRound,
  Document,
  Flag,
  Bell,
  Tickets,
  TrendCharts,
  User,
  Fold,
  Expand,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const unreadCount = ref(0)

const activeMenu = computed(() => route.path)

// 处理下拉菜单命令
function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/student/profile')
      break
    case 'settings':
      router.push('/student/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
async function handleLogout() {
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/student/login')
  } catch (error) {
    console.error('退出失败:', error)
  }
}

// 获取未读通知数
async function fetchUnreadCount() {
  // TODO: 调用API获取未读通知数
  // const res = await getUnreadNoticeCount()
  // unreadCount.value = res.data
}

onMounted(() => {
  fetchUnreadCount()
})
</script>

<style scoped>
.student-layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3a4a;
}

.logo img {
  width: 32px;
  height: 32px;
  margin-right: 8px;
}

.sidebar-menu {
  border-right: none;
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notice-badge {
  cursor: pointer;
}

.header-icon {
  font-size: 20px;
  color: #606266;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-avatar {
  background-color: #409EFF;
}

.user-name {
  color: #606266;
}

.main-content {
  background-color: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}
</style>
