/**
 * 学生端路由配置
 */

import StudentLayout from '@/layouts/StudentLayout.vue'

const routes = [
  // 首页
  {
    path: '/student',
    redirect: '/student/dashboard'
  },
  {
    path: '/student',
    component: StudentLayout,
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      // 智能问答
      {
        path: 'qa/search',
        name: 'QASearch',
        component: () => import('@/views/student/QASearch.vue'),
        meta: { title: '智能问答' }
      },
      {
        path: 'qa/tickets',
        name: 'QATickets',
        component: () => import('@/views/student/QATickets.vue'),
        meta: { title: '我的工单' }
      },
      {
        path: 'qa/ticket/:id',
        name: 'QATicketDetail',
        component: () => import('@/views/student/QATicketDetail.vue'),
        meta: { title: '工单详情', parent: '/student/qa/tickets' }
      },
      // 政策知识库
      {
        path: 'policies',
        name: 'PolicyList',
        component: () => import('@/views/student/PolicyList.vue'),
        meta: { title: '政策知识库' }
      },
      {
        path: 'policies/:id',
        name: 'PolicyDetail',
        component: () => import('@/views/student/PolicyDetail.vue'),
        meta: { title: '政策详情', parent: '/student/policies' }
      },
      // 党团流程
      {
        path: 'party',
        name: 'PartyFlow',
        component: () => import('@/views/student/PartyFlow.vue'),
        meta: { title: '党团流程' }
      },
      {
        path: 'party/progress/:id',
        name: 'PartyProgress',
        component: () => import('@/views/student/PartyProgress.vue'),
        meta: { title: '流程详情', parent: '/student/party' }
      },
      // 通知公告
      {
        path: 'notices',
        name: 'NoticeList',
        component: () => import('@/views/student/NoticeList.vue'),
        meta: { title: '通知公告' }
      },
      {
        path: 'notices/:id',
        name: 'NoticeDetail',
        component: () => import('@/views/student/NoticeDetail.vue'),
        meta: { title: '通知详情', parent: '/student/notices' }
      },
      // 办事申请
      {
        path: 'affairs',
        name: 'AffairsList',
        component: () => import('@/views/student/AffairsList.vue'),
        meta: { title: '我的申请' }
      },
      {
        path: 'affairs/apply',
        name: 'AffairsApply',
        component: () => import('@/views/student/AffairsApply.vue'),
        meta: { title: '新建申请' }
      },
      {
        path: 'affairs/:id',
        name: 'AffairsDetail',
        component: () => import('@/views/student/AffairsDetail.vue'),
        meta: { title: '申请详情', parent: '/student/affairs' }
      },
      // 学业分析
      {
        path: 'academic',
        name: 'AcademicOverview',
        component: () => import('@/views/student/AcademicOverview.vue'),
        meta: { title: '学业概览' }
      },
      {
        path: 'academic/transcript',
        name: 'Transcript',
        component: () => import('@/views/student/Transcript.vue'),
        meta: { title: '成绩单' }
      },
      {
        path: 'academic/audit-report',
        name: 'AuditReport',
        component: () => import('@/views/student/AuditReport.vue'),
        meta: { title: '培养方案审核报告' }
      },
      {
        path: 'academic/course-plan',
        name: 'CoursePlan',
        component: () => import('@/views/student/CoursePlan.vue'),
        meta: { title: '课程规划' }
      },
      {
        path: 'academic/plan-detail/:id',
        name: 'PlanDetail',
        component: () => import('@/views/student/PlanDetail.vue'),
        meta: { title: '规划详情', parent: '/student/academic/course-plan' }
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/student/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'profile/edit',
        name: 'ProfileEdit',
        component: () => import('@/views/student/ProfileEdit.vue'),
        meta: { title: '编辑资料', parent: '/student/profile' }
      },
      {
        path: 'certificates',
        name: 'Certificates',
        component: () => import('@/views/student/Certificates.vue'),
        meta: { title: '电子证明' }
      },
      {
        path: 'certificates/apply',
        name: 'CertApply',
        component: () => import('@/views/student/CertApply.vue'),
        meta: { title: '申请证明', parent: '/student/certificates' }
      }
    ]
  }
]

export default routes
