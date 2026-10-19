package com.example.culturemgt.news.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.asset.entity.CultureAsset;
import com.example.culturemgt.asset.service.AssetService;
import com.example.culturemgt.casehub.entity.CaseItem;
import com.example.culturemgt.casehub.mapper.CaseItemMapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.honor.entity.HonorApply;
import com.example.culturemgt.honor.mapper.HonorApplyMapper;
import com.example.culturemgt.news.entity.News;
import com.example.culturemgt.news.entity.NewsApproval;
import com.example.culturemgt.news.entity.NewsVersion;
import com.example.culturemgt.news.entity.NewsTag;
import com.example.culturemgt.news.mapper.NewsApprovalMapper;
import com.example.culturemgt.news.mapper.NewsMapper;
import com.example.culturemgt.news.mapper.NewsVersionMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 子模块9 新闻宣传管理。
 * 状态机：草稿 → 待审核 → 已发布 → 已下线归档；驳回退回草稿；归档仅查看不可编辑删除。
 * BPM两套审批链（按 source_org 自动判断）：
 *   集团稿件：C03集团文化管理员审核 → C07分管高管终审（2节点）
 *   子公司稿件：C04子公司负责人初审 → C03集团文化管理员复核 → C07集团高管终审（3节点）
 */
@Service
public class NewsService {

    private final NewsMapper newsMapper;
    private final NewsVersionMapper versionMapper;
    private final NewsApprovalMapper approvalMapper;
    private final CaseItemMapper caseMapper;
    private final HonorApplyMapper honorMapper;
    private final AssetService assetService;
    private final NewsTagService tagService;
    private final AuditService auditService;

    public static final String[] TYPES = {"文化动态", "榜样事迹", "高管寄语", "文化专题", "活动报道"};
    public static final String HQ_ORG = "集团总部";
    private static final int TOP_LIMIT = 5;

    public NewsService(NewsMapper newsMapper, NewsVersionMapper versionMapper,
                       NewsApprovalMapper approvalMapper, CaseItemMapper caseMapper,
                       HonorApplyMapper honorMapper, AssetService assetService,
                       NewsTagService tagService, AuditService auditService) {
        this.newsMapper = newsMapper;
        this.versionMapper = versionMapper;
        this.approvalMapper = approvalMapper;
        this.caseMapper = caseMapper;
        this.honorMapper = honorMapper;
        this.assetService = assetService;
        this.tagService = tagService;
        this.auditService = auditService;
    }

    // ============ 查询 ============

    /** 后台工作台列表（管理角色可见） */
    public List<News> list(String status, String newsType, String org, String keyword) {
        CulRole.requireAny(CulRole.BACKSTAGE);
        LambdaQueryWrapper<News> w = new LambdaQueryWrapper<>();
        w.eq(News::getDeleted, 0);
        if (notBlank(status)) w.eq(News::getStatus, status);
        if (notBlank(newsType)) w.eq(News::getNewsType, newsType);
        if (notBlank(org)) w.eq(News::getSourceOrg, org);
        if (notBlank(keyword)) {
            w.and(x -> x.like(News::getTitle, keyword).or().like(News::getSummary, keyword));
        }
        w.orderByDesc(News::getIsTop).orderByDesc(News::getUpdateTime);
        return newsMapper.selectList(w);
    }

    /** 前台公开列表：仅已发布，置顶优先+发布时间倒序 */
    public List<News> publicList(String newsType, String tag, String keyword) {
        LambdaQueryWrapper<News> w = new LambdaQueryWrapper<>();
        w.eq(News::getDeleted, 0).eq(News::getStatus, "已发布");
        if (notBlank(newsType)) w.eq(News::getNewsType, newsType);
        if (notBlank(tag)) w.like(News::getValueTags, "\"" + tag + "\"");
        if (notBlank(keyword)) {
            w.and(x -> x.like(News::getTitle, keyword).or().like(News::getSummary, keyword));
        }
        w.orderByDesc(News::getIsTop).orderByDesc(News::getPublishTime);
        return newsMapper.selectList(w);
    }

    /** 详情：附带关联案例标题/榜样姓名，供抽屉展示 */
    public Map<String, Object> get(Long id) {
        News n = must(id);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("news", n);
        if (n.getRelCaseId() != null) {
            CaseItem c = caseMapper.selectById(n.getRelCaseId());
            if (c != null) r.put("relCaseTitle", c.getCaseTitle());
        }
        if (n.getRelModelId() != null) {
            HonorApply h = honorMapper.selectById(n.getRelModelId());
            if (h != null) r.put("relModelName", h.getCandidate() + " - " + h.getProjectName());
        }
        return r;
    }

