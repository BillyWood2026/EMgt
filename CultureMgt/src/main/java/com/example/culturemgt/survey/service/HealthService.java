package com.example.culturemgt.survey.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.survey.entity.SurveyDimension;
import com.example.culturemgt.survey.entity.SurveyQuestionnaire;
import com.example.culturemgt.survey.entity.SurveyRectify;
import com.example.culturemgt.survey.entity.SurveyResponse;
import com.example.culturemgt.survey.mapper.SurveyDimensionMapper;
import com.example.culturemgt.survey.mapper.SurveyQuestionnaireMapper;
import com.example.culturemgt.survey.mapper.SurveyRectifyMapper;
import com.example.culturemgt.survey.mapper.SurveyResponseMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 文化健康度：算法61 氛围综合指数=∑维度均分×权重；部门诊断抽屉/趋势/词云/短板。 */
@Service
public class HealthService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final double SHORT_BOARD_LINE = 70.0;
    private static final Set<String> STOP_WORDS = Set.of(
            "的", "了", "和", "是", "在", "与", "及", "或", "都", "也", "就", "要", "能", "会",
            "建议", "希望", "可以", "我们", "部门", "文化", "集团", "公司", "进行", "加强", "一些",
            "多", "再", "更", "很", "较", "对", "把", "让", "为", "以", "于", "中", "等");

    @Autowired private SurveyResponseMapper responseMapper;
    @Autowired private SurveyQuestionnaireMapper questionnaireMapper;
    @Autowired private SurveyDimensionMapper dimensionMapper;
    @Autowired private SurveyRectifyMapper rectifyMapper;

    /** 健康度仪表盘：指定问卷（默认最新一份有答卷的问卷）各部门氛围指数。 */
    public Map<String, Object> overview(Long questionnaireId) {
        SurveyQuestionnaire q = resolveQuestionnaire(questionnaireId);
        List<SurveyResponse> rs = responsesOf(q.getId());
        Map<String, List<SurveyResponse>> byDept = rs.stream()
                .collect(Collectors.groupingBy(r -> r.getRespondentDept() == null ? "未知部门" : r.getRespondentDept(),
                        LinkedHashMap::new, Collectors.toList()));
        Map<String, Double> weights = enabledWeights();

        List<Map<String, Object>> depts = new ArrayList<>();
        for (Map.Entry<String, List<SurveyResponse>> e : byDept.entrySet()) {
            depts.add(deptCard(e.getKey(), e.getValue(), weights, q));
        }
        depts.sort(Comparator.comparingDouble((Map<String, Object> m) -> ((Number) m.get("index")).doubleValue()));

        // 集团整体
        Map<String, Object> overall = deptCard("集团整体", rs, weights, q);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("questionnaireId", q.getId());
        m.put("questionnaireTitle", q.getTitle());
        m.put("periodTag", rs.isEmpty() ? "" : rs.get(0).getPeriodTag());
        m.put("overall", overall);
        m.put("depts", depts);
        m.put("trend", trend(null));
        return m;
    }

    /** 部门氛围诊断抽屉：维度得分(含环比)、短板、词云、题项分布、历史趋势、整改台账。 */
    public Map<String, Object> orgDetail(String dept, Long questionnaireId) {
        SurveyQuestionnaire cur = resolveQuestionnaire(questionnaireId);
        Map<String, Double> weights = enabledWeights();
        List<SurveyResponse> curRs = responsesOf(cur.getId()).stream()
                .filter(r -> dept.equals(r.getRespondentDept())).collect(Collectors.toList());

        // 上一周期同部门答卷（用于环比）
        List<SurveyResponse> all = responseMapper.selectList(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getRespondentDept, dept).eq(SurveyResponse::getDeleted, 0));
        Map<String, List<SurveyResponse>> byPeriod = all.stream()
                .collect(Collectors.groupingBy(SurveyResponse::getPeriodTag, LinkedHashMap::new, Collectors.toList()));
        Map<String, Double> prevDimAvg = null;
        List<String> periodOrder = new ArrayList<>(byPeriod.keySet());
        String curTag = curRs.isEmpty() ? null : curRs.get(0).getPeriodTag();
        if (curTag != null) {
            int idx = periodOrder.indexOf(curTag);
            if (idx > 0) {
                prevDimAvg = dimAverages(byPeriod.get(periodOrder.get(idx - 1)));
            }
        }
        Map<String, Double> curDimAvg = dimAverages(curRs);

        List<Map<String, Object>> dimRows = new ArrayList<>();
        for (Map.Entry<String, Double> w : weights.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            double score = curDimAvg.getOrDefault(w.getKey(), 0.0);
            Double prev = prevDimAvg == null ? null : prevDimAvg.get(w.getKey());
            row.put("dimension", w.getKey());
            row.put("weight", w.getValue());
            row.put("score", score);
            row.put("prevScore", prev);
            row.put("delta", prev == null ? null : Math.round((score - prev) * 100) / 100.0);
            row.put("shortBoard", curDimAvg.containsKey(w.getKey()) && score < SHORT_BOARD_LINE);
            dimRows.add(row);
        }
        dimRows.sort(Comparator.comparingDouble(r -> ((Number) r.get("score")).doubleValue()));

        List<String> shortTags = dimRows.stream()
                .filter(r -> Boolean.TRUE.equals(r.get("shortBoard")))
                .map(r -> (String) r.get("dimension")).collect(Collectors.toList());

        Map<String, Object> card = deptCard(dept, curRs, weights, cur);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dept", dept);
        m.put("questionnaireId", cur.getId());
        m.put("questionnaireTitle", cur.getTitle());
        m.put("index", card.get("index"));
        m.put("grade", card.get("grade"));
        m.put("riskLevel", card.get("riskLevel"));
        m.put("headcount", card.get("headcount"));
        m.put("dimensions", dimRows);
        m.put("shortBoardTags", shortTags);
        m.put("wordCloud", wordCloud(curRs));
        m.put("optionStats", optionStats(cur, curRs));
        m.put("trend", trend(dept));
        List<SurveyRectify> rects = rectifyMapper.selectList(new LambdaQueryWrapper<SurveyRectify>()
                .eq(SurveyRectify::getDept, dept).eq(SurveyRectify::getDeleted, 0)
                .orderByDesc(SurveyRectify::getId));
        m.put("rectifies", rects);
        return m;
    }

    /** 全集团（dept=null）或指定部门的多周期氛围指数趋势。 */
    public List<Map<String, Object>> trend(String dept) {
        List<SurveyResponse> rs = responseMapper.selectList(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getDeleted, 0)
                .orderByAsc(SurveyResponse::getSubmitTime));
        if (dept != null) {
            rs = rs.stream().filter(r -> dept.equals(r.getRespondentDept())).collect(Collectors.toList());
        }
        Map<String, List<SurveyResponse>> byPeriod = rs.stream()
                .collect(Collectors.groupingBy(SurveyResponse::getPeriodTag, LinkedHashMap::new, Collectors.toList()));
        Map<String, Double> weights = enabledWeights();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, List<SurveyResponse>> e : byPeriod.entrySet()) {
            Map<String, Double> avg = dimAverages(e.getValue());
            double idx = weightedIndex(avg, weights);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("period", e.getKey());
            row.put("index", idx);
            row.put("grade", SurveyFlow.grade(idx));
            row.put("headcount", e.getValue().size());
            out.add(row);
        }
        return out;
    }

    // ---------- 内部 ----------

    private SurveyQuestionnaire resolveQuestionnaire(Long id) {
        if (id != null) {
            SurveyQuestionnaire q = questionnaireMapper.selectById(id);
            SurveyFlow.require(q != null, "01", "问卷不存在");
            return q;
        }
        // 默认取答卷数最多的问卷（主调研），并列时取 id 最大
        List<SurveyResponse> all = responseMapper.selectList(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getDeleted, 0));
        if (!all.isEmpty()) {
            Map<Long, Long> cnt = all.stream()
                    .collect(Collectors.groupingBy(SurveyResponse::getQuestionnaireId, Collectors.counting()));
            Long qid = cnt.entrySet().stream()
                    .max(Map.Entry.<Long, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                    .map(Map.Entry::getKey).orElse(null);
            if (qid != null) {
                return questionnaireMapper.selectById(qid);
            }
        }
        SurveyQuestionnaire q = questionnaireMapper.selectOne(new LambdaQueryWrapper<SurveyQuestionnaire>()
                .eq(SurveyQuestionnaire::getStatus, "已发布").eq(SurveyQuestionnaire::getDeleted, 0)
                .orderByDesc(SurveyQuestionnaire::getId).last("limit 1"));
        SurveyFlow.require(q != null, "01", "暂无调研数据");
        return q;
    }

    private List<SurveyResponse> responsesOf(Long questionnaireId) {
        return responseMapper.selectList(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getQuestionnaireId, questionnaireId)
                .eq(SurveyResponse::getDeleted, 0));
    }

    private Map<String, Double> enabledWeights() {
        List<SurveyDimension> ds = dimensionMapper.selectList(new LambdaQueryWrapper<SurveyDimension>()
                .eq(SurveyDimension::getStatus, "启用").eq(SurveyDimension::getDeleted, 0)
                .orderByAsc(SurveyDimension::getId));
        Map<String, Double> w = new LinkedHashMap<>();
        ds.forEach(d -> {
            // 权重0的启用维度不参与计分
            if (d.getWeight().doubleValue() > 0) {
                w.put(d.getDimensionName(), d.getWeight().doubleValue());
            }
        });
        return w;
    }

    /** 一批答卷的各维度平均百分制得分。 */
    private Map<String, Double> dimAverages(List<SurveyResponse> rs) {
        Map<String, List<Double>> collect = new LinkedHashMap<>();
        for (SurveyResponse r : rs) {
            Map<String, Object> sc = parseDimScores(r.getDimensionScores());
            sc.forEach((k, v) -> {
                double d = v instanceof Number n ? n.doubleValue() : Double.parseDouble(String.valueOf(v));
                collect.computeIfAbsent(k, x -> new ArrayList<>()).add(d);
            });
        }
        Map<String, Double> avg = new LinkedHashMap<>();
        collect.forEach((k, vs) -> avg.put(k,
                Math.round(vs.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 100) / 100.0));
        return avg;
    }

    private double weightedIndex(Map<String, Double> dimAvg, Map<String, Double> weights) {
        // 分母仅计有作答数据的维度权重（问卷未覆盖的维度不按0分拉低指数）
        double sumW = weights.entrySet().stream()
                .filter(e -> dimAvg.containsKey(e.getKey()))
                .mapToDouble(Map.Entry::getValue).sum();
        if (sumW <= 0) {
            return 0;
        }
        double acc = 0;
        for (Map.Entry<String, Double> w : weights.entrySet()) {
            if (dimAvg.containsKey(w.getKey())) {
                acc += dimAvg.get(w.getKey()) * w.getValue();
            }
        }
        return Math.round(acc / sumW * 100) / 100.0;
    }

    private Map<String, Object> deptCard(String dept, List<SurveyResponse> rs,
                                         Map<String, Double> weights, SurveyQuestionnaire q) {
        Map<String, Double> avg = dimAverages(rs);
        double idx = weightedIndex(avg, weights);
        List<String> shortTags = new ArrayList<>();
        weights.forEach((dim, w) -> {
            double s = avg.getOrDefault(dim, 0.0);
            // 该维度有答卷数据（含最低0分）且低于短板线即标记
            if (avg.containsKey(dim) && s < SHORT_BOARD_LINE) {
                shortTags.add(dim);
            }
        });
        int target = q.getTargetCount() == null ? 0 : q.getTargetCount();
        double rate = target > 0 ? Math.round(rs.size() * 10000.0 / target) / 100.0 : 0;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dept", dept);
        m.put("headcount", rs.size());
        m.put("index", idx);
        m.put("grade", SurveyFlow.grade(idx));
        m.put("riskLevel", SurveyFlow.riskLevel(SurveyFlow.grade(idx)));
        m.put("dimScores", avg);
        m.put("shortBoardTags", shortTags);
        m.put("participationRate", Math.min(100, rate));
        return m;
    }

    /** 开放文本题词云：非汉字切分后的2-6字词频（去停用词），top 40。 */
    private List<Map<String, Object>> wordCloud(List<SurveyResponse> rs) {
        Map<String, Integer> freq = new HashMap<>();
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5A-Za-z0-9]{2,6}");
        for (SurveyResponse r : rs) {
            Map<String, Object> ans = parseAnswers(r.getAnswers());
            for (Object v : ans.values()) {
                if (v == null) {
                    continue;
                }
                String text = String.valueOf(v);
                // 长中文串做 2-gram 抽取，捕捉"客户/整改/氛围"等业务短词
                Matcher m = p.matcher(text);
                while (m.find()) {
                    String word = m.group();
                    if (!STOP_WORDS.contains(word) && word.length() >= 2) {
                        freq.merge(word, 1, Integer::sum);
                    }
                }
            }
        }
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(40)
                .map(e -> {
                    Map<String, Object> x = new LinkedHashMap<>();
                    x.put("word", e.getKey());
                    x.put("count", e.getValue());
                    return x;
                }).collect(Collectors.toList());
    }

    /** 单选/多选题选项分布。 */
    private List<Map<String, Object>> optionStats(SurveyQuestionnaire q, List<SurveyResponse> rs) {
        List<Map<String, Object>> qDefs;
        try {
            qDefs = MAPPER.readValue(q.getQuestions(), new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
        List<SurveyResponse> choiceRs = rs;
        List<Map<String, Object>> out = new ArrayList<>();
        int idx = 0;
        for (Map<String, Object> def : qDefs) {
            idx++;
            String type = String.valueOf(def.get("type"));
            if (!"单选".equals(type) && !"多选".equals(type)) {
                continue;
            }
            String qid = String.valueOf(def.getOrDefault("qid", "q" + idx));
            Map<String, Integer> counts = new LinkedHashMap<>();
            ((List<?>) def.getOrDefault("options", new ArrayList<>()))
                    .forEach(o -> counts.put(String.valueOf(o), 0));
            for (SurveyResponse r : choiceRs) {
                Map<String, Object> ans = parseAnswers(r.getAnswers());
                Object v = ans.get(qid);
                if (v == null) {
                    continue;
                }
                for (String pick : String.valueOf(v).split("[,，]")) {
                    String t = pick.trim();
                    if (!t.isEmpty()) {
                        counts.merge(t, 1, Integer::sum);
                    }
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("qid", qid);
            row.put("stem", def.get("stem"));
            row.put("type", type);
            row.put("counts", counts);
            out.add(row);
        }
        return out;
    }

    private Map<String, Object> parseDimScores(String json) {
        try {
            return MAPPER.readValue(json == null || json.isBlank() ? "{}" : json,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> parseAnswers(String json) {
        try {
            return MAPPER.readValue(json == null || json.isBlank() ? "{}" : json,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }
}
