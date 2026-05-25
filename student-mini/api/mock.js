/**
 * Mock 数据 - 用于本地测试
 * 将此文件配置到 request.js 中可拦截 API 请求返回模拟数据
 */

// 统一使用 CommonJS 语法，避免 ES6/CommonJS 混用导致问题
const mockData = {
  // 首页数据
  '/banners': {
    data: [
      { id: 1, image: '/static/images/banner1.png', title: '欢迎使用学院服务平台' },
      { id: 2, image: '/static/images/banner2.png', title: '新学期开始了' }
    ]
  },
  '/shortcuts': {
    data: [
      { id: 1, name: '学业分析', icon: '📊', bgColor: '#e6f0ff' },
      { id: 2, name: '证明申请', icon: '📝', bgColor: '#f6ffed' },
      { id: 3, name: '政策文件', icon: '📋', bgColor: '#fff7e6' },
      { id: 4, name: '党团流程', icon: '🎗️', bgColor: '#fff2f0' }
    ]
  },
  '/news': {
    data: [
      { id: 1, title: '关于2026年春季学期选课通知', time: '2026-04-10' },
      { id: 2, title: '图书馆清明节开放时间调整', time: '2026-04-08' },
      { id: 3, title: '2026届毕业生论文答辩安排', time: '2026-04-05' }
    ]
  },
  
  // 消息列表
  '/messages': {
    data: {
      notifications: [
        { id: '1', title: '选课通知', summary: '2026年春季学期选课将于...', time: '2026-04-10', important: true },
        { id: '2', title: '考试成绩发布', summary: '您的2025-2026学年第一学期成绩已发布', time: '2026-04-08' }
      ],
      announcements: [
        { id: '1', title: '清明节放假通知', content: '清明节放假安排...', time: '2026-04-03' }
      ]
    }
  },
  
  // 政策文件列表
  '/kb/policies': {
    data: {
      list: [
        { id: '1', title: '学生管理条例', publishDate: '2026-03-15', category: '规章制度' },
        { id: '2', title: '学分认定办法', publishDate: '2026-03-10', category: '教学管理' },
        { id: '3', title: '奖学金评定细则', publishDate: '2026-02-28', category: '奖励资助' }
      ]
    }
  },
  
  // 政策详情
  '/kb/policies/1': {
    data: {
      id: '1',
      title: '学生管理条例',
      publishDate: '2026-03-15',
      source: '学生处',
      content: '第一章 总则\n\n第一条 为维护学校正常教育教学秩序...',
      attachments: [
        { id: '1', name: '学生管理条例.pdf', url: '/files/student-rules.pdf' }
      ]
    }
  },
  
  // 通知列表
  '/notices': {
    data: {
      list: [
        { id: '1', title: '关于开展2026年大学生创新创业训练计划项目申报工作的通知', publishTime: '2026-04-10', important: true },
        { id: '2', title: '2026年春季学期期中考试安排', publishTime: '2026-04-08' }
      ]
    }
  },
  
  // FAQ列表
  '/kb/faqs': {
    data: {
      list: [
        { id: '1', question: '如何申请成绩单？', answer: '您好，可以通过"证明申请"功能申请中英文成绩单...' },
        { id: '2', question: '学分认定需要哪些材料？', answer: '学分认定需要提供原始成绩单、课程大纲...' },
        { id: '3', question: '如何办理在读证明？', answer: '在读证明可通过"证明申请"模块申请...' }
      ]
    }
  },
  
  // 我的工单
  '/kb/tickets': {
    data: {
      list: [
        { id: '1', content: '关于学分认定的咨询', status: 'replied', statusText: '已回复', createTime: '2026-04-05', replyCount: 2 },
        { id: '2', content: '成绩单申请进度查询', status: 'pending', statusText: '处理中', createTime: '2026-04-08', replyCount: 0 }
      ]
    }
  },
  
  // 证明申请列表
  '/affairs': {
    data: {
      list: [
        { id: '1', typeName: '在读证明', status: 'approved', statusText: '已通过', createTime: '2026-04-01' },
        { id: '2', typeName: '成绩单', status: 'submitted', statusText: '审核中', createTime: '2026-04-08' }
      ]
    }
  },
  
  // 申请详情
  '/affairs/1': {
    data: {
      id: '1',
      typeName: '在读证明',
      status: 'approved',
      statusText: '已通过',
      purpose: '用于申请签证',
      remark: '',
      createTime: '2026-04-01 10:30',
      canCancel: false,
      approvals: [
        { id: '1', approverName: '辅导员 张老师', status: 'completed', resultText: '通过', time: '2026-04-01 14:00', comment: '同意' },
        { id: '2', approverName: '教务处 李老师', status: 'completed', resultText: '通过', time: '2026-04-02 09:00', comment: '已盖章' }
      ],
      generatedFile: { name: '在读证明.pdf', url: '/files/proof.pdf' }
    }
  },
  
  // 学业分析
  '/academic/overview': {
    data: {
      gpa: '3.65',
      credits: 128,
      rank: '第15名',
      totalCredits: 160
    }
  },
  
  // 学业报告
  '/academic/report': {
    data: {
      gpa: '3.65',
      credits: 128,
      rank: '15/180',
      warningCount: 0,
      gradeDistribution: [
        { range: '90-100', percent: 40, count: 8, color: '#52c41a' },
        { range: '80-89', percent: 30, count: 6, color: '#1677ff' },
        { range: '70-79', percent: 20, count: 4, color: '#faad14' },
        { range: '60-69', percent: 10, count: 2, color: '#ff7a45' }
      ],
      gpaTrend: [
        { semester: '2023-1', gpa: '3.5' },
        { semester: '2023-2', gpa: '3.6' },
        { semester: '2024-1', gpa: '3.7' },
        { semester: '2024-2', gpa: '3.65' }
      ],
      warnings: []
    }
  },
  
  // 党团进度 - 入党
  '/party/my-progress': {
    data: {
      title: '入党进程',
      subtitle: '积极分子 → 发展对象 → 预备党员 → 正式党员',
      currentStage: '发展对象',
      progressPercent: 50,
      stages: [
        { id: '1', name: '提交入党申请书', status: 'completed', statusText: '已完成' },
        { id: '2', name: '确定为积极分子', status: 'completed', statusText: '已完成' },
        { id: '3', name: '参加培训考核', status: 'completed', statusText: '已完成' },
        { id: '4', name: '确定为发展对象', status: 'processing', statusText: '进行中' },
        { id: '5', name: '接收为预备党员', status: 'pending', statusText: '待进行' },
        { id: '6', name: '转为正式党员', status: 'pending', statusText: '待进行' }
      ]
    }
  },
  
  // 党团流程 - 入党
  '/party/flows': {
    data: [
      { id: '1', name: '提交入党申请书', description: '向党组织提交入党申请' },
      { id: '2', name: '确定为积极分子', description: '经支部大会讨论确定' },
      { id: '3', name: '参加培训考核', description: '完成党校培训并通过' },
      { id: '4', name: '确定为发展对象', description: '经培养考察确定' },
      { id: '5', name: '政治审查', description: '形成政审材料' },
      { id: '6', name: '支部大会讨论', description: '填写入党志愿书' },
      { id: '7', name: '上级党委审批', description: '审批通过' },
      { id: '8', name: '预备期考察', description: '一年预备期' },
      { id: '9', name: '转为正式党员', description: '转正审批' }
    ]
  },
  
  // 党团进度 - 入团
  '/party/league-progress': {
    data: {
      title: '入团进程',
      subtitle: '入团积极分子 → 共青团员',
      currentStage: '入团积极分子',
      progressPercent: 30,
      stages: [
        { id: '1', name: '提交入团申请书', status: 'completed', statusText: '已完成' },
        { id: '2', name: '确定为入团积极分子', status: 'completed', statusText: '已完成' },
        { id: '3', name: '参加团课培训', status: 'processing', statusText: '进行中' },
        { id: '4', name: '接收为共青团员', status: 'pending', statusText: '待进行' }
      ]
    }
  },
  
  // 进度详情 - 入党
  '/party/progress/party': {
    data: {
      title: '入党进程',
      subtitle: '积极分子 → 发展对象 → 预备党员 → 正式党员',
      currentStage: '确定为发展对象',
      progressPercent: 50,
      canApply: false,
      applyBtnText: '入党',
      stages: [
        { id: '1', name: '提交入党申请书', status: 'completed', statusText: '已完成', time: '2024-03', description: '向党组织提交入党申请，表明入党动机和对党的认识' },
        { id: '2', name: '确定为积极分子', status: 'completed', statusText: '已完成', time: '2024-06', description: '经支部大会讨论确定为入党积极分子，进入培养考察期' },
        { id: '3', name: '参加培训考核', status: 'completed', statusText: '已完成', time: '2024-09', description: '完成党校培训课程并通过考核，取得结业证书' },
        { id: '4', name: '确定为发展对象', status: 'processing', statusText: '进行中', time: '2025-03', description: '经培养考察、民主推荐、支委会研究确定为发展对象' },
        { id: '5', name: '政治审查', status: 'pending', statusText: '待进行', description: '对发展对象进行政治审查，形成政审材料' },
        { id: '6', name: '支部大会讨论', status: 'pending', statusText: '待进行', description: '支部大会讨论接收预备党员，填写入党志愿书' },
        { id: '7', name: '上级党委审批', status: 'pending', statusText: '待进行', description: '上级党委审批通过，成为预备党员' },
        { id: '8', name: '预备期考察', status: 'pending', statusText: '待进行', description: '一年预备期，接受党组织教育和考察' },
        { id: '9', name: '转为正式党员', status: 'pending', statusText: '待进行', description: '预备期满后提出转正申请，经审批转为正式党员' }
      ]
    }
  },
  
  // 进度详情 - 入团
  '/party/progress/league': {
    data: {
      title: '入团进程',
      subtitle: '入团积极分子 → 共青团员',
      currentStage: '参加团课培训',
      progressPercent: 30,
      canApply: false,
      applyBtnText: '入团',
      stages: [
        { id: '1', name: '提交入团申请书', status: 'completed', statusText: '已完成', time: '2025-09', description: '向团组织提交入团申请，表明入团动机和对共青团的认识' },
        { id: '2', name: '确定为入团积极分子', status: 'completed', statusText: '已完成', time: '2025-10', description: '经支部大会讨论确定为入团积极分子，进入培养考察期' },
        { id: '3', name: '参加团课培训', status: 'processing', statusText: '进行中', description: '完成团课培训课程并通过考核，取得培训合格证书' },
        { id: '4', name: '政治审查', status: 'pending', statusText: '待进行', description: '对入团积极分子进行政治审查' },
        { id: '5', name: '支部大会讨论', status: 'pending', statusText: '待进行', description: '支部大会讨论接收新团员，填写入团志愿书' },
        { id: '6', name: '上级团组织审批', status: 'pending', statusText: '待进行', description: '上级团组织审批通过，正式成为共青团员' }
      ]
    }
  },
  
  // 党团流程 - 入团
  '/party/league-flows': {
    data: [
      { id: '1', name: '提交入团申请书', description: '向团组织提交入团申请' },
      { id: '2', name: '确定为入团积极分子', description: '经支部大会讨论确定' },
      { id: '3', name: '参加团课培训', description: '完成团课培训并通过' },
      { id: '4', name: '政治审查', description: '进行政治审查' },
      { id: '5', name: '支部大会讨论', description: '填写入团志愿书' },
      { id: '6', name: '上级团组织审批', description: '审批通过' }
    ]
  },
  
  // 搜索结果
  '/search': {
    data: [
      { id: '1', type: 'policy', typeName: '政策', title: '学生管理条例', summary: '学生管理相关规定...', time: '2026-03-15' },
      { id: '2', type: 'notice', typeName: '通知', title: '选课通知', summary: '关于2026年春季学期选课...', time: '2026-04-10' }
    ]
  },
  
  // 登录（模拟）
  '/auth/login': {
    data: {
      token: 'mock_token_12345',
      userInfo: {
        id: '2021001234',
        name: '张三',
        studentId: '2021001234',
        avatar: '',
        major: '计算机科学与技术',
        grade: '2021级'
      }
    }
  },
  
  // 账号绑定（Mock登录）
  '/auth/bindAccount': {
    data: {
      token: 'mock_token_12345',
      userInfo: {
        id: '2021001234',
        name: '张三',
        studentId: '2021001234',
        avatar: '',
        major: '计算机科学与技术',
        grade: '2021级'
      }
    }
  },
  
  // 账号绑定（Mock登录 - bind接口）
  '/auth/bind': {
    data: {
      token: 'mock_token_12345',
      userInfo: {
        id: '2021001234',
        name: '张三',
        studentId: '2021001234',
        avatar: '',
        major: '计算机科学与技术',
        grade: '2021级'
      }
    }
  }
}

