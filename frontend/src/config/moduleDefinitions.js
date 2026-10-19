/**
 * 模块选装配置 - 后端模块元数据
 * 
 * 用于 module-config-center 服务启动时初始化模块配置表
 * 每个业务微服务也可以通过此文件定义自身的模块元信息
 */

// 人力资源领域
export const hrmModules = [
  {
    moduleKey: '/hrm/staff',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '员工管理',
    subOrder: 1,
    description: '员工档案、入职、离职、转正、调动等',
    serviceName: 'staff-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/training',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '培训管理',
    subOrder: 2,
    description: '培训计划、培训实施、培训评估等',
    serviceName: 'training-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/performance',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '绩效管理',
    subOrder: 3,
    description: '绩效计划、绩效评估、绩效改进等',
    serviceName: 'perf-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/qualification',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '资质职级',
    subOrder: 4,
    description: '资质等级、职级体系、任职资格等',
    serviceName: 'qual-rank',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/attendance',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '考勤管理',
    subOrder: 5,
    description: '考勤打卡、请假、加班、出差等',
    serviceName: 'attendance',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/holiday',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '假期管理',
    subOrder: 6,
    description: '年假、病假、事假、调休等',
    serviceName: 'holiday',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/salary',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '薪资管理',
    subOrder: 7,
    description: '薪资核算、社保公积金、奖金等',
    serviceName: 'salary-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/talent',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '人才发展',
    subOrder: 8,
    description: '技能认证、人才评估、人才发展项目等',
    serviceName: 'talent',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/hr-planning',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '人力规划',
    subOrder: 9,
    description: '人力资源规划、编制管理、人力成本等',
    serviceName: 'hr-planning',
    version: '1.0.0'
  },
  {
    moduleKey: '/hrm/recruitment',
    parentKey: '/hrm',
    parentTitle: '人力资源管理',
    parentIcon: 'UserFilled',
    parentOrder: 2,
    subTitle: '招聘管理',
    subOrder: 10,
    description: '招聘需求、简历管理、面试管理、录用等',
    serviceName: 'recruitment-mgt',
    version: '1.0.0'
  }
]

// 环境健康安全能源领域
export const eehsModules = [
  {
    moduleKey: '/eehs/safety',
    parentKey: '/eehs',
    parentTitle: '环境健康安全能源',
    parentIcon: 'WarningFilled',
    parentOrder: 3,
    subTitle: '安全管理',
    subOrder: 1,
    description: '安全检查、隐患治理、事故管理等',
    serviceName: 'safety-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/eehs/environment',
    parentKey: '/eehs',
    parentTitle: '环境健康安全能源',
    parentIcon: 'WarningFilled',
    parentOrder: 3,
    subTitle: '环境管理',
    subOrder: 2,
    description: '环境监测、排污管理、环保项目等',
    serviceName: 'enviro-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/eehs/occupational-health',
    parentKey: '/eehs',
    parentTitle: '环境健康安全能源',
    parentIcon: 'WarningFilled',
    parentOrder: 3,
    subTitle: '职业健康',
    subOrder: 3,
    description: '职业健康检查、职业病管理、健康档案等',
    serviceName: 'occu-health-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/eehs/energy',
    parentKey: '/eehs',
    parentTitle: '环境健康安全能源',
    parentIcon: 'WarningFilled',
    parentOrder: 3,
    subTitle: '能源管理',
    subOrder: 4,
    description: '能耗统计、节能项目、能源分析等',
    serviceName: 'energy-mgt',
    version: '1.0.0'
  }
]

// 运营改善领域
export const opsModules = [
  {
    moduleKey: '/ops/process',
    parentKey: '/ops',
    parentTitle: '运营改善',
    parentIcon: 'DataAnalysis',
    parentOrder: 4,
    subTitle: '流程制度管理',
    subOrder: 1,
    description: '流程梳理、制度文档、流程优化等',
    serviceName: 'process-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/ops/improve',
    parentKey: '/ops',
    parentTitle: '运营改善',
    parentIcon: 'DataAnalysis',
    parentOrder: 4,
    subTitle: '改善管理',
    subOrder: 2,
    description: '改善提案、改善项目、改善成果等',
    serviceName: 'improve-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/ops/audit',
    parentKey: '/ops',
    parentTitle: '运营改善',
    parentIcon: 'DataAnalysis',
    parentOrder: 4,
    subTitle: '审计管理',
    subOrder: 3,
    description: '审计计划、审计实施、审计报告等',
    serviceName: 'audit-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/ops/project',
    parentKey: '/ops',
    parentTitle: '运营改善',
    parentIcon: 'DataAnalysis',
    parentOrder: 4,
    subTitle: '项目管理',
    subOrder: 4,
    description: '项目立项、项目进度、项目验收等',
    serviceName: 'project-mgt',
    version: '1.0.0'
  }
]

// 系统管理监控领域
export const sysModules = [
  {
    moduleKey: '/sys-mgt-mon/sys-mgt',
    parentKey: '/sys-mgt-mon',
    parentTitle: '系统管理监控',
    parentIcon: 'Setting',
    parentOrder: 6,
    subTitle: '系统管理',
    subOrder: 1,
    description: '用户、角色、菜单、字典、日志等',
    serviceName: 'sys-mgt',
    version: '1.0.0'
  },
  {
    moduleKey: '/sys-mgt-mon/sys-monitor',
    parentKey: '/sys-mgt-mon',
    parentTitle: '系统管理监控',
    parentIcon: 'Setting',
    parentOrder: 6,
    subTitle: '系统监控',
    subOrder: 2,
    description: '服务器监控、数据库监控、性能监控、告警等',
    serviceName: 'sys-monitor',
    version: '1.0.0'
  }
]

// 质量任务（独立文档模板）
export const qualityModules = [
  {
    moduleKey: '/quality/templates',
    parentKey: '/quality',
    parentTitle: '质量任务文档',
    parentIcon: 'Document',
    parentOrder: 5,
    subTitle: '文档模板',
    subOrder: 1,
    description: 'P2-P7各单项质量任务独立文档模板',
    serviceName: 'quality-templates',
    version: '1.0.0'
  }
]

export const allModuleDefinitions = [
  ...hrmModules,
  ...eehsModules,
  ...opsModules,
  ...sysModules,
  ...qualityModules
]

export default allModuleDefinitions
