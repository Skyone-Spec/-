<template>
  <div class="course-plan">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程规划</span>
          <el-button type="primary">新建规划</el-button>
        </div>
      </template>
      
      <el-table :data="plans" stripe>
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="title" label="规划名称" />
        <el-table-column prop="courseCount" label="课程数" width="100" align="center" />
        <el-table-column prop="credit" label="学分" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const plans = ref([
  { id: 1, semester: '2026-2027-1', title: '大三上学期课程规划', courseCount: 6, credit: 22, status: 'pending' },
  { id: 2, semester: '2025-2026-2', title: '大二下学期课程规划', courseCount: 7, credit: 25, status: 'in_progress' },
  { id: 3, semester: '2025-2026-1', title: '大二上学期课程规划', courseCount: 6, credit: 24, status: 'completed' }
])

function getStatusType(status) {
  return { completed: 'success', in_progress: 'primary', pending: 'info' }[status] || 'info'
}

function getStatusText(status) {
  return { completed: '已完成', in_progress: '进行中', pending: '待完成' }[status] || status
}

function goToDetail(row) {
  router.push(`/student/academic/plan-detail/${row.id}`)
}
</script>

<style scoped>
.course-plan { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
