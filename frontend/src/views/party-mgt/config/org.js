// 党组织管理 - 页面字段元数据（由代码生成器生成）

export default {
  partyOrg: {
    title: '组织架构',
    apiBase: '/api/party/org/organization',
    search: [{prop:"name",label:"组织名称",type:"input",like:true},{prop:"orgType",label:"组织类型",type:"select",options:[{label:"党委",value:"党委"},{label:"党总支",value:"党总支"},{label:"党支部",value:"党支部"},{label:"党组",value:"党组"},{label:"党小组",value:"党小组"}]},{prop:"status",label:"状态",type:"select",options:[{label:"正常",value:"正常"},{label:"撤销",value:"撤销"},{label:"筹建",value:"筹建"}]}],
    columns: [{prop:"orgNo",label:"组织编号"},{prop:"name",label:"组织名称"},{prop:"shortName",label:"组织简称"},{prop:"orgType",label:"组织类型",options:[{label:"党委",value:"党委"},{label:"党总支",value:"党总支"},{label:"党支部",value:"党支部"},{label:"党组",value:"党组"},{label:"党小组",value:"党小组"}]},{prop:"parentId",label:"上级组织ID",width:110},{prop:"secretary",label:"书记"},{prop:"status",label:"状态",options:[{label:"正常",value:"正常"},{label:"撤销",value:"撤销"},{label:"筹建",value:"筹建"}]}],
    fields: [{prop:"orgNo",label:"组织编号",type:"input"},{prop:"name",label:"组织名称",type:"input",required:true},{prop:"shortName",label:"组织简称",type:"input"},{prop:"orgType",label:"组织类型",type:"select",options:[{label:"党委",value:"党委"},{label:"党总支",value:"党总支"},{label:"党支部",value:"党支部"},{label:"党组",value:"党组"},{label:"党小组",value:"党小组"}]},{prop:"parentId",label:"上级组织ID",type:"number"},{prop:"secretary",label:"书记",type:"input"},{prop:"deputySecretary",label:"副书记",type:"input"},{prop:"committeeMembers",label:"委员成员",type:"input"},{prop:"contactPhone",label:"联系电话",type:"input"},{prop:"address",label:"组织地址",type:"input"},{prop:"establishedDate",label:"成立日期",type:"date"},{prop:"sortOrder",label:"排序",type:"number"},{prop:"status",label:"状态",type:"select",options:[{label:"正常",value:"正常"},{label:"撤销",value:"撤销"},{label:"筹建",value:"筹建"}]}]
  },
  termElection: {
    title: '换届选举',
    apiBase: '/api/party/org/term-election',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"筹备中",value:"筹备中"},{label:"已完成",value:"已完成"},{label:"延期",value:"延期"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"termNo",label:"届次编号"},{prop:"meetingName",label:"党员大会名称"},{prop:"electionDate",label:"选举日期"},{prop:"termStart",label:"任期开始"},{prop:"status",label:"状态",options:[{label:"筹备中",value:"筹备中"},{label:"已完成",value:"已完成"},{label:"延期",value:"延期"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"termNo",label:"届次编号",type:"input"},{prop:"meetingName",label:"党员大会名称",type:"input"},{prop:"electionDate",label:"选举日期",type:"date"},{prop:"termStart",label:"任期开始",type:"date"},{prop:"termEnd",label:"任期结束",type:"date"},{prop:"electedSecretary",label:"当选书记",type:"input"},{prop:"electedMembers",label:"当选委员",type:"input"},{prop:"voterCount",label:"应到人数",type:"number"},{prop:"status",label:"状态",type:"select",options:[{label:"筹备中",value:"筹备中"},{label:"已完成",value:"已完成"},{label:"延期",value:"延期"}]}]
  },
  lifeMeeting: {
    title: '组织与民主生活会',
    apiBase: '/api/party/org/life-meeting',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"meetingType",label:"会议类型",type:"select",options:[{label:"组织生活会",value:"组织生活会"},{label:"民主生活会",value:"民主生活会"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待召开",value:"待召开"},{label:"已召开",value:"已召开"},{label:"已取消",value:"已取消"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"meetingType",label:"会议类型",options:[{label:"组织生活会",value:"组织生活会"},{label:"民主生活会",value:"民主生活会"}]},{prop:"title",label:"会议主题"},{prop:"topic",label:"会议议题"},{prop:"meetingDate",label:"召开日期"},{prop:"status",label:"状态",options:[{label:"待召开",value:"待召开"},{label:"已召开",value:"已召开"},{label:"已取消",value:"已取消"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"meetingType",label:"会议类型",type:"select",options:[{label:"组织生活会",value:"组织生活会"},{label:"民主生活会",value:"民主生活会"}]},{prop:"title",label:"会议主题",type:"input",required:true},{prop:"topic",label:"会议议题",type:"input"},{prop:"meetingDate",label:"召开日期",type:"date"},{prop:"location",label:"会议地点",type:"input"},{prop:"host",label:"主持人",type:"input"},{prop:"participants",label:"参会人员",type:"input"},{prop:"absentee",label:"缺席人员",type:"input"},{prop:"minutes",label:"会议记录",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待召开",value:"待召开"},{label:"已召开",value:"已召开"},{label:"已取消",value:"已取消"}]}]
  },
  democraticAppraisal: {
    title: '民主评议党员',
    apiBase: '/api/party/org/democratic-appraisal',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"result",label:"评议结果",type:"select",options:[{label:"优秀",value:"优秀"},{label:"合格",value:"合格"},{label:"基本合格",value:"基本合格"},{label:"不合格",value:"不合格"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"meetingId",label:"关联会议ID",width:110},{prop:"memberId",label:"党员ID",width:110},{prop:"memberName",label:"党员姓名"},{prop:"appraisalYear",label:"评议年度",width:110}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"meetingId",label:"关联会议ID",type:"number"},{prop:"memberId",label:"党员ID",type:"number"},{prop:"memberName",label:"党员姓名",type:"input"},{prop:"appraisalYear",label:"评议年度",type:"number"},{prop:"selfComment",label:"自我评议",type:"textarea"},{prop:"mutualComment",label:"互评意见",type:"textarea"},{prop:"orgOpinion",label:"组织意见",type:"textarea"},{prop:"result",label:"评议结果",type:"select",options:[{label:"优秀",value:"优秀"},{label:"合格",value:"合格"},{label:"基本合格",value:"基本合格"},{label:"不合格",value:"不合格"}]},{prop:"appraisalDate",label:"评议日期",type:"date"}]
  },
  heartTalk: {
    title: '谈心谈话',
    apiBase: '/api/party/org/heart-talk',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"talkType",label:"谈话类型",type:"select",options:[{label:"日常谈心",value:"日常谈心"},{label:"提醒谈话",value:"提醒谈话"},{label:"任前谈话",value:"任前谈话"},{label:"组织处理谈话",value:"组织处理谈话"}]},{prop:"status",label:"状态",type:"select",options:[{label:"待开展",value:"待开展"},{label:"已完成",value:"已完成"},{label:"已取消",value:"已取消"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"talkerName",label:"谈话人"},{prop:"talkerIdentity",label:"谈话人身份"},{prop:"intervieweeName",label:"谈话对象"},{prop:"intervieweeIdentity",label:"对象身份"},{prop:"status",label:"状态",options:[{label:"待开展",value:"待开展"},{label:"已完成",value:"已完成"},{label:"已取消",value:"已取消"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"talkerName",label:"谈话人",type:"input"},{prop:"talkerIdentity",label:"谈话人身份",type:"input"},{prop:"intervieweeName",label:"谈话对象",type:"input"},{prop:"intervieweeIdentity",label:"对象身份",type:"input"},{prop:"talkType",label:"谈话类型",type:"select",options:[{label:"日常谈心",value:"日常谈心"},{label:"提醒谈话",value:"提醒谈话"},{label:"任前谈话",value:"任前谈话"},{label:"组织处理谈话",value:"组织处理谈话"}]},{prop:"talkDate",label:"谈话日期",type:"date"},{prop:"location",label:"谈话地点",type:"input"},{prop:"topic",label:"谈话主题",type:"input"},{prop:"content",label:"谈话内容",type:"textarea"},{prop:"status",label:"状态",type:"select",options:[{label:"待开展",value:"待开展"},{label:"已完成",value:"已完成"},{label:"已取消",value:"已取消"}]}]
  },
  partyPublicity: {
    title: '党务公开',
    apiBase: '/api/party/org/publicity',
    search: [{prop:"orgName",label:"党组织名称",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已公开",value:"已公开"},{label:"已下架",value:"已下架"}]}],
    columns: [{prop:"orgId",label:"党组织ID",width:110},{prop:"orgName",label:"党组织名称"},{prop:"title",label:"公开标题"},{prop:"category",label:"公开类别",options:[{label:"决议决定",value:"决议决定"},{label:"思想建设",value:"思想建设"},{label:"组织管理",value:"组织管理"},{label:"党风廉政",value:"党风廉政"},{label:"其他",value:"其他"}]},{prop:"publicScope",label:"公开范围"},{prop:"publisher",label:"发布人"},{prop:"status",label:"状态",options:[{label:"草稿",value:"草稿"},{label:"已公开",value:"已公开"},{label:"已下架",value:"已下架"}]}],
    fields: [{prop:"orgId",label:"党组织ID",type:"number"},{prop:"orgName",label:"党组织名称",type:"input"},{prop:"title",label:"公开标题",type:"input",required:true},{prop:"category",label:"公开类别",type:"select",options:[{label:"决议决定",value:"决议决定"},{label:"思想建设",value:"思想建设"},{label:"组织管理",value:"组织管理"},{label:"党风廉政",value:"党风廉政"},{label:"其他",value:"其他"}]},{prop:"content",label:"公开内容",type:"textarea"},{prop:"publicScope",label:"公开范围",type:"input"},{prop:"publisher",label:"发布人",type:"input"},{prop:"publishDate",label:"发布日期",type:"date"},{prop:"attachments",label:"附件",type:"input"},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"已公开",value:"已公开"},{label:"已下架",value:"已下架"}]}]
  }
}
