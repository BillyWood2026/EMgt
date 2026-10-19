package com.example.culturemgt.survey.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.survey.entity.SurveyDimension;
import com.example.culturemgt.survey.entity.SurveyQuestionnaire;
import com.example.culturemgt.survey.entity.SurveyResponse;
import com.example.culturemgt.survey.mapper.SurveyDimensionMapper;
import com.example.culturemgt.survey.mapper.SurveyQuestionnaireMapper;
import com.example.culturemgt.survey.mapper.SurveyResponseMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 问卷编辑器 / 模板库 / 发布 / 回收进度。 */
@Service
public class QuestionnaireService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> PERIOD_TYPES = Set.of("季度", "年度", "专项");
    private static final Set<String> QTYPES = Set.of("单选", "多选", "量表", "文本");

    @Autowired private SurveyQuestionnaireMapper mapper;
    @Autowired private SurveyResponseMapper responseMapper;
    @Autowired private SurveyDimensionMapper dimensionMapper;
    @Autowired private AuditService audit;

    public List<SurveyQuestionnaire> list(String status, String periodType, Boolean templateOnly) {
        LambdaQueryWrapper<SurveyQuestionnaire> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            w.eq(SurveyQuestionnaire::getStatus, status);
        }
        if (periodType != null && !periodType.isBlank()) {
            w.eq(SurveyQuestionnaire::getPeriodType, periodType);
        }
        if (templateOnly != null) {
            w.eq(SurveyQuestionnaire::getTemplateFlag, templateOnly ? 1 : 0);
        }
        w.orderByDesc(SurveyQuestionnaire::getId);
        return mapper.selectList(w);
    }

    public SurveyQuestionnaire get(Long id) {
        SurveyQuestionnaire q = mapper.selectById(id);
        SurveyFlow.require(q != null, "01", "问卷不存在");
        return q;
    }

    /** 保存草稿（新增/编辑）；仅草稿可编辑，模板不可编辑。 */
    public SurveyQuestionnaire save(SurveyQuestionnaire q) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyFlow.require(SurveyFlow.len(q.getTitle()) >= 2 && SurveyFlow.len(q.getTitle()) <= 100,
                "02", "问卷名称需2-100字");
        SurveyFlow.require(PERIOD_TYPES.contains(q.getPeriodType()), "03", "调研周期类型须为 季度/年度/专项");
        List<Map<String, Object>> qs = parseQuestions(q.getQuestions());
        validateQuestions(qs);
        LocalDateTime now = LocalDateTime.now();
        if (q.getId() == null) {
            SurveyQuestionnaire dup = mapper.selectOne(new LambdaQueryWrapper<SurveyQuestionnaire>()
                    .eq(SurveyQuestionnaire::getTitle, q.getTitle().trim())
                    .eq(SurveyQuestionnaire::getDeleted, 0).last("limit 1"));
            SurveyFlow.require(dup == null, "02", "同名问卷已存在");
            q.setId(null);
            q.setStatus("草稿");
            q.setTemplateFlag(q.getTemplateFlag() == null ? 0 : q.getTemplateFlag());
            q.setAnonymousFlag(q.getAnonymousFlag() == null ? 1 : q.getAnonymousFlag());
            q.setTargetCount(q.getTargetCount() == null ? 0 : q.getTargetCount());
            q.setCreator(CurrentHeader.operator());
            mapper.insert(q);
            audit.log("NEW", "cul_survey_questionnaire", q.getId(), "新建调研问卷：" + q.getTitle());
            return q;
        }
        SurveyQuestionnaire old = mapper.selectById(q.getId());
        SurveyFlow.require(old != null, "01", "问卷不存在");
        SurveyFlow.require("草稿".equals(old.getStatus()), "07", "仅草稿状态问卷可编辑");
        SurveyFlow.require(old.getTemplateFlag() == null || old.getTemplateFlag() == 0, "07", "内置模板不可编辑，请复制后修改");
        old.setTitle(q.getTitle().trim());
        old.setPeriodType(q.getPeriodType());
        old.setAnonymousFlag(q.getAnonymousFlag() == null ? 1 : q.getAnonymousFlag());
        old.setTargetOrg(q.getTargetOrg());
        old.setStartTime(q.getStartTime());
        old.setEndTime(q.getEndTime());
        old.setQuestions(MAPPER.valueToTree(qs).toString());
        old.setTargetCount(q.getTargetCount() == null ? 0 : q.getTargetCount());
        old.setUpdateTime(now);
        mapper.updateById(old);
        audit.log("EDIT", "cul_survey_questionnaire", old.getId(), "编辑问卷草稿：" + old.getTitle());
        return old;
    }

    /** 从内置模板复制创建草稿。 */
    public SurveyQuestionnaire copyTemplate(Long templateId) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyQuestionnaire t = get(templateId);
        SurveyFlow.require(t.getTemplateFlag() != null && t.getTemplateFlag() == 1, "01", "来源不是内置模板");
        SurveyQuestionnaire q = new SurveyQuestionnaire();
        q.setTitle(t.getTitle().replace("模板-", "") + "-副本");
        q.setPeriodType(t.getPeriodType());
        q.setAnonymousFlag(t.getAnonymousFlag());
        q.setTargetOrg(t.getTargetOrg());
        q.setQuestions(t.getQuestions());
        q.setTargetCount(0);
        q.setStatus("草稿");
        q.setTemplateFlag(0);
        q.setCreator(CurrentHeader.operator());
        mapper.insert(q);
        audit.log("COPY", "cul_survey_questionnaire", q.getId(), "从模板复制问卷：" + t.getTitle());
        return q;
    }

    /** 发布：草稿→已发布，校验有效期与目标人数。body {targetCount,startTime,endTime,targetOrg} 可选覆盖。 */
    public SurveyQuestionnaire publish(Long id, SurveyQuestionnaire body) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyQuestionnaire q = get(id);
        SurveyFlow.require("草稿".equals(q.getStatus()), "07", "仅草稿状态问卷可发布");
        SurveyFlow.require(q.getTemplateFlag() == null || q.getTemplateFlag() == 0, "07", "内置模板不可直接发布");
        LocalDateTime start = body.getStartTime() != null ? body.getStartTime() : q.getStartTime();
        LocalDateTime end = body.getEndTime() != null ? body.getEndTime() : q.getEndTime();
        Integer target = body.getTargetCount() != null ? body.getTargetCount() : q.getTargetCount();
        SurveyFlow.require(start != null && end != null && end.isAfter(start), "06", "问卷有效期起止必填且止须晚于起");
        SurveyFlow.require(target != null && target > 0, "12", "发布前须设置目标参与人数");
        validateQuestions(parseQuestions(q.getQuestions()));
        q.setStartTime(start);
        q.setEndTime(end);
        q.setTargetCount(target);
        if (body.getTargetOrg() != null && !body.getTargetOrg().isBlank()) {
            q.setTargetOrg(body.getTargetOrg());
        }
        q.setStatus("已发布");
        q.setPublisher(CurrentHeader.operator());
        q.setPublishTime(LocalDateTime.now());
        mapper.updateById(q);
        audit.log("PUBLISH", "cul_survey_questionnaire", id, "发布问卷：" + q.getTitle() + "，目标" + target + "人");
        return q;
    }

    /** 手动结束问卷。 */
    public SurveyQuestionnaire finish(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyQuestionnaire q = get(id);
        SurveyFlow.require("已发布".equals(q.getStatus()), "07", "仅已发布问卷可结束");
        q.setStatus("已结束");
        mapper.updateById(q);
        audit.log("FINISH", "cul_survey_questionnaire", id, "结束问卷：" + q.getTitle());
        return q;
    }

    /** 删除：仅草稿可删（已发布/已结束禁删，保证审计可追溯）。 */
    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        SurveyQuestionnaire q = get(id);
        Long used = responseMapper.selectCount(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getQuestionnaireId, id).eq(SurveyResponse::getDeleted, 0));
        SurveyFlow.require("草稿".equals(q.getStatus()) && used == 0, "08", "已发布/已有答卷的问卷禁止删除，请改用结束");
        mapper.deleteById(id);
        audit.log("DELETE", "cul_survey_questionnaire", id, "删除草稿问卷：" + q.getTitle());
    }

    /** 回收进度：答卷数/参与率/部门分布/题项统计概览。 */
    public Map<String, Object> progress(Long id) {
        SurveyQuestionnaire q = get(id);
        List<SurveyResponse> rs = responseMapper.selectList(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getQuestionnaireId, id).eq(SurveyResponse::getDeleted, 0));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("questionnaire", q);
        m.put("submitted", rs.size());
        m.put("targetCount", q.getTargetCount() == null ? 0 : q.getTargetCount());
        double rate = q.getTargetCount() != null && q.getTargetCount() > 0
                ? Math.round(rs.size() * 10000.0 / q.getTargetCount()) / 100.0 : 0;
        m.put("participationRate", Math.min(100, rate));
        Map<String, Integer> deptDist = new LinkedHashMap<>();
        rs.forEach(r -> deptDist.merge(r.getRespondentDept() == null ? "未知部门" : r.getRespondentDept(), 1, Integer::sum));
        m.put("deptDistribution", deptDist);
        return m;
    }

    // ---------- 题目校验 ----------

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> parseQuestions(String json) {
        SurveyFlow.require(json != null && !json.isBlank(), "04", "问卷题目为空，请至少配置1道题");
        try {
            List<Map<String, Object>> qs = MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
            SurveyFlow.require(qs != null && !qs.isEmpty(), "04", "问卷题目为空，请至少配置1道题");
            return qs;
        } catch (com.example.culturemgt.common.cul.CulBizException be) {
            throw be;
        } catch (Exception e) {
            throw new IllegalStateException("题目JSON解析失败：" + e.getMessage());
        }
    }

    private void validateQuestions(List<Map<String, Object>> qs) {
        Set<String> dimNames = dimensionMapper.selectList(new LambdaQueryWrapper<SurveyDimension>()
                        .eq(SurveyDimension::getStatus, "启用").eq(SurveyDimension::getDeleted, 0))
                .stream().map(SurveyDimension::getDimensionName).collect(Collectors.toSet());
        int idx = 0;
        for (Map<String, Object> q : qs) {
            idx++;
            String type = String.valueOf(q.get("type"));
            String stem = String.valueOf(q.getOrDefault("stem", ""));
            SurveyFlow.require(QTYPES.contains(type), "04", "第" + idx + "题题型非法（须 单选/多选/量表/文本）");
            SurveyFlow.require(!stem.isBlank() && !"null".equals(stem), "04", "第" + idx + "题题干必填");
            if ("单选".equals(type) || "多选".equals(type)) {
                List<Object> opts = (List<Object>) q.getOrDefault("options", new ArrayList<>());
                SurveyFlow.require(opts.size() >= 2, "04", "第" + idx + "题选项至少2项");
            }
            if ("量表".equals(type)) {
                int max = q.get("maxScale") == null ? 5 : Integer.parseInt(String.valueOf(q.get("maxScale")));
                SurveyFlow.require(max >= 2 && max <= 10, "05", "第" + idx + "题量表上限须在2-10之间（默认5）");
                String dim = String.valueOf(q.getOrDefault("dimension", ""));
                SurveyFlow.require(!dim.isBlank() && !"null".equals(dim), "04", "第" + idx + "道量表题必须归属氛围维度");
                SurveyFlow.require(dimNames.contains(dim), "04", "第" + idx + "题维度「" + dim + "」不在启用维度配置中");
            }
        }
    }
}
