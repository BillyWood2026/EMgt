package com.example.culturemgt.survey.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CurrentHeader;
import com.example.culturemgt.survey.entity.SurveyQuestionnaire;
import com.example.culturemgt.survey.entity.SurveyResponse;
import com.example.culturemgt.survey.mapper.SurveyQuestionnaireMapper;
import com.example.culturemgt.survey.mapper.SurveyResponseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 问卷作答：时间窗/匿名/必答校验，量表题自动换算维度百分制得分。 */
@Service
public class ResponseService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private SurveyResponseMapper mapper;
    @Autowired private SurveyQuestionnaireMapper questionnaireMapper;
    @Autowired private AuditService audit;

    /** 前台：我的调研（已发布问卷 + 当前用户作答状态 + 是否在作答时间窗）。 */
    public List<Map<String, Object>> myList() {
        String me = CurrentHeader.operator();
        List<SurveyQuestionnaire> qs = questionnaireMapper.selectList(new LambdaQueryWrapper<SurveyQuestionnaire>()
                .eq(SurveyQuestionnaire::getStatus, "已发布")
                .eq(SurveyQuestionnaire::getTemplateFlag, 0)
                .eq(SurveyQuestionnaire::getDeleted, 0)
                .orderByDesc(SurveyQuestionnaire::getId));
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> out = new ArrayList<>();
        for (SurveyQuestionnaire q : qs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("title", q.getTitle());
            m.put("periodType", q.getPeriodType());
            m.put("anonymousFlag", q.getAnonymousFlag());
            m.put("targetOrg", q.getTargetOrg());
            m.put("startTime", q.getStartTime());
            m.put("endTime", q.getEndTime());
            m.put("questions", q.getQuestions());
            boolean inWindow = !now.isBefore(q.getStartTime()) && !now.isAfter(q.getEndTime());
            m.put("inWindow", inWindow);
            if (q.getAnonymousFlag() != null && q.getAnonymousFlag() == 1) {
                long mine = mapper.selectCount(new LambdaQueryWrapper<SurveyResponse>()
                        .eq(SurveyResponse::getQuestionnaireId, q.getId())
                        .eq(SurveyResponse::getRespondent, me)
                        .eq(SurveyResponse::getDeleted, 0));
                m.put("submitted", mine > 0);
            } else {
                long mine = mapper.selectCount(new LambdaQueryWrapper<SurveyResponse>()
                        .eq(SurveyResponse::getQuestionnaireId, q.getId())
                        .eq(SurveyResponse::getRespondent, me)
                        .eq(SurveyResponse::getDeleted, 0));
                m.put("submitted", mine > 0);
            }
            out.add(m);
        }
        return out;
    }

    /** 后台：单份问卷答卷明细。 */
    public List<SurveyResponse> responses(Long questionnaireId) {
        return mapper.selectList(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getQuestionnaireId, questionnaireId)
                .eq(SurveyResponse::getDeleted, 0)
                .orderByDesc(SurveyResponse::getSubmitTime));
    }

    /** 提交答卷，body {answers:{qid:val}, userDept}。 */
    @Transactional
    public SurveyResponse submit(Long questionnaireId, Map<String, Object> body) {
        SurveyQuestionnaire q = questionnaireMapper.selectById(questionnaireId);
        SurveyFlow.require(q != null, "01", "问卷不存在");
        SurveyFlow.require("已发布".equals(q.getStatus()), "07", "问卷未发布或已结束");
        LocalDateTime now = LocalDateTime.now();
        SurveyFlow.require(!now.isBefore(q.getStartTime()) && !now.isAfter(q.getEndTime()), "09",
                "不在问卷作答时间窗（" + q.getStartTime() + " ~ " + q.getEndTime() + "）内");

        @SuppressWarnings("unchecked")
        Map<String, Object> answers = body.get("answers") instanceof Map
                ? (Map<String, Object>) body.get("answers") : new LinkedHashMap<>();
        String userDept = body.get("userDept") == null ? "" : String.valueOf(body.get("userDept"));
        String me = CurrentHeader.operator();
        boolean anonymous = q.getAnonymousFlag() != null && q.getAnonymousFlag() == 1;

        // 非匿名问卷同人在途/重复提交拦截
        if (!anonymous) {
            Long dup = mapper.selectCount(new LambdaQueryWrapper<SurveyResponse>()
                    .eq(SurveyResponse::getQuestionnaireId, questionnaireId)
                    .eq(SurveyResponse::getRespondent, me)
                    .eq(SurveyResponse::getDeleted, 0));
            SurveyFlow.require(dup == 0, "10", "您已提交过本问卷，实名问卷不可重复提交");
        }

        List<Map<String, Object>> qDefs = QuestionnaireService.parseQuestions(q.getQuestions());
        // 维度 -> 百分制得分列表
        Map<String, List<Double>> dimVals = new LinkedHashMap<>();
        int idx = 0;
        for (Map<String, Object> def : qDefs) {
            idx++;
            String qid = String.valueOf(def.getOrDefault("qid", "q" + idx));
            String type = String.valueOf(def.get("type"));
            boolean required = Boolean.TRUE.equals(def.get("required"))
                    || Integer.valueOf(1).equals(def.get("required"));
            Object raw = answers.get(qid);
            String val = raw == null ? "" : String.valueOf(raw).trim();
            if (required) {
                SurveyFlow.require(!val.isEmpty(), "11", "第" + idx + "题为必答题，请作答后提交");
            }
            if (val.isEmpty()) {
                continue;
            }
            if ("单选".equals(type)) {
                List<String> opts = stringList(def.get("options"));
                SurveyFlow.require(opts.contains(val), "11", "第" + idx + "题答案不在选项范围内");
            } else if ("多选".equals(type)) {
                List<String> opts = stringList(def.get("options"));
                List<String> gave = Arrays.stream(val.split("[,，]")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                SurveyFlow.require(!gave.isEmpty() && opts.containsAll(gave), "11", "第" + idx + "题答案不在选项范围内");
                answers.put(qid, String.join(",", gave));
            } else if ("量表".equals(type)) {
                int max = def.get("maxScale") == null ? 5 : Integer.parseInt(String.valueOf(def.get("maxScale")));
                int rating;
                try {
                    rating = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    rating = -1;
                }
                SurveyFlow.require(rating >= 1 && rating <= max, "05", "第" + idx + "题量表分值须在1-" + max + "之间");
                String dim = String.valueOf(def.getOrDefault("dimension", ""));
                if (!dim.isBlank() && !"null".equals(dim)) {
                    double score = max == 1 ? 100 : Math.round((rating - 1) * 10000.0 / (max - 1)) / 100.0;
                    dimVals.computeIfAbsent(dim, k -> new ArrayList<>()).add(score);
                }
            }
        }

        Map<String, Object> dimScore = new LinkedHashMap<>();
        dimVals.forEach((d, vals) -> dimScore.put(d,
                Math.round(vals.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 100) / 100.0));

        SurveyResponse r = new SurveyResponse();
        r.setQuestionnaireId(questionnaireId);
        r.setTitle(q.getTitle());
        r.setPeriodTag(periodTag(q));
        r.setRespondent(anonymous ? "匿名用户" : me);
        r.setRespondentDept(anonymous ? (userDept.isBlank() ? "未填部门" : userDept) : (userDept.isBlank() ? "未填部门" : userDept));
        r.setAnonymousFlag(anonymous ? 1 : 0);
        try {
            r.setAnswers(MAPPER.writeValueAsString(answers));
            r.setDimensionScores(MAPPER.writeValueAsString(dimScore));
        } catch (Exception e) {
            r.setAnswers("{}");
            r.setDimensionScores("{}");
        }
        r.setSubmitTime(now);
        mapper.insert(r);
        audit.log("SUBMIT", "cul_survey_response", r.getId(),
                (anonymous ? "匿名" : me) + " 提交问卷《" + q.getTitle() + "》");
        return r;
    }

    private List<String> stringList(Object o) {
        if (o instanceof List<?> list) {
            List<String> r = new ArrayList<>();
            list.forEach(x -> r.add(String.valueOf(x)));
            return r;
        }
        return new ArrayList<>();
    }

    /** 周期标签：季度→yyyyQn，年度→yyyy，专项→专项+yyMMdd。 */
    private String periodTag(SurveyQuestionnaire q) {
        LocalDate d = q.getStartTime() != null ? q.getStartTime().toLocalDate() : LocalDate.now();
        return switch (q.getPeriodType()) {
            case "季度" -> d.getYear() + "Q" + ((d.getMonthValue() - 1) / 3 + 1);
            case "年度" -> String.valueOf(d.getYear());
            default -> "专项-" + String.format("%02d%02d", d.getMonthValue(), d.getDayOfMonth());
        };
    }
}
