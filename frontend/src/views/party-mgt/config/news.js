// 新闻宣传管理 - 页面字段元数据（由代码生成器生成）

export default {
  newsColumn: {
    title: '新闻栏目',
    apiBase: '/api/party/news/column',
    search: [{prop:"name",label:"栏目名称",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}],
    columns: [{prop:"name",label:"栏目名称"},{prop:"code",label:"栏目编码"},{prop:"parentId",label:"上级栏目ID",width:110},{prop:"icon",label:"图标"},{prop:"sortOrder",label:"排序",width:110},{prop:"visible",label:"是否显示",options:[{label:"显示",value:"显示"},{label:"隐藏",value:"隐藏"}]},{prop:"status",label:"状态",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}],
    fields: [{prop:"name",label:"栏目名称",type:"input",required:true},{prop:"code",label:"栏目编码",type:"input"},{prop:"parentId",label:"上级栏目ID",type:"number"},{prop:"icon",label:"图标",type:"input"},{prop:"sortOrder",label:"排序",type:"number"},{prop:"visible",label:"是否显示",type:"select",options:[{label:"显示",value:"显示"},{label:"隐藏",value:"隐藏"}]},{prop:"description",label:"栏目描述",type:"input"},{prop:"status",label:"状态",type:"select",options:[{label:"启用",value:"启用"},{label:"停用",value:"停用"}]}]
  },
  newsArticle: {
    title: '稿件管理',
    apiBase: '/api/party/news/article',
    search: [{prop:"title",label:"稿件标题",type:"input",like:true},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"待审核",value:"待审核"},{label:"已通过",value:"已通过"},{label:"已退回",value:"已退回"},{label:"已发布",value:"已发布"}]}],
    columns: [{prop:"title",label:"稿件标题"},{prop:"columnId",label:"栏目ID",width:110},{prop:"author",label:"作者"},{prop:"source",label:"来源"},{prop:"coverImage",label:"封面图URL"},{prop:"submitTime",label:"投稿时间"},{prop:"status",label:"状态",options:[{label:"草稿",value:"草稿"},{label:"待审核",value:"待审核"},{label:"已通过",value:"已通过"},{label:"已退回",value:"已退回"},{label:"已发布",value:"已发布"}]}],
    fields: [{prop:"title",label:"稿件标题",type:"input",required:true},{prop:"columnId",label:"栏目ID",type:"number"},{prop:"author",label:"作者",type:"input"},{prop:"source",label:"来源",type:"input"},{prop:"summary",label:"摘要",type:"textarea"},{prop:"content",label:"正文",type:"textarea"},{prop:"coverImage",label:"封面图URL",type:"input"},{prop:"submitTime",label:"投稿时间",type:"datetime"},{prop:"status",label:"状态",type:"select",options:[{label:"草稿",value:"草稿"},{label:"待审核",value:"待审核"},{label:"已通过",value:"已通过"},{label:"已退回",value:"已退回"},{label:"已发布",value:"已发布"}]}]
  },
  newsReview: {
    title: '三级审核',
    apiBase: '/api/party/news/review',
    search: [{prop:"reviewLevel",label:"审核级别",type:"select",options:[{label:"一级审核",value:"一级审核"},{label:"二级审核",value:"二级审核"},{label:"三级审核",value:"三级审核"}]},{prop:"result",label:"审核结果",type:"select",options:[{label:"通过",value:"通过"},{label:"退回",value:"退回"}]}],
    columns: [{prop:"articleId",label:"稿件ID",width:110},{prop:"articleTitle",label:"稿件标题"},{prop:"reviewLevel",label:"审核级别",options:[{label:"一级审核",value:"一级审核"},{label:"二级审核",value:"二级审核"},{label:"三级审核",value:"三级审核"}]},{prop:"reviewer",label:"审核人"},{prop:"reviewTime",label:"审核时间"},{prop:"result",label:"审核结果",options:[{label:"通过",value:"通过"},{label:"退回",value:"退回"}]}],
    fields: [{prop:"articleId",label:"稿件ID",type:"number"},{prop:"articleTitle",label:"稿件标题",type:"input"},{prop:"reviewLevel",label:"审核级别",type:"select",options:[{label:"一级审核",value:"一级审核"},{label:"二级审核",value:"二级审核"},{label:"三级审核",value:"三级审核"}]},{prop:"reviewer",label:"审核人",type:"input"},{prop:"reviewTime",label:"审核时间",type:"datetime"},{prop:"result",label:"审核结果",type:"select",options:[{label:"通过",value:"通过"},{label:"退回",value:"退回"}]},{prop:"opinion",label:"审核意见",type:"textarea"}]
  },
  newsRecommendation: {
    title: '置顶推荐',
    apiBase: '/api/party/news/recommendation',
    search: [{prop:"recType",label:"推荐类型",type:"select",options:[{label:"置顶",value:"置顶"},{label:"推荐",value:"推荐"},{label:"热门",value:"热门"}]},{prop:"status",label:"状态",type:"select",options:[{label:"生效中",value:"生效中"},{label:"已失效",value:"已失效"}]}],
    columns: [{prop:"articleId",label:"稿件ID",width:110},{prop:"articleTitle",label:"稿件标题"},{prop:"recType",label:"推荐类型",options:[{label:"置顶",value:"置顶"},{label:"推荐",value:"推荐"},{label:"热门",value:"热门"}]},{prop:"rankScore",label:"排序权重",width:110},{prop:"startTime",label:"开始时间"},{prop:"endTime",label:"结束时间"},{prop:"status",label:"状态",options:[{label:"生效中",value:"生效中"},{label:"已失效",value:"已失效"}]}],
    fields: [{prop:"articleId",label:"稿件ID",type:"number"},{prop:"articleTitle",label:"稿件标题",type:"input"},{prop:"recType",label:"推荐类型",type:"select",options:[{label:"置顶",value:"置顶"},{label:"推荐",value:"推荐"},{label:"热门",value:"热门"}]},{prop:"rankScore",label:"排序权重",type:"number"},{prop:"startTime",label:"开始时间",type:"datetime"},{prop:"endTime",label:"结束时间",type:"datetime"},{prop:"operator",label:"操作人",type:"input"},{prop:"status",label:"状态",type:"select",options:[{label:"生效中",value:"生效中"},{label:"已失效",value:"已失效"}]}]
  },
  newsReadStat: {
    title: '阅读统计',
    apiBase: '/api/party/news/read-stat',
    search: [],
    columns: [{prop:"articleId",label:"稿件ID",width:110},{prop:"articleTitle",label:"稿件标题"},{prop:"statDate",label:"统计日期"},{prop:"pv",label:"浏览量",width:110},{prop:"uv",label:"独立访客",width:110},{prop:"likeCount",label:"点赞数",width:110}],
    fields: [{prop:"articleId",label:"稿件ID",type:"number"},{prop:"articleTitle",label:"稿件标题",type:"input"},{prop:"statDate",label:"统计日期",type:"date"},{prop:"pv",label:"浏览量",type:"number"},{prop:"uv",label:"独立访客",type:"number"},{prop:"likeCount",label:"点赞数",type:"number"},{prop:"commentCount",label:"评论数",type:"number"},{prop:"shareCount",label:"转发数",type:"number"}]
  },
  newsPublish: {
    title: '多端发布',
    apiBase: '/api/party/news/publish',
    search: [{prop:"status",label:"状态",type:"select",options:[{label:"待发布",value:"待发布"},{label:"已发布",value:"已发布"},{label:"已撤回",value:"已撤回"}]}],
    columns: [{prop:"articleId",label:"稿件ID",width:110},{prop:"articleTitle",label:"稿件标题"},{prop:"channel",label:"发布渠道",options:[{label:"门户网站",value:"门户网站"},{label:"移动APP",value:"移动APP"},{label:"微信公众号",value:"微信公众号"},{label:"大屏端",value:"大屏端"}]},{prop:"publisher",label:"发布人"},{prop:"publishTime",label:"发布时间"},{prop:"url",label:"访问链接"},{prop:"status",label:"状态",options:[{label:"待发布",value:"待发布"},{label:"已发布",value:"已发布"},{label:"已撤回",value:"已撤回"}]}],
    fields: [{prop:"articleId",label:"稿件ID",type:"number"},{prop:"articleTitle",label:"稿件标题",type:"input"},{prop:"channel",label:"发布渠道",type:"select",options:[{label:"门户网站",value:"门户网站"},{label:"移动APP",value:"移动APP"},{label:"微信公众号",value:"微信公众号"},{label:"大屏端",value:"大屏端"}]},{prop:"publisher",label:"发布人",type:"input"},{prop:"publishTime",label:"发布时间",type:"datetime"},{prop:"url",label:"访问链接",type:"input"},{prop:"status",label:"状态",type:"select",options:[{label:"待发布",value:"待发布"},{label:"已发布",value:"已发布"},{label:"已撤回",value:"已撤回"}]}]
  }
}
