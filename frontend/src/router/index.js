import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import StudentManagement from '../views/StudentManagement.vue'
import KnowledgeManagement from '../views/KnowledgeManagement.vue'
import NoticeManagement from '../views/NoticeManagement.vue'
import ApprovalManagement from '../views/ApprovalManagement.vue'
import WorkLogManagement from '../views/WorkLogManagement.vue'
import DataImport from '../views/DataImport.vue'
import OperationLog from '../views/OperationLog.vue'
import AdvisorScope from '../views/AdvisorScope.vue'
import SystemSettings from '../views/SystemSettings.vue'
import LoginLog from '../views/LoginLog.vue'
import PartyAffairs from '../views/PartyAffairs.vue'
import AcademicAnalysis from '../views/AcademicAnalysis.vue'
import AffairsCert from '../views/AffairsCert.vue'
import PolicyManagement from '../views/PolicyManagement.vue'
import QATicket from '../views/QATicket.vue'
import KeywordSearch from '../views/KeywordSearch.vue'
import DataScope from '../views/DataScope.vue'
import studentRoutes from './student'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/students',
    name: 'StudentManagement',
    component: StudentManagement
  },
  {
    path: '/knowledge',
    name: 'KnowledgeManagement',
    component: KnowledgeManagement
  },
  {
    path: '/notices',
    name: 'NoticeManagement',
    component: NoticeManagement
  },
  {
    path: '/approvals',
    name: 'ApprovalManagement',
    component: ApprovalManagement
  },
  {
    path: '/worklogs',
    name: 'WorkLogManagement',
    component: WorkLogManagement
  },
  {
    path: '/import',
    name: 'DataImport',
    component: DataImport
  },
  {
    path: '/logs',
    name: 'OperationLog',
    component: OperationLog
  },
  {
    path: '/scope',
    name: 'AdvisorScope',
    component: AdvisorScope
  },
  {
    path: '/settings',
    name: 'SystemSettings',
    component: SystemSettings
  },
  {
    path: '/login-logs',
    name: 'LoginLog',
    component: LoginLog
  },
  {
    path: '/party',
    name: 'PartyAffairs',
    component: PartyAffairs
  },
  {
    path: '/academic',
    name: 'AcademicAnalysis',
    component: AcademicAnalysis
  },
  {
    path: '/affairs',
    name: 'AffairsCert',
    component: AffairsCert
  },
  {
    path: '/policy',
    name: 'PolicyManagement',
    component: PolicyManagement
  },
  {
    path: '/qa-tickets',
    name: 'QATicket',
    component: QATicket
  },
  {
    path: '/keywords',
    name: 'KeywordSearch',
    component: KeywordSearch
  },
  {
    path: '/data-scope',
    name: 'DataScope',
    component: DataScope
  },
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes: [...routes, ...studentRoutes]
})

export default router
