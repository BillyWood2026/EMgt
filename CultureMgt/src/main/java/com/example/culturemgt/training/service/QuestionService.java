package com.example.culturemgt.training.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.common.cul.AuditService;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.training.entity.Exam;
import com.example.culturemgt.training.entity.Question;
import com.example.culturemgt.training.mapper.ExamMapper;
import com.example.culturemgt.training.mapper.QuestionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 文化题库：单选/多选/判断，支持批量导入（Excel解析后的JSON行）。 */
@Service
public class QuestionService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> TYPES = List.of("单选", "多选", "判断");

    @Autowired private QuestionMapper questionMapper;
    @Autowired private ExamMapper examMapper;
    @Autowired private AuditService audit;

    public List<Question> list(String type, String tag, String keyword) {
        LambdaQueryWrapper<Question> w = new LambdaQueryWrapper<>();
        if (type != null && !type.isBlank()) w.eq(Question::getQuestionType, type);
        if (tag != null && !tag.isBlank()) w.like(Question::getValueTag, tag);
        if (keyword != null && !keyword.isBlank()) w.like(Question::getStem, keyword);
        w.orderByDesc(Question::getId);
        return questionMapper.selectList(w);
    }

    public Question save(Question q) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        validate(q, 0);
        if (q.getId() == null) {
            q.setCreator(CulRole.currentRoles().stream().sorted().findFirst().orElse("C03"));
            q.setCreateTime(LocalDateTime.now());
            questionMapper.insert(q);
            audit.log("NEW", "cul_question", q.getId(), "新增" + q.getQuestionType() + "题：" + brief(q.getStem()));
        } else {
            TrainingFlow.require(questionMapper.selectById(q.getId()) != null, "01", "题目不存在");
            q.setUpdateTime(LocalDateTime.now());
            questionMapper.updateById(q);
            audit.log("EDIT", "cul_question", q.getId(), "调整" + q.getQuestionType() + "题：" + brief(q.getStem()));
        }
        return q;
    }

    /** 批量导入（前端Excel模板解析为JSON行提交）；行级校验，首错即阻断并提示行号。 */
    public Map<String, Object> importBatch(List<Question> rows) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        TrainingFlow.require(rows != null && !rows.isEmpty(), "06", "导入数据为空，请按模板填写");
        List<Question> valid = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Question q = rows.get(i);
            validate(q, i + 1);
            q.setCreator(CulRole.currentRoles().stream().sorted().findFirst().orElse("C03"));
            q.setCreateTime(LocalDateTime.now());
            valid.add(q);
        }
        valid.forEach(questionMapper::insert);
        audit.log("IMPORT", "cul_question", null, "批量导入题库 " + valid.size() + " 题");
        Map<String, Object> r = new HashMap<>();
        r.put("imported", valid.size());
        return r;
    }

    /** 删除题目：被考试任务引用时阻断（E-TR-14）。 */
    public void delete(Long id) {
        CulRole.requireAny(CulRole.CULTURE_STAFF);
        Question q = questionMapper.selectById(id);
        TrainingFlow.require(q != null, "01", "题目不存在");
        int refs = 0;
        for (Exam e : examMapper.selectList(new LambdaQueryWrapper<Exam>().eq(Exam::getDeleted, 0))) {
            if (parseBank(e.getBankIds()).contains(id)) refs++;
        }
        TrainingFlow.require(refs == 0, "14", "题目已被 " + refs + " 场考试引用，禁止删除");
        questionMapper.deleteById(id);
        audit.log("DELETE", "cul_question", id, "删除题目：" + brief(q.getStem()));
    }

    /** 题目校验：类型枚举/题干/选项与答案匹配（E-TR-05），导入时错误带行号（E-TR-06）。 */
    private void validate(Question q, int lineNo) {
        String at = lineNo > 0 ? "第" + lineNo + "行：" : "";
        TrainingFlow.require(q.getQuestionType() != null && TYPES.contains(q.getQuestionType()),
                lineNo > 0 ? "06" : "05", at + "题目类型须为 单选/多选/判断");
        TrainingFlow.require(TrainingFlow.len(q.getStem()) >= 5, lineNo > 0 ? "06" : "05", at + "题干不少于5字");
        List<String> opts = parseOptions(q.getOptionItems());
        if ("判断".equals(q.getQuestionType())) {
            TrainingFlow.require("正确".equals(trim(q.getAnswer())) || "错误".equals(trim(q.getAnswer())),
                    lineNo > 0 ? "06" : "05", at + "判断题答案须为 正确/错误");
            q.setOptionItems("[\"正确\",\"错误\"]");
        } else {
            TrainingFlow.require(opts.size() >= 2, lineNo > 0 ? "06" : "05", at + "选项至少2个");
            List<String> ans = splitAnswer(q.getAnswer());
            TrainingFlow.require(!ans.isEmpty(), lineNo > 0 ? "06" : "05", at + "正确答案必填");
            for (String a : ans) {
                TrainingFlow.require(opts.contains(a), lineNo > 0 ? "06" : "05", at + "正确答案「" + a + "」不在选项内");
            }
            TrainingFlow.require(!"多选".equals(q.getQuestionType()) || ans.size() >= 2,
                    lineNo > 0 ? "06" : "05", at + "多选题答案须至少2项");
            // 多选答案规范化为逗号分隔（按选项顺序）
            if ("多选".equals(q.getQuestionType())) {
                StringBuilder sb = new StringBuilder();
                for (String o : opts) {
                    if (ans.contains(o)) {
                        if (sb.length() > 0) sb.append(',');
                        sb.append(o);
                    }
                }
                q.setAnswer(sb.toString());
            }
        }
        TrainingFlow.require(TrainingFlow.len(q.getValueTag()) > 0, lineNo > 0 ? "06" : "05", at + "关联价值观标签必填");
    }

    public static List<Long> parseBank(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<?> l = MAPPER.readValue(json, List.class);
            List<Long> ids = new ArrayList<>();
            for (Object o : l) ids.add(Long.valueOf(String.valueOf(o)));
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<String> r = new ArrayList<>();
            for (Object o : MAPPER.readValue(json, List.class)) r.add(String.valueOf(o));
            return r;
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> splitAnswer(String answer) {
        if (answer == null || answer.isBlank()) return List.of();
        return Arrays.stream(answer.split("[,，]")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String brief(String stem) {
        return stem == null ? "" : (stem.length() > 24 ? stem.substring(0, 24) + "…" : stem);
    }
}
