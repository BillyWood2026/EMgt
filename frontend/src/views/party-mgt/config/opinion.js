// 舆情监控管理 - 页面字段元数据（由代码生成器生成）

export default {
  opinionCollection: {
    title: '舆情采集',
    apiBase: '/api/party/opinion/collection',
    search: [{prop:"title",label:"舆情标题",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"待分析",value:"待分析"},{label:"已分析",value:"已分析"},{label:"已预警",value:"已预警"}]}],
    columns: [{prop:"title",label:"舆情标题"},{prop:"source",label:"来源渠道",options:[{label:"网站",value:"网站"},{label:"论坛",value:"论坛"},{label:"博客",value:"博客"},{label:"微博",value:"微博"},{label:"微信",value:"微信"},{label:"短视频",value:"短视频"},{label:"新闻媒体",value:"新闻媒体"}]},{prop:"url",label:"原文链接"},{prop:"author",label:"发布者"},{prop:"region",label:"涉及地区"},{prop:"keywords",label:"关键词"},{prop:"status",label:"状态",options:[{label:"待分析",value:"待分析"},{label:"已分析",value:"已分析"},{label:"已预警",value:"已预警"}]}],
    fields: [{prop:"title",label:"舆情标题",type:"input",required:true},{prop:"source",label:"来源渠道",type:"select",options:[{label:"网站",value:"网站"},{label:"论坛",value:"论坛"},{label:"博客",value:"博客"},{label:"微博",value:"微博"},{label:"微信",value:"微信"},{label:"短视频",value:"短视频"},{label:"新闻媒体",value:"新闻媒体"}]},{prop:"url",label:"原文链接",type:"input"},{prop:"author",label:"发布者",type:"input"},{prop:"region",label:"涉及地区",type:"input"},{prop:"keywords",label:"关键词",type:"input"},{prop:"content",label:"舆情内容",type:"textarea"},{prop:"collectTime",label:"采集时间",type:"datetime"},{prop:"sentimentHint",label:"情感预判",type:"select",options:[{label:"正面",value:"正面"},{label:"负面",value:"负面"},{label:"中性",value:"中性"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待分析",value:"待分析"},{label:"已分析",value:"已分析"},{label:"已预警",value:"已预警"}]}]
  },
  opinionAnalysis: {
    title: '舆情分析',
    apiBase: '/api/party/opinion/analysis',
    search: [{prop:"title",label:"舆情标题",type:"input",like:true},{prop:"sentiment",label:"情感倾向",type:"select",options:[{label:"正面",value:"正面"},{label:"负面",value:"负面"},{label:"中性",value:"中性"}]},{prop:"heatLevel",label:"热度等级",type:"select",options:[{label:"高",value:"高"},{label:"中",value:"中"},{label:"低",value:"低"}]}],
    columns: [{prop:"collectionId",label:"采集信息ID",width:110},{prop:"title",label:"舆情标题"},{prop:"sentiment",label:"情感倾向",options:[{label:"正面",value:"正面"},{label:"负面",value:"负面"},{label:"中性",value:"中性"}]},{prop:"heatLevel",label:"热度等级",options:[{label:"高",value:"高"},{label:"中",value:"中"},{label:"低",value:"低"}]},{prop:"heatValue",label:"热度值",width:110},{prop:"keywords",label:"关键词"}],
    fields: [{prop:"collectionId",label:"采集信息ID",type:"number"},{prop:"title",label:"舆情标题",type:"input",required:true},{prop:"sentiment",label:"情感倾向",type:"select",options:[{label:"正面",value:"正面"},{label:"负面",value:"负面"},{label:"中性",value:"中性"}]},{prop:"heatLevel",label:"热度等级",type:"select",options:[{label:"高",value:"高"},{label:"中",value:"中"},{label:"低",value:"低"}]},{prop:"heatValue",label:"热度值",type:"number"},{prop:"keywords",label:"关键词",type:"input"},{prop:"analyst",label:"分析人",type:"input"},{prop:"analyzeTime",label:"分析时间",type:"datetime"},{prop:"summary",label:"分析摘要",type:"textarea"}]
  },
  opinionAlert: {
    title: '分级预警',
    apiBase: '/api/party/opinion/alert',
    search: [{prop:"title",label:"舆情标题",type:"input",like:true},{prop:"alertLevel",label:"预警级别",type:"select",options:[{label:"蓝色一般",value:"蓝色一般"},{label:"黄色较大",value:"黄色较大"},{label:"橙色重大",value:"橙色重大"},{label:"红色特别重大",value:"红色特别重大"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待处置",value:"待处置"},{label:"处置中",value:"处置中"},{label:"已解除",value:"已解除"}]}],
    columns: [{prop:"collectionId",label:"采集信息ID",width:110},{prop:"title",label:"舆情标题"},{prop:"alertLevel",label:"预警级别",options:[{label:"蓝色一般",value:"蓝色一般"},{label:"黄色较大",value:"黄色较大"},{label:"橙色重大",value:"橙色重大"},{label:"红色特别重大",value:"红色特别重大"}]},{prop:"alertTime",label:"预警时间"},{prop:"receiver",label:"接收人"},{prop:"handleDeadline",label:"处置时限"},{prop:"status",label:"状态",options:[{label:"待处置",value:"待处置"},{label:"处置中",value:"处置中"},{label:"已解除",value:"已解除"}]}],
    fields: [{prop:"collectionId",label:"采集信息ID",type:"number"},{prop:"title",label:"舆情标题",type:"input",required:true},{prop:"alertLevel",label:"预警级别",type:"select",options:[{label:"蓝色一般",value:"蓝色一般"},{label:"黄色较大",value:"黄色较大"},{label:"橙色重大",value:"橙色重大"},{label:"红色特别重大",value:"红色特别重大"}]},{prop:"alertReason",label:"预警原因",type:"textarea"},{prop:"alertTime",label:"预警时间",type:"datetime"},{prop:"receiver",label:"接收人",type:"input"},{prop:"handleDeadline",label:"处置时限",type:"date"},{prop:"status",label:"状态",type:"select",options:[{label:"待处置",value:"待处置"},{label:"处置中",value:"处置中"},{label:"已解除",value:"已解除"}]}]
  },
  opinionHandle: {
    title: '处置闭环',
    apiBase: '/api/party/opinion/handle',
    search: [{prop:"title",label:"舆情标题",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"待处置",value:"待处置"},{label:"处置中",value:"处置中"},{label:"已办结",value:"已办结"}]}],
    columns: [{prop:"alertId",label:"预警ID",width:110},{prop:"collectionId",label:"采集信息ID",width:110},{prop:"title",label:"舆情标题"},{prop:"handler",label:"处置人"},{prop:"handleTime",label:"处置时间"},{prop:"deadline",label:"完成时限"},{prop:"status",label:"状态",options:[{label:"待处置",value:"待处置"},{label:"处置中",value:"处置中"},{label:"已办结",value:"已办结"}]}],
    fields: [{prop:"alertId",label:"预警ID",type:"number"},{prop:"collectionId",label:"采集信息ID",type:"number"},{prop:"title",label:"舆情标题",type:"input",required:true},{prop:"measure",label:"处置措施",type:"textarea"},{prop:"handler",label:"处置人",type:"input"},{prop:"handleTime",label:"处置时间",type:"datetime"},{prop:"deadline",label:"完成时限",type:"date"},{prop:"result",label:"处置结果",type:"textarea"},{prop:"feedback",label:"反馈情况",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待处置",value:"待处置"},{label:"处置中",value:"处置中"},{label:"已办结",value:"已办结"}]}]
  },
  opinionReport: {
    title: '舆情报告',
    apiBase: '/api/party/opinion/report',
    search: [{prop:"title",label:"报告标题",type:"input",like:true},{prop:"reportType",label:"报告类型",type:"select",options:[{label:"日报",value:"日报"},{label:"周报",value:"周报"},{label:"月报",value:"月报"},{label:"专报",value:"专报"}]},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已提交",value:"已提交"},{label:"已发布",value:"已发布"}]}],
    columns: [{prop:"reportNo",label:"报告编号"},{prop:"title",label:"报告标题"},{prop:"reportType",label:"报告类型",options:[{label:"日报",value:"日报"},{label:"周报",value:"周报"},{label:"月报",value:"月报"},{label:"专报",value:"专报"}]},{prop:"periodStart",label:"统计开始"},{prop:"periodEnd",label:"统计结束"},{prop:"reporter",label:"报告人"},{prop:"status",label:"状态",options:[{label:"草稿",value:"草稿"},{label:"已提交",value:"已提交"},{label:"已发布",value:"已发布"}]}],
    fields: [{prop:"reportNo",label:"报告编号",type:"input"},{prop:"title",label:"报告标题",type:"input",required:true},{prop:"reportType",label:"报告类型",type:"select",options:[{label:"日报",value:"日报"},{label:"周报",value:"周报"},{label:"月报",value:"月报"},{label:"专报",value:"专报"}]},{prop:"periodStart",label:"统计开始",type:"date"},{prop:"periodEnd",label:"统计结束",type:"date"},{prop:"content",label:"报告内容",type:"textarea"},{prop:"reporter",label:"报告人",type:"input"},{prop:"reportTime",label:"报告时间",type:"datetime"},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已提交",value:"已提交"},{label:"已发布",value:"已发布"}]}]
  }
}
