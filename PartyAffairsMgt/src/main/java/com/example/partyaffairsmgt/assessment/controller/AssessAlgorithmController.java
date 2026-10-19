package com.example.partyaffairsmgt.assessment.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.assessment.service.AssessTaskService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评分算法接口（需求 10.4）：定量映射/定性去极值均值/加权汇总/等级判定预览。
 */
@RestController
@RequestMapping("/api/party/assessment/algorithm")
@CrossOrigin(origins = "*")
public class AssessAlgorithmController {

    @Autowired
    private AssessTaskService taskService;

    /** 线性映射：输入完成率%与满分，输出得分。 */
    @GetMapping("/linear-map")
    public ResponseResult<Map<String, Object>> linearMap(
            @RequestParam double ratio,
            @RequestParam double maxScore) {
        PartyRole.requireAny(PartyRole.READ);
        double score = Math.min(ratio, maxScore);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ratio", ratio);
        r.put("maxScore", maxScore);
        r.put("score", BigDecimal.valueOf(score).setScale(2, java.math.RoundingMode.HALF_UP));
        return ResponseResult.success(r);
    }

    /** 定性去极值均值：输入评委分逗号分隔。 */
    @GetMapping("/trim-mean")
    public ResponseResult<Map<String, Object>> trimMean(@RequestParam String values) {
        PartyRole.requireAny(PartyRole.READ);
        BigDecimal mean = AssessTaskService.trimMean(values);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("input", values);
        r.put("mean", mean);
        return ResponseResult.success(r);
    }
}
