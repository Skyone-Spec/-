<template>
  <div class="profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
          <el-button type="primary" @click="router.push('/student/profile/edit')">
            编辑资料
          </el-button>
        </div>
      </template>
      
      <div v-if="userInfo" class="profile-content">
        <!-- 头像和基本信息 -->
        <div class="profile-header">
          <div class="avatar-section">
            <el-avatar :size="100" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <el-button type="primary" link>更换头像</el-button>
          </div>
          <div class="basic-info">
            <h2>{{ userInfo.fullName }}</h2>
            <p class="student-no">学号：{{ userInfo.studentNo }}</p>
            <div class="info-tags">
              <el-tag>{{ userInfo.major }}</el-tag>
              <el-tag type="info">{{ userInfo.grade }}</el-tag>
              <el-tag type="info">{{ userInfo.className }}</el-tag>
            </div>
          </div>
        </div>
        
        <!-- 详细信息 -->
        <div class="info-section">
          <h3>基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ userInfo.fullName }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ userInfo.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ userInfo.gender }}</el-descriptions-item>
            <el-descriptions-item label="民族">{{ userInfo.nation }}</el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ userInfo.birthDate }}</el-descriptions-item>
            <el-descriptions-item label="政治面貌">{{ userInfo.politicalStatus }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="info-section">
          <h3>联系信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="手机号码">{{ userInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="电子邮箱">{{ userInfo.email }}</el-descriptions-item>
            <el-descriptions-item label="QQ号码">{{ userInfo.qq }}</el-descriptions-item>
            <el-descriptions-item label="宿舍地址">{{ userInfo.dormitory }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="info-section">
          <h3>其他信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="辅导员">{{ userInfo.counselor }}</el-descriptions-item>
            <el-descriptions-item label="班主任">{{ userInfo.classTeacher }}</el-descriptions-item>
            <el-descriptions-item label="入学年份">{{ userInfo.enrollmentYear }}</el-descriptions-item>
            <el-descriptions-item label="学生类型">{{ userInfo.studentType }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h3>快捷操作</h3>
          <div class="action-grid">
            <div class="action-item" @click="router.push('/student/certificates')">
              <el-icon><Medal /></el-icon>
              <span>电子证明</span>
            </div>
            <div class="action-item" @click="handlePassword">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </div>
            <div class="action-item" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Medal, Lock, SwitchButton } from '@element-plus/icons-vue'
import { useStudentStore } from '@/stores/student'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const store = useStudentStore()

const userInfo = ref(null)

function handlePassword() {
  ElMessageBox.prompt('请输入新密码', '修改密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'password'
  }).then(() => {
    // 实际API调用
  })
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    store.logout()
    router.push('/student/login')
  })
}

onMounted(() => {
  userInfo.value = store.userInfo || {
    fullName: '张三',
    studentNo: '2023100001',
    major: '计算机科学与技术',
    grade: '2023级',
    className: '计算机2301班',
    gender: '男',
    nation: '汉族',
    birthDate: '2004-05-01',
    politicalStatus: '共青团员',
    phone: '138****5678',
    email: 'zhangsan@example.com',
    qq: '123456789',
    dormitory: '学生公寓3号楼301室',
    counselor: '李老师',
    classTeacher: '王老师',
    enrollmentYear: '2023年',
    studentType: '普通本科'
  }
})
</script>

<style scoped>
.profile {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-header {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  margin-bottom: 24px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.basic-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.student-no {
  margin: 0 0 12px 0;
  opacity: 0.9;
}

.info-tags {
  display: flex;
  gap: 8px;
}

.info-section {
  margin-bottom: 24px;
}

.info-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.quick-actions h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.action-item .el-icon {
  font-size: 32px;
  color: #409EFF;
}

.action-item span {
  font-size: 14px;
  color: #606266;
}
</style>