    /** 审批待办：仅当前登录人具备当前节点审批角色的稿件 */
    public List<News> todo() {
        CulRole.requireAny(CulRole.BACKSTAGE);
        List<News> pending = newsMapper.selectList(new LambdaQueryWrapper<News>()
                .eq(News::getDeleted, 0).eq(News::getStatus, "待审核")
                .orderByAsc(News::getSubmitTime));
        return pending.stream().filter(n -> CulRole.hasAny(stageRoles(n))).collect(Collectors.toList());
    }

    public List<NewsVersion> versions(Long newsId) {
        must(newsId);
        return versionMapper.selectList(new LambdaQueryWrapper<NewsVersion>()
                .eq(NewsVersion::getNewsId, newsId).orderByAsc(NewsVersion::getVerNo));
    }

    public List<NewsApproval> approvals(Long newsId) {
        must(newsId);
        return approvalMapper.selectList(new LambdaQueryWrapper<NewsApproval>()
                .eq(NewsApproval::getNewsId, newsId).orderByAsc(NewsApproval::getApproveTime));
    }

    /** 统计分析：总量/状态分布/类型占比/组织产出与阅读 */
    public Map<String, Object> stats() {
        CulRole.requireAny(CulRole.BACKSTAGE);
        List<News> all = newsMapper.selectList(new LambdaQueryWrapper<News>().eq(News::getDeleted, 0));
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", all.size());
        r.put("draft", all.stream().filter(n -> "草稿".equals(n.getStatus())).count());
        r.put("pending", all.stream().filter(n -> "待审核".equals(n.getStatus())).count());
        r.put("published", all.stream().filter(n -> "已发布".equals(n.getStatus())).count());
        r.put("offline", all.stream().filter(n -> "已下线归档".equals(n.getStatus())).count());
        r.put("totalRead", all.stream().mapToInt(n -> nz(n.getReadCount())).sum());
        r.put("mobileRead", all.stream().mapToInt(n -> nz(n.getMobileReadCount())).sum());
        r.put("totalLike", all.stream().mapToInt(n -> nz(n.getLikeCount())).sum());
        r.put("topRead", all.stream().filter(n -> n.getIsTop() != null && n.getIsTop() == 1)
                .mapToInt(n -> nz(n.getReadCount())).sum());
        r.put("byType", Arrays.stream(TYPES).map(t -> {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("type", t);
            m.put("count", all.stream().filter(n -> t.equals(n.getNewsType())).count());
            return m;
        }).collect(Collectors.toList()));
        Map<String, List<News>> byOrg = all.stream()
                .collect(Collectors.groupingBy(n -> n.getSourceOrg() == null ? "未知" : n.getSourceOrg()));
        r.put("byOrg", byOrg.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("org", e.getKey());
            m.put("count", e.getValue().size());
            m.put("read", e.getValue().stream().mapToInt(n -> nz(n.getReadCount())).sum());
            return m;
        }).sorted((a, b) -> Long.compare(((Number) b.get("count")).longValue(), ((Number) a.get("count")).longValue()))
          .collect(Collectors.toList()));
        return r;
    }

    // ============ 写操作 ============

    /** 新建/编辑稿件：校验+每次保存自动生成版本快照 */
    @Transactional
    public News save(News n) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        validate(n);
        // 子公司专员不能将稿件归属集团总部
        if (n.getId() == null && HQ_ORG.equals(n.getSourceOrg()) && CulRole.hasAny(CulRole.C02) && !CulRole.hasAny(CulRole.C03, CulRole.C99)) {
            throw NewsFlow.block("06", "子公司文化专员不能将稿件归属集团总部");
        }
        // 重复标题预警（确认后可放行）
        Long dup = newsMapper.selectCount(new LambdaQueryWrapper<News>()
                .eq(News::getDeleted, 0).eq(News::getTitle, n.getTitle().trim())
                .ne(n.getId() != null, News::getId, n.getId()));
        if (dup > 0 && !Boolean.TRUE.equals(n.getConfirmDuplicate())) {
            throw NewsFlow.warn("18", "存在相同标题的稿件，请确认是否重复创建（确认后可再次保存）");
        }
        // 置顶上限：同一组织最多5条
        if (n.getIsTop() != null && n.getIsTop() == 1) {
            Long tops = newsMapper.selectCount(new LambdaQueryWrapper<News>()
                    .eq(News::getDeleted, 0).eq(News::getIsTop, 1)
                    .eq(News::getSourceOrg, n.getSourceOrg())
                    .ne(n.getId() != null, News::getId, n.getId()));
            NewsFlow.require(tops < TOP_LIMIT, "15", "同一组织置顶稿件最多" + TOP_LIMIT + "条，当前已达上限");
        }
        LocalDateTime now = LocalDateTime.now();
        if (n.getId() == null) {
            n.setNewsNo(nextNo());
            n.setStatus("草稿");
            n.setStage(0);
            n.setCreator(CurrentHeader.operator());
            n.setReadCount(0);
            n.setMobileReadCount(0);
            n.setLikeCount(0);
            n.setCreateTime(now);
        } else {
            News old = must(n.getId());
            NewsFlow.require("草稿".equals(old.getStatus()), "07", "仅草稿状态可编辑，当前：" + old.getStatus());
            requireOwner(old);
            n.setNewsNo(old.getNewsNo());
            n.setStatus(old.getStatus());
            n.setStage(old.getStage());
            n.setCreator(old.getCreator());
            n.setReadCount(old.getReadCount());
            n.setMobileReadCount(old.getMobileReadCount());
            n.setLikeCount(old.getLikeCount());
            n.setSubmitTime(old.getSubmitTime());
            n.setPublishTime(old.getPublishTime());
            n.setOfflineReason(old.getOfflineReason());
        }
        n.setTitle(n.getTitle().trim());
        n.setUpdateTime(now);
        if (n.getId() == null) {
            newsMapper.insert(n);
        } else {
            newsMapper.updateById(n);
        }
        snapshot(n);
        auditService.log(n.getNewsNo() == null ? "NEW" : "EDIT", "cul_news", n.getId(),
                CurrentHeader.operator() + " 保存稿件《" + n.getTitle() + "》");
        return n;
    }

    /** 删除草稿（归档/已发布/待审不可删除） */
    @Transactional
    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        News n = must(id);
        NewsFlow.require("草稿".equals(n.getStatus()), "07", "仅草稿状态可删除，已发布/归档稿件禁止删除");
        requireOwner(n);
        newsMapper.deleteById(id);
        auditService.log("DELETE", "cul_news", id, CurrentHeader.operator() + " 删除草稿《" + n.getTitle() + "》");
    }

    /** 提交审批：草稿 → 待审核，进入BPM第1节点 */
    @Transactional
    public News submit(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        News n = must(id);
        NewsFlow.require("草稿".equals(n.getStatus()), "07", "仅草稿状态可提交审批");
        requireOwner(n);
        n.setStatus("待审核");
        n.setStage(1);
        n.setSubmitTime(LocalDateTime.now());
        n.setUpdateTime(n.getSubmitTime());
        newsMapper.updateById(n);
        auditService.log("SUBMIT", "cul_news", id, CurrentHeader.operator() + " 提交稿件《" + n.getTitle() + "》进入审批");
        return n;
    }

    /** 撤回：待审核 → 草稿（撰稿人） */
    @Transactional
    public News withdraw(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        News n = must(id);
        NewsFlow.require("待审核".equals(n.getStatus()), "07", "仅待审核状态可撤回");
        requireOwner(n);
        n.setStatus("草稿");
        n.setStage(0);
        n.setUpdateTime(LocalDateTime.now());
        newsMapper.updateById(n);
        auditService.log("WITHDRAW", "cul_news", id, CurrentHeader.operator() + " 撤回稿件《" + n.getTitle() + "》");
        return n;
    }

    /** 审批：通过进入下一节点/终审通过即发布；驳回退回草稿（意见必填留痕） */
    @Transactional
    public News approve(Long id, boolean pass, String opinion) {
        News n = must(id);
        NewsFlow.require("待审核".equals(n.getStatus()), "07", "仅待审核状态可审批");
        String[] roles = stageRoles(n);
        String node = stageNode(n);
        NewsFlow.require(CulRole.hasAny(roles), "16", "当前节点【" + node + "】需 "
                + String.join("/", roles) + " 角色审批");
        LocalDateTime now = LocalDateTime.now();
        if (!pass) {
            NewsFlow.require(opinion != null && opinion.trim().length() >= 20 && opinion.trim().length() <= 500,
                    "10", "驳回意见必填且需20-500字（当前" + (opinion == null ? 0 : opinion.trim().length()) + "字）");
            logApproval(n, node, "驳回", opinion.trim());
            n.setStatus("草稿");
            n.setStage(0);
        } else {
            logApproval(n, node, "通过", opinion);
            int maxStage = maxStage(n);
            if (n.getStage() >= maxStage) {
                n.setStatus("已发布");
                n.setPublishTime(now);
                onPublished(n);
            } else {
                n.setStage(n.getStage() + 1);
            }
        }
        n.setUpdateTime(now);
        newsMapper.updateById(n);
        auditService.log(pass ? "APPROVE" : "REJECT", "cul_news", id,
                CurrentHeader.operator() + " 在节点【" + node + "】" + (pass ? "通过" : "驳回") + "稿件《" + n.getTitle() + "》");
        return n;
    }

    /** 下线归档：已发布 → 已下线归档（原因必填，置顶自动取消） */
    @Transactional
    public News offline(Long id, String reason) {
        CulRole.requireAny(CulRole.CULTURE_HQ);
        News n = must(id);
        NewsFlow.require("已发布".equals(n.getStatus()), "07", "仅已发布状态可下线归档");
        NewsFlow.require(reason != null && !reason.trim().isEmpty(), "11", "下线原因必填");
        n.setStatus("已下线归档");
        n.setOfflineReason(reason.trim());
        n.setIsTop(0);
        n.setUpdateTime(LocalDateTime.now());
        newsMapper.updateById(n);
        auditService.log("OFFLINE", "cul_news", id,
                CurrentHeader.operator() + " 下线归档稿件《" + n.getTitle() + "》，原因：" + reason.trim());
        return n;
    }

    /** 前台阅读计数（channel=mobile 计移动端） */
    @Transactional
    public News view(Long id, String channel) {
        News n = must(id);
        NewsFlow.require("已发布".equals(n.getStatus()), "07", "仅已发布新闻可阅读");
        n.setReadCount(nz(n.getReadCount()) + 1);
        if ("mobile".equals(channel)) {
            n.setMobileReadCount(nz(n.getMobileReadCount()) + 1);
        }
        newsMapper.updateById(n);
        return n;
    }

    /** 点赞 */
    @Transactional
    public News like(Long id) {
        News n = must(id);
        NewsFlow.require("已发布".equals(n.getStatus()), "07", "仅已发布新闻可点赞");
        n.setLikeCount(nz(n.getLikeCount()) + 1);
        newsMapper.updateById(n);
        return n;
    }

    /** 稿件配图一键存入文化资产库（自动打"新闻来源"标签） */
    @Transactional
    public CultureAsset material(Map<String, String> body) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        String url = body == null ? null : body.get("url");
        String name = body == null || body.get("name") == null || body.get("name").isBlank()
                ? "新闻配图素材" : body.get("name");
        NewsFlow.require(url != null && !url.isBlank(), "04", "素材URL不能为空");
        CultureAsset a = new CultureAsset();
        a.setAssetName(name.trim());
        a.setAssetType("其他");
        a.setTags("[\"新闻来源\",\"" + name.trim() + "\"]");
        a.setFileUrl(url.trim());
        a.setFileSize(0L);
        a.setOrgScope("[\"集团总部\"]");
        return assetService.save(a);
    }

    // ============ 内部工具 ============

    private int maxStage(News n) {
        return HQ_ORG.equals(n.getSourceOrg()) ? 2 : 3;
    }

    /** 当前审批节点的角色与节点名 */
    private String[] stageRoles(News n) {
        boolean hq = HQ_ORG.equals(n.getSourceOrg());
        int s = n.getStage() == null ? 0 : n.getStage();
        if (hq) {
            return s == 1 ? new String[]{CulRole.C03, CulRole.C99} : new String[]{CulRole.C07, CulRole.C99};
        }
        return switch (s) {
            case 1 -> new String[]{CulRole.C04, CulRole.C99};
            case 2 -> new String[]{CulRole.C03, CulRole.C99};
            default -> new String[]{CulRole.C07, CulRole.C99};
        };
    }

    private String stageNode(News n) {
        boolean hq = HQ_ORG.equals(n.getSourceOrg());
        int s = n.getStage() == null ? 0 : n.getStage();
        if (hq) {
            return s == 1 ? "集团文化管理员审核" : "分管高管终审";
        }
        return switch (s) {
            case 1 -> "子公司负责人初审";
            case 2 -> "集团文化管理员复核";
            default -> "集团高管终审";
        };
    }

    private void onPublished(News n) {
        // 联动：新闻发布后对应案例被引用计数+1
        if (n.getRelCaseId() != null) {
            CaseItem c = caseMapper.selectById(n.getRelCaseId());
            if (c != null) {
                c.setRefNewsCount(nz(c.getRefNewsCount()) + 1);
                caseMapper.updateById(c);
            }
        }
    }

    private void logApproval(News n, String node, String action, String opinion) {
        NewsApproval a = new NewsApproval();
        a.setNewsId(n.getId());
        a.setNodeName(node);
        a.setApprover(CurrentHeader.operator());
        a.setAction(action);
        a.setOpinion(opinion);
        a.setApproveTime(LocalDateTime.now());
        approvalMapper.insert(a);
    }

    /** 版本快照：每次保存自动生成，永久保存 */
    private void snapshot(News n) {
        Long cnt = versionMapper.selectCount(new LambdaQueryWrapper<NewsVersion>()
                .eq(NewsVersion::getNewsId, n.getId()));
        NewsVersion v = new NewsVersion();
        v.setNewsId(n.getId());
        v.setVerNo(cnt.intValue() + 1);
        v.setTitle(n.getTitle());
        v.setSummary(n.getSummary());
        v.setContent(n.getContent());
        v.setEditor(CurrentHeader.operator());
        v.setEditTime(LocalDateTime.now());
        versionMapper.insert(v);
    }

    private void requireOwner(News n) {
        boolean owner = CurrentHeader.operator().equals(n.getCreator());
        if (HQ_ORG.equals(n.getSourceOrg())) {
            NewsFlow.require(owner || CulRole.hasAny(CulRole.C03, CulRole.C99),
                    "09", "集团稿件仅撰稿本人或集团文化管理员可操作（越权组织）");
        } else {
            NewsFlow.require(owner || CulRole.hasAny(CulRole.C99),
                    "08", "仅撰稿本人可操作本组织稿件");
        }
    }

    private void validate(News n) {
        NewsFlow.require(n.getTitle() != null && n.getTitle().trim().length() >= 10
                && n.getTitle().trim().length() <= 120, "02",
                "标题需10-120字（当前" + (n.getTitle() == null ? 0 : n.getTitle().trim().length()) + "字）");
        NewsFlow.require(n.getSummary() != null && n.getSummary().trim().length() >= 30
                && n.getSummary().trim().length() <= 300, "03",
                "摘要需30-300字（当前" + (n.getSummary() == null ? 0 : n.getSummary().trim().length()) + "字）");
        NewsFlow.require(n.getContent() != null && !n.getContent().isBlank(), "04", "正文内容必填");
        NewsFlow.require(n.getNewsType() != null && Arrays.asList(TYPES).contains(n.getNewsType()),
                "05", "新闻类型非法，需为：" + String.join("/", TYPES));
        NewsFlow.require(n.getSourceOrg() != null && !n.getSourceOrg().isBlank(), "06", "来源所属组织必填");
        if (n.getPlanPublishTime() != null && n.getPlanPublishTime().isBefore(LocalDateTime.now())) {
            News old = n.getId() == null ? null : newsMapper.selectById(n.getId());
            boolean unchanged = old != null && n.getPlanPublishTime().equals(old.getPlanPublishTime());
            NewsFlow.require(unchanged, "12", "计划发布时间不能早于当前时间");
        }
        // 标签必须存在且启用
        for (String t : parseTags(n.getValueTags())) {
            NewsTag tag = tagService.find(t);
            NewsFlow.require(tag != null && tag.getEnabled() == 1, "14", "价值观标签不存在或已停用：" + t);
        }
    }

    public News must(Long id) {
        News n = newsMapper.selectById(id);
        NewsFlow.require(n != null && n.getDeleted() == 0, "01", "稿件不存在");
        return n;
    }

    private String nextNo() {
        String prefix = "XW" + LocalDateTime.now().toString().substring(0, 10).replace("-", "");
        Long cnt = newsMapper.selectCount(new LambdaQueryWrapper<News>()
                .likeRight(News::getNewsNo, prefix));
        return prefix + String.format("%04d", cnt + 1);
    }

    static List<String> parseTags(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return new ObjectMapper().readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
