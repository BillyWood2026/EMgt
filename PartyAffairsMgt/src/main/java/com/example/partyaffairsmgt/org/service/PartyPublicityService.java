package com.example.partyaffairsmgt.org.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.party.AuditService;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.org.entity.PartyAffairPublicity;
import com.example.partyaffairsmgt.org.entity.PublicityConfig;
import com.example.partyaffairsmgt.org.mapper.PartyAffairPublicityMapper;
import com.example.partyaffairsmgt.org.mapper.PublicityConfigMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 4.6 党务公开：事项台账、脱敏校验、范围权限、默认期限、到期处理。 */
@Service
public class PartyPublicityService {

    /** 18 位身份证（末位 X）。 */
    private static final Pattern ID_CARD = Pattern.compile("\\d{17}[\\dXx]");
    /** 11 位手机号（且相邻不是更长数字串，避免误报脱敏后片段——此处直接检测完整手机号）。 */
    private static final Pattern MOBILE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    /** 已脱敏手机号（如 138****1234）。 */
    private static final Pattern MOBILE_MASKED = Pattern.compile("1[3-9]\\d{1}\\*{4}\\d{4}");

    @Autowired private PartyAffairPublicityMapper publicityMapper;
    @Autowired private PublicityConfigMapper configMapper;
    @Autowired private AuditService auditService;

    // ---------- 配置 ----------

    public List<PublicityConfig> configs() {
        return configMapper.selectList(new QueryWrapper<PublicityConfig>().orderByAsc("id"));
    }

    @Transactional
    public PublicityConfig saveConfig(PublicityConfig c) {
        if (c.getCategory() == null || c.getCategory().isBlank()) {
            throw PartyBizException.block("E-PUB-01", "公开事项类别必填");
        }
        if (c.getDurationDays() == null || c.getDurationDays() < 1) {
            throw PartyBizException.block("E-PUB-01", "默认公开期限必须≥1天");
        }
        LocalDateTime now = LocalDateTime.now();
        if (c.getId() == null) {
            c.setCreateTime(now);
            c.setUpdateTime(now);
            configMapper.insert(c);
        } else {
            c.setUpdateTime(now);
            configMapper.updateById(c);
        }
        return c;
    }

    // ---------- 台账 ----------

    public List<PartyAffairPublicity> list(Long orgId, String category, String status, Integer year) {
        QueryWrapper<PartyAffairPublicity> qw = new QueryWrapper<>();
        if (orgId != null) {
            qw.eq("org_id", orgId);
        }
        if (category != null && !category.isBlank()) {
            qw.eq("category", category);
        }
        if (status != null && !status.isBlank()) {
            qw.eq("status", status);
        }
        if (year != null) {
            qw.apply("year(create_time)={0}", year);
        }
        qw.orderByDesc("update_time", "id");
        return publicityMapper.selectList(qw);
    }

    @Transactional
    public PartyAffairPublicity save(PartyAffairPublicity p) {
        if (p.getTitle() == null || p.getTitle().isBlank()) {
            throw PartyBizException.block("E-PUB-01", "公开标题必填");
        }
        if (p.getCategory() == null) {
            throw PartyBizException.block("E-PUB-01", "公开类别必填");
        }
        LocalDateTime now = LocalDateTime.now();
        if (p.getId() == null) {
            if (p.getStatus() == null) {
                p.setStatus("草稿");
            }
            p.setPublished(0);
            p.setCreateTime(now);
            p.setUpdateTime(now);
            publicityMapper.insert(p);
        } else {
            p.setUpdateTime(now);
            publicityMapper.updateById(p);
        }
        return p;
    }

    @Transactional
    public void delete(Long id) {
        PartyAffairPublicity p = publicityMapper.selectById(id);
        if (p != null) {
            publicityMapper.deleteById(id);
            auditService.log("PUBLICITY_DELETE", "party_affair_publicity", id, "删除公开事项：" + p.getTitle());
        }
    }

    // ---------- 发布 ----------

