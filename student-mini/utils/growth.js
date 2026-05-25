const moduleMetaMap = {
  'award-support': {
    title: '奖助情况',
    iconText: '奖',
    description: '查看荣誉称号、奖学金和助学金等已登记奖助信息',
    editMode: 'DISABLED',
    editModeLabel: '禁止修改',
    emptyText: '暂无奖助记录',
    fields: [
      { key: 'assessmentAcademicYear', label: '评定学年', type: 'text', required: true, placeholder: '例如 2024-2025' },
      { key: 'awardName', label: '奖学金名称', type: 'text', required: true, placeholder: '请输入奖学金名称' },
      { key: 'batchName', label: '批次名称', type: 'text', required: false, placeholder: '请输入批次名称' },
      { key: 'awardLevel', label: '奖励级别', type: 'text', required: false, placeholder: '请输入奖励级别' },
      { key: 'awardGrade', label: '奖励等级', type: 'text', required: false, placeholder: '请输入奖励等级' },
      { key: 'awardAmount', label: '奖学金额（元）', type: 'digit', required: false, placeholder: '请输入金额' },
      { key: 'awardType', label: '奖励类型', type: 'text', required: false, placeholder: '请输入奖励类型' }
    ]
  },
  competition: {
    title: '学科竞赛',
    iconText: '赛',
    description: '记录获奖竞赛、获奖级别和指导教师等竞赛经历',
    editMode: 'SELF',
    editModeLabel: '自主修改',
    emptyText: '暂无学科竞赛记录',
    fields: [
      { key: 'awardDate', label: '获奖日期', type: 'date', required: true, placeholder: '请选择获奖日期' },
      { key: 'competitionName', label: '获奖竞赛名称', type: 'text', required: true, placeholder: '请输入竞赛名称' },
      { key: 'competitionLevel', label: '获奖级别', type: 'text', required: false, placeholder: '请输入获奖级别' },
      { key: 'competitionGrade', label: '获奖等级', type: 'text', required: false, placeholder: '请输入获奖等级' },
      { key: 'competitionCategory', label: '获奖类别', type: 'text', required: false, placeholder: '请输入获奖类别' },
      { key: 'organizer', label: '竞赛主办单位', type: 'text', required: false, placeholder: '请输入主办单位' },
      { key: 'advisorTeacherInfo', label: '指导教师信息', type: 'text', required: false, placeholder: '请输入指导教师' },
      { key: 'remarks', label: '其他说明', type: 'textarea', required: false, placeholder: '请输入补充说明' }
    ]
  },
  'innovation-entrepreneurship': {
    title: '创新创业',
    iconText: '创',
    description: '管理创新创业项目的周期、角色和项目状态',
    editMode: 'SELF',
    editModeLabel: '自主修改',
    emptyText: '暂无创新创业记录',
    fields: [
      { key: 'startDate', label: '开始日期', type: 'date', required: true, placeholder: '请选择开始日期' },
      { key: 'endDate', label: '结束日期', type: 'date', required: false, placeholder: '请选择结束日期' },
      { key: 'projectCode', label: '项目编号', type: 'text', required: false, placeholder: '请输入项目编号' },
      { key: 'projectName', label: '项目名称', type: 'text', required: true, placeholder: '请输入项目名称' },
      { key: 'collegeName', label: '项目依托学院', type: 'text', required: false, placeholder: '请输入依托学院' },
      { key: 'projectStatus', label: '项目状态', type: 'text', required: false, placeholder: '请输入项目状态' },
      { key: 'projectLevel', label: '项目级别', type: 'text', required: false, placeholder: '请输入项目级别' },
      { key: 'completionGrade', label: '结项等级', type: 'text', required: false, placeholder: '请输入结项等级' },
      { key: 'participantRole', label: '参与角色', type: 'text', required: false, placeholder: '请输入参与角色' },
      { key: 'projectType', label: '项目类型', type: 'text', required: false, placeholder: '请输入项目类型' },
      { key: 'projectBatch', label: '项目批次', type: 'text', required: false, placeholder: '请输入项目批次' },
      { key: 'participantCount', label: '参与学生总人数', type: 'digit', required: false, placeholder: '请输入参与人数' },
      { key: 'advisorTeacher', label: '指导教师', type: 'text', required: false, placeholder: '请输入指导教师' }
    ]
  },
  'social-practice': {
    title: '社会实践',
    iconText: '践',
    description: '维护社会实践主题、地点、团队等级和指导老师信息',
    editMode: 'SELF',
    editModeLabel: '自主修改',
    emptyText: '暂无社会实践记录',
    fields: [
      { key: 'practiceStartDate', label: '实践开始日期', type: 'date', required: true, placeholder: '请选择实践开始日期' },
      { key: 'practiceEndDate', label: '实践结束日期', type: 'date', required: false, placeholder: '请选择实践结束日期' },
      { key: 'practiceTeamName', label: '实践团名称', type: 'text', required: true, placeholder: '请输入实践团名称' },
      { key: 'practiceTheme', label: '实践主题', type: 'text', required: true, placeholder: '请输入实践主题' },
      { key: 'practiceLocation', label: '实践地点', type: 'text', required: false, placeholder: '请输入实践地点' },
      { key: 'practiceTeamLevel', label: '实践团等级', type: 'text', required: false, placeholder: '请输入实践团等级' },
      { key: 'advisorTeacher', label: '指导老师', type: 'text', required: false, placeholder: '请输入指导老师' }
    ]
  },
  'student-work': {
    title: '学生工作',
    iconText: '工',
    description: '记录组织名称、担任职务和工作情况等学生工作经历',
    editMode: 'SELF',
    editModeLabel: '自主修改',
    emptyText: '暂无学生工作记录',
    fields: [
      { key: 'startDate', label: '开始日期', type: 'date', required: true, placeholder: '请选择开始日期' },
      { key: 'endDate', label: '结束日期', type: 'date', required: false, placeholder: '请选择结束日期' },
      { key: 'organizationName', label: '组织名称', type: 'text', required: true, placeholder: '请输入组织名称' },
      { key: 'positionName', label: '担任职务', type: 'text', required: true, placeholder: '请输入担任职务' },
      { key: 'workDescription', label: '工作情况', type: 'textarea', required: false, placeholder: '请输入工作情况' }
    ]
  },
  'volunteer-service': {
    title: '志愿服务',
    iconText: '愿',
    description: '维护志愿服务项目、地点、时长和组织名称',
    editMode: 'SELF',
    editModeLabel: '自主修改',
    emptyText: '暂无志愿服务记录',
    fields: [
      { key: 'serviceDate', label: '志愿服务日期', type: 'date', required: true, placeholder: '请选择志愿服务日期' },
      { key: 'serviceProject', label: '志愿服务项目', type: 'text', required: true, placeholder: '请输入志愿服务项目' },
      { key: 'serviceLocation', label: '志愿服务地点', type: 'text', required: false, placeholder: '请输入志愿服务地点' },
      { key: 'serviceDurationHours', label: '志愿服务时长', type: 'digit', required: false, placeholder: '请输入服务时长' },
      { key: 'serviceOrganizationName', label: '志愿服务组织名称', type: 'text', required: false, placeholder: '请输入组织名称' }
    ]
  },
  'skill-certificate': {
    title: '技能证书',
    iconText: '证',
    description: '登记技能或证书名称、获得时间、级别和说明',
    editMode: 'SELF',
    editModeLabel: '自主修改',
    emptyText: '暂无技能证书记录',
    fields: [
      { key: 'certificateName', label: '技能/证书名称', type: 'text', required: true, placeholder: '请输入技能/证书名称' },
      { key: 'obtainedDate', label: '获得时间', type: 'date', required: true, placeholder: '请选择获得时间' },
      { key: 'certificateLevel', label: '级别', type: 'text', required: false, placeholder: '请输入级别' },
      { key: 'description', label: '说明', type: 'textarea', required: false, placeholder: '请输入说明' }
    ]
  }
}

const getModuleMeta = (moduleCode) => {
  return moduleMetaMap[moduleCode] || { title: moduleCode, fields: [] }
}

const buildFieldsForEdit = (moduleCode, rawFields = {}) => {
  const meta = getModuleMeta(moduleCode)
  return meta.fields.map((field) => ({
    ...field,
    value: rawFields[field.key] || ''
  }))
}

module.exports = {
  moduleMetaMap,
  getModuleMeta,
  buildFieldsForEdit
}
