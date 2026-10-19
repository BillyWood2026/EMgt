// 党建考核评价 - 页面字段元数据（由代码生成器生成）

export default {
  assessmentIndicator: {
    title: '指标体系',
    apiBase: '/api/party/assessment/indicator',
    search: [{prop:"name",label:"指标名称",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}],
    columns: [{prop:"name",label:"指标名称"},{prop:"code",label:"指标编码"},{prop:"parentId",label:"上级指标ID",width:110},{prop:"category",label:"指标分类",options:[{label:"政治建设",value:"政治建设"},{label:"思想建设",value:"思想建设"},{label:"组织建设",value:"组织建设"},{label:"作风建设",value:"作风建设"},{label:"纪律建设",value:"纪律建设"},{label:"制度建设",value:"制度建设"},{label:"反腐败斗争",value:"反腐败斗争"}]},{prop:"weight",label:"权重",width:110},{prop:"maxScore",label:"分值上限",width:110},{prop:"status",label:"状态",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}],
    fields: [{prop:"name",label:"指标名称",type:"input",required:true},{prop:"code",label:"指标编码",type:"input"},{prop:"parentId",label:"上级指标ID",type:"number"},{prop:"category",label:"指标分类",type:"select",options:[{label:"政治建设",value:"政治建设"},{label:"思想建设",value:"思想建设"},{label:"组织建设",value:"组织建设"},{label:"作风建设",value:"作风建设"},{label:"纪律建设",value:"纪律建设"},{label:"制度建设",value:"制度建设"},{label:"反腐败斗争",value:"反腐败斗争"}]},{prop:"weight",label:"权重",type:"number",step:"0.01"},{prop:"maxScore",label:"分值上限",type:"number",step:"0.01"},{prop:"scoringStandard",label:"评分标准",type:"textarea"},{prop:"sortOrder",label:"排序",type:"number"},{prop:"status",label:"状态",type:"select",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}]
  },
  assessmentScheme: {
    title: '考核方案',
    apiBase: '/api/party/assessment/scheme',
    search: [{prop:"name",label:"方案名称",type:"input",like:true},{prop:"targetType",label:"考核对象",type:"select",options:[{label:"党组织",value:"党组织"},{label:"党员",value:"党员"},{label:"党组织书记",value:"党组织书记"}]},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已发布",value:"已发布"},{label:"执行中",value:"执行中"},{label:"已结束",value:"已结束"}]}],
    columns: [{prop:"schemeNo",label:"方案编号"},{prop:"name",label:"方案名称"},{prop:"assessYear",label:"考核年度",width:110},{prop:"targetType",label:"考核对象",options:[{label:"党组织",value:"党组织"},{label:"党员",value:"党员"},{label:"党组织书记",value:"党组织书记"}]},{prop:"startDate",label:"开始日期"},{prop:"endDate",label:"结束日期"},{prop:"status",label:"状态",options:[{label:"草稿",value:"草稿"},{label:"已发布",value:"已发布"},{label:"执行中",value:"执行中"},{label:"已结束",value:"已结束"}]}],
    fields: [{prop:"schemeNo",label:"方案编号",type:"input"},{prop:"name",label:"方案名称",type:"input",required:true},{prop:"assessYear",label:"考核年度",type:"number"},{prop:"targetType",label:"考核对象",type:"select",options:[{label:"党组织",value:"党组织"},{label:"党员",value:"党员"},{label:"党组织书记",value:"党组织书记"}]},{prop:"startDate",label:"开始日期",type:"date"},{prop:"endDate",label:"结束日期",type:"date"},{prop:"description",label:"方案说明",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已发布",value:"已发布"},{label:"执行中",value:"执行中"},{label:"已结束",value:"已结束"}]}]
  },
  assessmentProcess: {
    title: '考核流程',
    apiBase: '/api/party/assessment/process',
    search: [{prop:"nodeName",label:"环节名称",type:"input",like:true},{prop:"nodeType",label:"环节类型",type:"select",options:[{label:"自评",value:"自评"},{label:"群众评议",value:"群众评议"},{label:"组织考评",value:"组织考评"},{label:"综合评定",value:"综合评定"},{label:"结果反馈",value:"结果反馈"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待开始",value:"待开始"},{label:"进行中",value:"进行中"},{label:"已完成",value:"已完成"}]}],
    columns: [{prop:"schemeId",label:"方案ID",width:110},{prop:"nodeName",label:"环节名称"},{prop:"nodeOrder",label:"环节顺序",width:110},{prop:"nodeType",label:"环节类型",options:[{label:"自评",value:"自评"},{label:"群众评议",value:"群众评议"},{label:"组织考评",value:"组织考评"},{label:"综合评定",value:"综合评定"},{label:"结果反馈",value:"结果反馈"}]},{prop:"handlerRole",label:"责任角色"},{prop:"deadline",label:"完成时限"},{prop:"status",label:"状态",options:[{label:"待开始",value:"待开始"},{label:"进行中",value:"进行中"},{label:"已完成",value:"已完成"}]}],
    fields: [{prop:"schemeId",label:"方案ID",type:"number"},{prop:"nodeName",label:"环节名称",type:"input"},{prop:"nodeOrder",label:"环节顺序",type:"number"},{prop:"nodeType",label:"环节类型",type:"select",options:[{label:"自评",value:"自评"},{label:"群众评议",value:"群众评议"},{label:"组织考评",value:"组织考评"},{label:"综合评定",value:"综合评定"},{label:"结果反馈",value:"结果反馈"}]},{prop:"handlerRole",label:"责任角色",type:"input"},{prop:"deadline",label:"完成时限",type:"date"},{prop:"requirement",label:"环节要求",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待开始",value:"待开始"},{label:"进行中",value:"进行中"},{label:"已完成",value:"已完成"}]}]
  },
  scoreAlgorithm: {
    title: '评分算法',
    apiBase: '/api/party/assessment/algorithm',
    search: [{prop:"name",label:"算法名称",type:"input",like:true},{prop:"algorithmType",label:"算法类型",type:"select",options:[{label:"加权求和",value:"加权求和"},{label:"扣分制",value:"扣分制"},{label:"等级评定",value:"等级评定"},{label:"自定义公式",value:"自定义公式"}]},{prop:"status",label:"状态",type:"select",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}],
    columns: [{prop:"name",label:"算法名称"},{prop:"code",label:"算法编码"},{prop:"algorithmType",label:"算法类型",options:[{label:"加权求和",value:"加权求和"},{label:"扣分制",value:"扣分制"},{label:"等级评定",value:"等级评定"},{label:"自定义公式",value:"自定义公式"}]},{prop:"formula",label:"计算公式"},{prop:"status",label:"状态",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}],
    fields: [{prop:"name",label:"算法名称",type:"input",required:true},{prop:"code",label:"算法编码",type:"input"},{prop:"algorithmType",label:"算法类型",type:"select",options:[{label:"加权求和",value:"加权求和"},{label:"扣分制",value:"扣分制"},{label:"等级评定",value:"等级评定"},{label:"自定义公式",value:"自定义公式"}]},{prop:"formula",label:"计算公式",type:"input"},{prop:"weightRule",label:"权重规则",type:"textarea"},{prop:"description",label:"算法说明",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}]
  },
  assessmentResult: {
    title: '结果应用',
    apiBase: '/api/party/assessment/result',
    search: [{prop:"targetName",label:"对象名称",type:"input",like:true},{prop:"grade",label:"考核等次",type:"select",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待评定",value:"待评定"},{label:"已公示",value:"已公示"},{label:"已应用",value:"已应用"}]}],
    columns: [{prop:"schemeId",label:"方案ID",width:110},{prop:"targetOrg",label:"考核对象组织"},{prop:"targetName",label:"对象名称"},{prop:"assessYear",label:"考核年度",width:110},{prop:"totalScore",label:"总分",width:110},{prop:"grade",label:"考核等次",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]},{prop:"status",label:"状态",options:[{label:"待评定",value:"待评定"},{label:"已公示",value:"已公示"},{label:"已应用",value:"已应用"}]}],
    fields: [{prop:"schemeId",label:"方案ID",type:"number"},{prop:"targetOrg",label:"考核对象组织",type:"input"},{prop:"targetName",label:"对象名称",type:"input"},{prop:"assessYear",label:"考核年度",type:"number"},{prop:"totalScore",label:"总分",type:"number",step:"0.01"},{prop:"grade",label:"考核等次",type:"select",options:[{label:"优秀",value:"优秀"},{label:"良好",value:"良好"},{label:"合格",value:"合格"},{label:"不合格",value:"不合格"}]},{prop:"rankLevel",label:"排名档次",type:"input"},{prop:"application",label:"结果应用",type:"select",options:[{label:"评优评先",value:"评优评先"},{label:"干部任用",value:"干部任用"},{label:"整改提升",value:"整改提升"},{label:"通报表扬",value:"通报表扬"}]},{prop:"feedback",label:"结果反馈",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待评定",value:"待评定"},{label:"已公示",value:"已公示"},{label:"已应用",value:"已应用"}]}]
  }
}
