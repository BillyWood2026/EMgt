// 统计分析中心 - 页面字段元数据（由代码生成器生成）

export default {
  dashboardStat: {
    title: '党建驾驶舱',
    apiBase: '/api/party/stats/dashboard',
    search: [],
    columns: [{prop:"statDate",label:"快照日期"},{prop:"orgCount",label:"党组织数",width:110},{prop:"memberCount",label:"党员总数",width:110},{prop:"fullMemberCount",label:"正式党员数",width:110},{prop:"probationCount",label:"预备党员数",width:110},{prop:"floatingMemberCount",label:"流动党员数",width:110}],
    fields: [{prop:"statDate",label:"快照日期",type:"date"},{prop:"orgCount",label:"党组织数",type:"number"},{prop:"memberCount",label:"党员总数",type:"number"},{prop:"fullMemberCount",label:"正式党员数",type:"number"},{prop:"probationCount",label:"预备党员数",type:"number"},{prop:"floatingMemberCount",label:"流动党员数",type:"number"},{prop:"activityCount",label:"活动数",type:"number"},{prop:"meetingCount",label:"三会一课次数",type:"number"},{prop:"dueCollected",label:"党费收缴额",type:"number",step:"0.01"},{prop:"petitionCount",label:"信访举报数",type:"number"},{prop:"remark",label:"备注",type:"input"}]
  },
  statReport: {
    title: '固定报表',
    apiBase: '/api/party/stats/report',
    search: [{prop:"title",label:"报表名称",type:"input",like:true},{prop:"reportType",label:"报表类型",type:"select",options:[{label:"党员统计",value:"党员统计"},{label:"党费统计",value:"党费统计"},{label:"组织统计",value:"组织统计"},{label:"活动统计",value:"活动统计"},{label:"考核统计",value:"考核统计"},{label:"纪检统计",value:"纪检统计"}]},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已生成",value:"已生成"},{label:"已发布",value:"已发布"}]}],
    columns: [{prop:"reportNo",label:"报表编号"},{prop:"title",label:"报表名称"},{prop:"reportType",label:"报表类型",options:[{label:"党员统计",value:"党员统计"},{label:"党费统计",value:"党费统计"},{label:"组织统计",value:"组织统计"},{label:"活动统计",value:"活动统计"},{label:"考核统计",value:"考核统计"},{label:"纪检统计",value:"纪检统计"}]},{prop:"periodStart",label:"统计开始"},{prop:"periodEnd",label:"统计结束"},{prop:"generator",label:"生成人"},{prop:"status",label:"状态",options:[{label:"草稿",value:"草稿"},{label:"已生成",value:"已生成"},{label:"已发布",value:"已发布"}]}],
    fields: [{prop:"reportNo",label:"报表编号",type:"input"},{prop:"title",label:"报表名称",type:"input",required:true},{prop:"reportType",label:"报表类型",type:"select",options:[{label:"党员统计",value:"党员统计"},{label:"党费统计",value:"党费统计"},{label:"组织统计",value:"组织统计"},{label:"活动统计",value:"活动统计"},{label:"考核统计",value:"考核统计"},{label:"纪检统计",value:"纪检统计"}]},{prop:"periodStart",label:"统计开始",type:"date"},{prop:"periodEnd",label:"统计结束",type:"date"},{prop:"content",label:"报表内容",type:"textarea"},{prop:"generator",label:"生成人",type:"input"},{prop:"generateTime",label:"生成时间",type:"datetime"},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已生成",value:"已生成"},{label:"已发布",value:"已发布"}]}]
  },
  statDrill: {
    title: '数据下钻',
    apiBase: '/api/party/stats/drill',
    search: [{prop:"name",label:"下钻名称",type:"input",like:true}],
    columns: [{prop:"name",label:"下钻名称"},{prop:"dimension",label:"分析维度",options:[{label:"组织维度",value:"组织维度"},{label:"时间维度",value:"时间维度"},{label:"党员维度",value:"党员维度"},{label:"活动维度",value:"活动维度"}]},{prop:"metric",label:"指标名称"},{prop:"drillPath",label:"下钻路径"},{prop:"lastUpdateTime",label:"更新时间"}],
    fields: [{prop:"name",label:"下钻名称",type:"input",required:true},{prop:"dimension",label:"分析维度",type:"select",options:[{label:"组织维度",value:"组织维度"},{label:"时间维度",value:"时间维度"},{label:"党员维度",value:"党员维度"},{label:"活动维度",value:"活动维度"}]},{prop:"metric",label:"指标名称",type:"input"},{prop:"drillPath",label:"下钻路径",type:"input"},{prop:"configJson",label:"下钻配置(JSON)",type:"textarea"},{prop:"description",label:"说明",type:"textarea"},{prop:"lastUpdateTime",label:"更新时间",type:"datetime"}]
  }
}