/**
 * 获取 mock 数据
 * @param {string} url - API 路径
 * @param {object} params - 请求参数
 */
const honorShowcases = [
  {
    id: 101,
    awardYear: 2024,
    honorCategory: '信息学院国家奖学金',
    recipientType: 'PERSONAL',
    title: '2024年信息学院国家奖学金',
    description: '展示信息学院在国家奖学金评选中表现突出的学生代表。'
  },
  {
    id: 102,
    awardYear: 2024,
    honorCategory: '优秀学生集体',
    recipientType: 'COLLECTIVE',
    title: '2024年信息学院优秀学生集体',
    description: '展示班级建设、学风引领和志愿服务等方面表现突出的优秀集体。'
  },
  {
    id: 103,
    awardYear: 2023,
    honorCategory: '社会工作奖学金',
    recipientType: 'PERSONAL',
    title: '2023年社会工作奖学金',
    description: '记录在学生工作、公共服务和校园治理中积极贡献的同学。'
  }
]

const honorRecipients = [
  {
    id: 201,
    showcaseId: 101,
    recipientType: 'PERSONAL',
    recipientName: '张一鸣',
    major: '计算机科学与技术',
    grade: '2021级',
    className: '计科1班',
    awardIntro: '连续三年综合测评位列专业前列，主持大学生创新训练项目并获得校级优秀结项。',
    advancedDeeds: '他长期参与开源社区和朋辈辅导，累计服务同学超过120小时，将课程笔记整理为共享资料，带动班级形成互帮互学的氛围。',
    photoFileId: null,
    members: [],
    attachments: [
      { id: 301, fileId: 9001, attachmentType: 'PHOTO', fileName: 'recipient-zhang.png', contentType: 'image/png', fileSize: 0, storagePath: '/static/images/banner1.png', caption: '获奖风采' }
    ]
  },
  {
    id: 202,
    showcaseId: 101,
    recipientType: 'PERSONAL',
    recipientName: '李清扬',
    major: '数据科学与大数据技术',
    grade: '2022级',
    className: '数科2班',
    awardIntro: '学习成绩优异，积极参与科研训练，在数据治理方向形成阶段性成果。',
    advancedDeeds: '她担任课程助教和班级学习委员，主动组织算法训练营，帮助多名同学完成竞赛备赛和课程复习。',
    photoFileId: null,
    members: [],
    attachments: []
  },
  {
    id: 203,
    showcaseId: 102,
    recipientType: 'COLLECTIVE',
    recipientName: '2021级计算机科学与技术1班',
    major: '计算机科学与技术',
    grade: '2021级',
    className: '计科1班',
    awardIntro: '班级学风扎实、凝聚力强，在课程学习、科研竞赛、志愿服务中表现突出。',
    advancedDeeds: '班级建立学习互助小组和项目共创机制，全年开展主题团日、公益服务和专业分享活动二十余次。',
    photoFileId: null,
    members: [
      { id: 401, studentName: '王思远', major: '计算机科学与技术', grade: '2021级', className: '计科1班', memberRole: '班长' },
      { id: 402, studentName: '周雨涵', major: '计算机科学与技术', grade: '2021级', className: '计科1班', memberRole: '学习委员' }
    ],
    attachments: [
      { id: 302, fileId: 9002, attachmentType: 'PHOTO', fileName: 'class-photo.png', contentType: 'image/png', fileSize: 0, storagePath: '/static/images/banner2.png', caption: '集体合影' }
    ]
  },
  {
    id: 204,
    showcaseId: 103,
    recipientType: 'PERSONAL',
    recipientName: '陈嘉宁',
    major: '软件工程',
    grade: '2020级',
    className: '软工1班',
    awardIntro: '积极参与学院学生工作，在迎新、就业服务和心理健康活动中表现突出。',
    advancedDeeds: '她注重把服务做细做实，参与搭建毕业生经验分享资料库，协助学院提升学生服务效率。',
    photoFileId: null,
    members: [],
    attachments: []
  }
]