    /**
     * 发布：范围权限 + 敏感信息脱敏校验 + 默认期限。
     */
    @Transactional
    public PartyAffairPublicity publish(Long id) {
        PartyAffairPublicity p = publicityMapper.selectById(id);
        if (p == null) {
            throw PartyBizException.block("E-PUB-01", "公开事项不存在：" + id);
        }
        checkScopePermission(p.getPublicScope());

        // 敏感信息：身份证 / 未脱敏手机号 → 阻断
        String text = nz(p.getTitle()) + "\n" + nz(p.getContent());
        Matcher idm = ID_CARD.matcher(text);
        if (idm.find()) {
            throw PartyBizException.block("E-PUB-02",
                    "内容含未脱敏的身份证号（" + mask(idm.group(), 6, 4) + "），禁止公开，请脱敏后再发布");
        }
        Matcher mb = MOBILE.matcher(stripMasked(text));
        if (mb.find()) {
            throw PartyBizException.block("E-PUB-02",
                    "内容含未脱敏手机号（" + mask(mb.group(), 3, 4) + "），禁止公开，请脱敏后再发布");
        }
        if (p.getApprover() == null || p.getApprover().isBlank()) {
            throw PartyBizException.block("E-PUB-03", "发布前必须登记审核批准人");
        }

        LocalDate start = p.getPeriodStart() != null ? p.getPeriodStart() : LocalDate.now();
        p.setPeriodStart(start);
        if (p.getPeriodEnd() == null || !p.getPeriodEnd().isAfter(start)) {
            int days = durationFor(p.getCategory());
            p.setPeriodEnd(start.plusDays(days));
        }
        if (p.getExpireAction() == null) {
            p.setExpireAction("自动下架");
        }
        if (p.getPublisher() == null) {
            p.setPublisher(CurrentHeader.operator());
        }
        p.setPublished(1);
        p.setPublishDate(start);
        p.setStatus("已发布");
        p.setUpdateTime(LocalDateTime.now());
        publicityMapper.updateById(p);
        auditService.log("PUBLICITY_PUBLISH", "party_affair_publicity", id,
                "发布《" + p.getTitle() + "》范围=" + p.getPublicScope()
                        + " 期限至" + p.getPeriodEnd() + " 批准人=" + p.getApprover());
        return p;
    }

    /** 公开范围权限矩阵（越权公开阻断）。 */
    private void checkScopePermission(String scope) {
        if (scope == null) {
            throw PartyBizException.block("E-PUB-04", "未选择公开范围");
        }
        switch (scope) {
            case "社会公众":
                PartyRole.requireAny(PartyRole.R02);
                break;
            case "全体职工":
                PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
                break;
            case "全体党员":
                PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
                break;
            default:
                throw PartyBizException.block("E-PUB-04", "公开范围非法：" + scope);
        }
    }

    private int durationFor(String category) {
        PublicityConfig c = configMapper.selectOne(new QueryWrapper<PublicityConfig>()
                .eq("category", category).last("limit 1"));
        return c != null && c.getDurationDays() != null ? c.getDurationDays() : 30;
    }

    private String stripMasked(String s) {
        return MOBILE_MASKED.matcher(s).replaceAll("");
    }

    private String mask(String s, int head, int tail) {
        if (s.length() <= head + tail) {
            return "****";
        }
        return s.substring(0, head) + "****" + s.substring(s.length() - tail);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    // ---------- 到期处理 ----------

    @Transactional
    public Map<String, Object> processExpired() {
        LocalDate today = LocalDate.now();
        List<PartyAffairPublicity> due = publicityMapper.selectList(new QueryWrapper<PartyAffairPublicity>()
                .eq("status", "已发布").lt("period_end", today));
        int taken = 0, archived = 0, renewed = 0;
        for (PartyAffairPublicity p : due) {
            String action = nz(p.getExpireAction()).isBlank() ? "自动下架" : p.getExpireAction();
            switch (action) {
                case "归档":
                    p.setStatus("已归档");
                    archived++;
                    break;
                case "续期":
                    p.setPeriodEnd(today.plusDays(durationFor(p.getCategory())));
                    renewed++;
                    break;
                case "自动下架":
                default:
                    p.setStatus("已下架");
                    p.setPublished(0);
                    taken++;
                    break;
            }
            p.setUpdateTime(LocalDateTime.now());
            publicityMapper.updateById(p);
            auditService.log("PUBLICITY_EXPIRE", "party_affair_publicity", p.getId(),
                    "到期处理：" + ("续期".equals(action) ? "续期至" + p.getPeriodEnd() : action));
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("total", due.size());
        r.put("takenOff", taken);
        r.put("archived", archived);
        r.put("renewed", renewed);
        return r;
    }

    /** 台账超期未发布提醒：超过 3 天仍为草稿/待审核。 */
    public List<PartyAffairPublicity> overdueUnpublished() {
        return publicityMapper.selectList(new QueryWrapper<PartyAffairPublicity>()
                .in("status", "草稿", "待审核")
                .lt("create_time", LocalDateTime.now().minusDays(3))
                .orderByAsc("create_time"));
    }
}
