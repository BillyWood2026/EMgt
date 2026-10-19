// 党组织活动管理 - 页面字段元数据（由代码生成器生成）

export default {
  activityPlan: {
    title: '活动计划',
    apiBase: '/api/party/activity/plan',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"activityType",label:"活动类型",type:"select",options:[{label:"三会一课",value:"三会一课"},{label:"主题党日",value:"主题党日"},{label:"志愿服务",value:"志愿服务"},{label:"学习教育",value:"学习教育"},{label:"其他",value:"其他"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待发布",value:"待发布"},{label:"已发布",value:"已发布"},{label:"已取消",value:"已取消"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"title",label:"活动标题"},{prop:"activityType",label:"活动类型",options:[{label:"三会一课",value:"三会一课"},{label:"主题党日",value:"主题党日"},{label:"志愿服务",value:"志愿服务"},{label:"学习教育",value:"学习教育"},{label:"其他",value:"其他"}]},{prop:"planDate",label:"计划日期"},{prop:"location",label:"活动地点"},{prop:"status",label:"状态",options:[{label:"待发布",value:"待发布"},{label:"已发布",value:"已发布"},{label:"已取消",value:"已取消"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"title",label:"活动标题",type:"input",required:true},{prop:"activityType",label:"活动类型",type:"select",options:[{label:"三会一课",value:"三会一课"},{label:"主题党日",value:"主题党日"},{label:"志愿服务",value:"志愿服务"},{label:"学习教育",value:"学习教育"},{label:"其他",value:"其他"}]},{prop:"planDate",label:"计划日期",type:"date"},{prop:"location",label:"活动地点",type:"input"},{prop:"content",label:"活动内容",type:"textarea"},{prop:"participantScope",label:"参与范围",type:"input"},{prop:"budget",label:"预算经费",type:"number",step:"0.01"},{prop:"planner",label:"计划人",type:"input"},{prop:"status",label:"状态",type:"select",options:[{label:"待发布",value:"待发布"},{label:"已发布",value:"已发布"},{label:"已取消",value:"已取消"}]}]
  },
  activityEnrollment: {
    title: '报名签到',
    apiBase: '/api/party/activity/enrollment',
    search: [{prop:"memberName",label:"党员姓名",type:"input",like:true},{prop:"attendStatus",label:"参与状态",type:"select",options:[{label:"已报名",value:"已报名"},{label:"已签到",value:"已签到"},{label:"已签退",value:"已签退"},{label:"缺席",value:"缺席"}]}],
    columns: [{prop:"activityId",label:"活动ID",width:110},{prop:"activityTitle",label:"活动标题"},{prop:"memberId",label:"党员ID",width:110},{prop:"memberName",label:"党员姓名"},{prop:"enrollTime",label:"报名时间"},{prop:"signInTime",label:"签到时间"}],
    fields: [{prop:"activityId",label:"活动ID",type:"number"},{prop:"activityTitle",label:"活动标题",type:"input"},{prop:"memberId",label:"党员ID",type:"number"},{prop:"memberName",label:"党员姓名",type:"input"},{prop:"enrollTime",label:"报名时间",type:"datetime"},{prop:"signInTime",label:"签到时间",type:"datetime"},{prop:"signOutTime",label:"签退时间",type:"datetime"},{prop:"attendStatus",label:"参与状态",type:"select",options:[{label:"已报名",value:"已报名"},{label:"已签到",value:"已签到"},{label:"已签退",value:"已签退"},{label:"缺席",value:"缺席"}]}]
  },
  activityRecord: {
    title: '活动记录归档',
    apiBase: '/api/party/activity/record',
    search: [{prop:"title",label:"活动标题",type:"input",like:true},{prop:"activityType",label:"活动类型",type:"select",options:[{label:"三会一课",value:"三会一课"},{label:"主题党日",value:"主题党日"},{label:"志愿服务",value:"志愿服务"},{label:"学习教育",value:"学习教育"},{label:"其他",value:"其他"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待归档",value:"待归档"},{label:"已归档",value:"已归档"}]}],
    columns: [{prop:"activityId",label:"活动ID",width:110},{prop:"orgId",label:"党组织ID",width:110},{prop:"title",label:"活动标题"},{prop:"activityType",label:"活动类型",options:[{label:"三会一课",value:"三会一课"},{label:"主题党日",value:"主题党日"},{label:"志愿服务",value:"志愿服务"},{label:"学习教育",value:"学习教育"},{label:"其他",value:"其他"}]},{prop:"holdDate",label:"举办日期"},{prop:"location",label:"活动地点"},{prop:"status",label:"状态",options:[{label:"待归档",value:"待归档"},{label:"已归档",value:"已归档"}]}],
    fields: [{prop:"activityId",label:"活动ID",type:"number"},{prop:"orgId",label:"党组织ID",type:"number"},{prop:"title",label:"活动标题",type:"input",required:true},{prop:"activityType",label:"活动类型",type:"select",options:[{label:"三会一课",value:"三会一课"},{label:"主题党日",value:"主题党日"},{label:"志愿服务",value:"志愿服务"},{label:"学习教育",value:"学习教育"},{label:"其他",value:"其他"}]},{prop:"holdDate",label:"举办日期",type:"date"},{prop:"location",label:"活动地点",type:"input"},{prop:"host",label:"主持人",type:"input"},{prop:"attendCount",label:"参与人数",type:"number"},{prop:"content",label:"活动内容",type:"textarea"},{prop:"minutes",label:"会议记录",type:"textarea"},{prop:"attachments",label:"附件",type:"input"},{prop:"archiveTime",label:"归档时间",type:"datetime"},{prop:"archivist",label:"归档人",type:"input"},{prop:"status",label:"状态",type:"select",options:[{label:"待归档",value:"待归档"},{label:"已归档",value:"已归档"}]}]
  },
  threeMeetingsLesson: {
    title: '三会一课台账',
    apiBase: '/api/party/activity/three-lesson',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"meetingType",label:"会议类型",type:"select",options:[{label:"支部党员大会",value:"支部党员大会"},{label:"支部委员会",value:"支部委员会"},{label:"党小组会",value:"党小组会"},{label:"党课",value:"党课"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待召开",value:"待召开"},{label:"已召开",value:"已召开"},{label:"已补录",value:"已补录"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"meetingType",label:"会议类型",options:[{label:"支部党员大会",value:"支部党员大会"},{label:"支部委员会",value:"支部委员会"},{label:"党小组会",value:"党小组会"},{label:"党课",value:"党课"}]},{prop:"title",label:"会议主题"},{prop:"holdDate",label:"召开日期"},{prop:"location",label:"会议地点"},{prop:"status",label:"状态",options:[{label:"待召开",value:"待召开"},{label:"已召开",value:"已召开"},{label:"已补录",value:"已补录"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"meetingType",label:"会议类型",type:"select",options:[{label:"支部党员大会",value:"支部党员大会"},{label:"支部委员会",value:"支部委员会"},{label:"党小组会",value:"党小组会"},{label:"党课",value:"党课"}]},{prop:"title",label:"会议主题",type:"input",required:true},{prop:"holdDate",label:"召开日期",type:"date"},{prop:"location",label:"会议地点",type:"input"},{prop:"host",label:"主持人",type:"input"},{prop:"shouldCount",label:"应到人数",type:"number"},{prop:"actualCount",label:"实到人数",type:"number"},{prop:"content",label:"主要内容",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待召开",value:"待召开"},{label:"已召开",value:"已召开"},{label:"已补录",value:"已补录"}]}]
  },
  activityAssessment: {
    title: '活动统计考核',
    apiBase: '/api/party/activity/assessment',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"grade",label:"考核等次",type:"select",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"assessYear",label:"考核年度",width:110},{prop:"planCount",label:"计划次数",width:110},{prop:"actualCount",label:"实际次数",width:110},{prop:"attendanceRate",label:"参会率(%)",width:110}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"assessYear",label:"考核年度",type:"number"},{prop:"planCount",label:"计划次数",type:"number"},{prop:"actualCount",label:"实际次数",type:"number"},{prop:"attendanceRate",label:"参会率(%)",type:"number",step:"0.01"},{prop:"qualifyRate",label:"达标率(%)",type:"number",step:"0.01"},{prop:"score",label:"考核得分",type:"number",step:"0.01"},{prop:"grade",label:"考核等次",type:"select",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]},{prop:"assessDate",label:"考核日期",type:"date"},{prop:"comment",label:"考核评语",type:"textarea"}]
  }
}
