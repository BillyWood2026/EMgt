package com.example.talent.controller;

import com.example.common.dto.ResponseResult;
import com.example.talent.entity.EvaluationStandard;
import com.example.talent.service.EvaluationStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 胜任力标准 / 评估模型 Controller
 * 对齐页面: comp-assess/Model.vue
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/evaluation/standard")
public class EvaluationStandardController {

    @Autowired
    private EvaluationStandardService evaluationStandardService;

    /**
     * 模型/标准列表（数组）。所有筛选参数可选。
     */
    @GetMapping("/search")
    public ResponseResult<List<EvaluationStandard>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String standardName,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String positionFamily,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status) {
        String name = StringUtils.hasText(standardName) ? standardName : modelName;
        String family = StringUtils.hasText(positionFamily) ? positionFamily : position;
        return ResponseResult.success(
                evaluationStandardService.search(keyword, name, family, status));
    }

    /**
     * 新建模型。body 带 id 或已存在的 standardCode 时执行更新（幂等 upsert，
     * 同时兼容前端编辑场景实际发送 POST 的行为）。
     */
    @PostMapping
    public ResponseResult<EvaluationStandard> create(@RequestBody EvaluationStandard body) {
        return ResponseResult.success(evaluationStandardService.saveOrUpdateByBusinessKey(body));
    }

    /**
     * 编辑模型（id 在 body 中；前端亦可能只提交 standardCode 作为定位键）。
     */
    @PutMapping
    public ResponseResult<EvaluationStandard> update(@RequestBody EvaluationStandard body) {
        return ResponseResult.success(evaluationStandardService.saveOrUpdateByBusinessKey(body));
    }

    /**
     * 删除模型。路径键支持数据库自增 id 或模型编号 standardCode。
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable("id") String id) {
        evaluationStandardService.removeByIdOrKey(id);
        return ResponseResult.success();
    }
}