const getHonorMockData = (url, params = {}) => {
  if (url === '/student/honors/page') {
    const page = Number(params.page || 0)
    const size = Number(params.size || 10)
    const keyword = (params.keyword || '').trim()
    const recipientType = params.recipientType || ''
    let content = honorShowcases.filter((item) => {
      const matchesType = !recipientType || item.recipientType === recipientType
      const matchesKeyword = !keyword || `${item.awardYear}${item.honorCategory}${item.title}${item.description}`.includes(keyword)
      return matchesType && matchesKeyword
    })
    content = content.map((item) => ({
      ...item,
      recipientCount: honorRecipients.filter((recipient) => recipient.showcaseId === item.id).length,
      recipients: []
    }))
    const start = page * size
    return {
      data: {
        content: content.slice(start, start + size),
        totalElements: content.length,
        totalPages: Math.ceil(content.length / size),
        page,
        size
      },
      success: true
    }
  }

  const showcaseMatch = url.match(/^\/student\/honors\/(\d+)$/)
  if (showcaseMatch) {
    const id = Number(showcaseMatch[1])
    const showcase = honorShowcases.find((item) => item.id === id)
    const recipients = honorRecipients.filter((item) => item.showcaseId === id)
    return { data: { ...showcase, recipientCount: recipients.length, recipients }, success: true }
  }

  const recipientsMatch = url.match(/^\/student\/honors\/(\d+)\/recipients$/)
  if (recipientsMatch) {
    const id = Number(recipientsMatch[1])
    return { data: honorRecipients.filter((item) => item.showcaseId === id), success: true }
  }

  const recipientMatch = url.match(/^\/student\/honors\/recipients\/(\d+)$/)
  if (recipientMatch) {
    const id = Number(recipientMatch[1])
    return { data: honorRecipients.find((item) => item.id === id), success: true }
  }

  return null
}

exports.getMockData = (url, params = {}) => {
  const honorMock = getHonorMockData(url, params)
  if (honorMock) {
    return honorMock
  }

  // 直接匹配完整 URL
  if (mockData[url]) {
    console.log('[Mock] 匹配到:', url)
    return { data: mockData[url].data, success: true }
  }
  
  // 处理带参数的路径
  const mockKey = url.split('?')[0]
  if (mockData[mockKey]) {
    console.log('[Mock] 匹配到基础路径:', mockKey)
    return { data: mockData[mockKey].data, success: true }
  }
  
  console.log('[Mock] 未找到数据:', url)
  return { data: null, success: true }
}
