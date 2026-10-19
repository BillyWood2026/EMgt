// 监察监督与党风廉政 - 页面字段元数据（由代码生成器生成）

export default {
  dailySupervision: {
    title: '日常监督',
    apiBase: '/api/party/supervision/daily',
    search: [{prop:"title",label:"监督事项",type:"input",like:true},{prop:"supervisionType",label:"监督类型",type:"select",options:[{label:"专项监督",value:"专项监督"},{label:"常态化监督",value:"常态化监督"},{label:"节点监督",value:"节点监督"}]},{prop:"status",label:"状态",type:"select",options:[{label:"计划中",value:"计划中"},{label:"进行中",value:"进行中"},{label:"已完成",value:"已完成"}]}],
    columns: [{prop:"title",label:"监督事项"},{prop:"supervisionType",label:"监督类型",options:[{label:"专项监督",value:"专项监督"},{label:"常态化监督",value:"常态化监督"},{label:"节点监督",value:"节点监督"}]},{prop:"targetOrg",label:"监督对象组织"},{prop:"targetPerson",label:"监督对象人员"},{prop:"supervisor",label:"监督人"},{prop:"superviseDate",label:"监督日期"},{prop:"status",label:"状态",options:[{label:"计划中",value:"计划中"},{label:"进行中",value:"进行中"},{label:"已完成",value:"已完成"}]}],
    fields: [{prop:"title",label:"监督事项",type:"input",required:true},{prop:"supervisionType",label:"监督类型",type:"select",options:[{label:"专项监督",value:"专项监督"},{label:"常态化监督",value:"常态化监督"},{label:"节点监督",value:"节点监督"}]},{prop:"targetOrg",label:"监督对象组织",type:"input"},{prop:"targetPerson",label:"监督对象人员",type:"input"},{prop:"supervisor",label:"监督人",type:"input"},{prop:"superviseDate",label:"监督日期",type:"date"},{prop:"content",label:"监督内容",type:"textarea"},{prop:"finding",label:"发现问题",type:"textarea"},{prop:"requirement",label:"整改要求",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"计划中",value:"计划中"},{label:"进行中",value:"进行中"},{label:"已完成",value:"已完成"}]}]
  },
  integrityProfile: {
    title: '廉政档案',
    apiBase: '/api/party/supervision/integrity-profile',
    search: [{prop:"personName",label:"建档人员",type:"input",like:true}],
    columns: [{prop:"personName",label:"建档人员"},{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"所在组织"},{prop:"position",label:"职务"},{prop:"updateDate",label:"更新日期"}],
    fields: [{prop:"personName",label:"建档人员",type:"input"},{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"所在组织",type:"input"},{prop:"position",label:"职务",type:"input"},{prop:"baseInfo",label:"基本情况",type:"textarea"},{prop:"familyInfo",label:"家庭情况",type:"textarea"},{prop:"personalReport",label:"个人有关事项报告",type:"textarea"},{prop:"commitment",label:"廉洁承诺",type:"textarea"},{prop:"disciplineRecord",label:"违纪记录",type:"textarea"},{prop:"remark",label:"备注",type:"textarea"},{prop:"updateDate",label:"更新日期",type:"date"}]
  },
  integrityRisk: {
    title: '廉政风险防控',
    apiBase: '/api/party/supervision/integrity-risk',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"riskLevel",label:"风险等级",type:"select",options:[{label:"高",value:"高"},{label:"中",value:"中"},{label:"低",value:"低"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待防控",value:"待防控"},{label:"防控中",value:"防控中"},{label:"已化解",value:"已化解"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"riskPoint",label:"风险点"},{prop:"riskCategory",label:"风险类别",options:[{label:"三重一大",value:"三重一大"},{label:"财务管理",value:"财务管理"},{label:"人事管理",value:"人事管理"},{label:"物资采购",value:"物资采购"},{label:"工程项目",value:"工程项目"},{label:"其他",value:"其他"}]},{prop:"riskLevel",label:"风险等级",options:[{label:"高",value:"高"},{label:"中",value:"中"},{label:"低",value:"低"}]},{prop:"owner",label:"责任人"},{prop:"status",label:"状态",options:[{label:"待防控",value:"待防控"},{label:"防控中",value:"防控中"},{label:"已化解",value:"已化解"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"riskPoint",label:"风险点",type:"input"},{prop:"riskCategory",label:"风险类别",type:"select",options:[{label:"三重一大",value:"三重一大"},{label:"财务管理",value:"财务管理"},{label:"人事管理",value:"人事管理"},{label:"物资采购",value:"物资采购"},{label:"工程项目",value:"工程项目"},{label:"其他",value:"其他"}]},{prop:"riskLevel",label:"风险等级",type:"select",options:[{label:"高",value:"高"},{label:"中",value:"中"},{label:"低",value:"低"}]},{prop:"preventionMeasure",label:"防控措施",type:"textarea"},{prop:"owner",label:"责任人",type:"input"},{prop:"assessDate",label:"评估日期",type:"date"},{prop:"status",label:"状态",type:"select",options:[{label:"待防控",value:"待防控"},{label:"防控中",value:"防控中"},{label:"已化解",value:"已化解"}]}]
  },
  integrityTalk: {
    title: '廉政谈话提醒',
    apiBase: '/api/party/supervision/integrity-talk',
    search: [{prop:"talkerName",label:"谈话人",type:"input",like:true},{prop:"talkType",label:"谈话类型",type:"select",options:[{label:"任职谈话",value:"任职谈话"},{label:"廉政谈话",value:"廉政谈话"},{label:"提醒谈话",value:"提醒谈话"},{label:"诫勉谈话",value:"诫勉谈话"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待开展",value:"待开展"},{label:"已完成",value:"已完成"}]}],
    columns: [{prop:"talkType",label:"谈话类型",options:[{label:"任职谈话",value:"任职谈话"},{label:"廉政谈话",value:"廉政谈话"},{label:"提醒谈话",value:"提醒谈话"},{label:"诫勉谈话",value:"诫勉谈话"}]},{prop:"talkerName",label:"谈话人"},{prop:"intervieweeName",label:"谈话对象"},{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"talkDate",label:"谈话日期"},{prop:"status",label:"状态",options:[{label:"待开展",value:"待开展"},{label:"已完成",value:"已完成"}]}],
    fields: [{prop:"talkType",label:"谈话类型",type:"select",options:[{label:"任职谈话",value:"任职谈话"},{label:"廉政谈话",value:"廉政谈话"},{label:"提醒谈话",value:"提醒谈话"},{label:"诫勉谈话",value:"诫勉谈话"}]},{prop:"talkerName",label:"谈话人",type:"input"},{prop:"intervieweeName",label:"谈话对象",type:"input"},{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"talkDate",label:"谈话日期",type:"date"},{prop:"location",label:"谈话地点",type:"input"},{prop:"content",label:"谈话内容",type:"textarea"},{prop:"rectification",label:"整改要求",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待开展",value:"待开展"},{label:"已完成",value:"已完成"}]}]
  },
  inspectionRectification: {
    title: '巡察整改监督',
    apiBase: '/api/party/supervision/inspection',
    search: [{prop:"inspectionName",label:"巡察名称",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"待整改",value:"待整改"},{label:"整改中",value:"整改中"},{label:"已销号",value:"已销号"}]}],
    columns: [{prop:"inspectionName",label:"巡察名称"},{prop:"roundNo",label:"巡察轮次"},{prop:"inspectedOrg",label:"被巡察组织"},{prop:"inspectDate",label:"巡察日期"},{prop:"rectifyDeadline",label:"整改时限"},{prop:"rectifyDate",label:"完成日期"},{prop:"status",label:"状态",options:[{label:"待整改",value:"待整改"},{label:"整改中",value:"整改中"},{label:"已销号",value:"已销号"}]}],
    fields: [{prop:"inspectionName",label:"巡察名称",type:"input"},{prop:"roundNo",label:"巡察轮次",type:"input"},{prop:"inspectedOrg",label:"被巡察组织",type:"input"},{prop:"inspectDate",label:"巡察日期",type:"date"},{prop:"feedbackIssue",label:"反馈问题",type:"textarea"},{prop:"rectificationMeasure",label:"整改措施",type:"textarea"},{prop:"rectifyDeadline",label:"整改时限",type:"date"},{prop:"rectifyDate",label:"完成日期",type:"date"},{prop:"result",label:"整改成效",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待整改",value:"待整改"},{label:"整改中",value:"整改中"},{label:"已销号",value:"已销号"}]}]
  },
  integrityAssessment: {
    title: '党风廉政建设考核',
    apiBase: '/api/party/supervision/integrity-assessment',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"grade",label:"考核等次",type:"select",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待考核",value:"待考核"},{label:"已完成",value:"已完成"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"assessYear",label:"考核年度",width:110},{prop:"score",label:"考核得分",width:110},{prop:"grade",label:"考核等次",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]},{prop:"responsibilityPerson",label:"责任人"},{prop:"status",label:"状态",options:[{label:"待考核",value:"待考核"},{label:"已完成",value:"已完成"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"assessYear",label:"考核年度",type:"number"},{prop:"score",label:"考核得分",type:"number",step:"0.01"},{prop:"grade",label:"考核等次",type:"select",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]},{prop:"responsibilityPerson",label:"责任人",type:"input"},{prop:"assessDate",label:"考核日期",type:"date"},{prop:"comment",label:"考核意见",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待考核",value:"待考核"},{label:"已完成",value:"已完成"}]}]
  }
}
