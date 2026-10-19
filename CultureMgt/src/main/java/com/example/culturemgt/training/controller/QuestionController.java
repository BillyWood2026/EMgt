package com.example.culturemgt.training.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.training.entity.Question;
import com.example.culturemgt.training.service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 文化题库管理（后台），含批量导入。 */
@RestController
@RequestMapping("/api/culture/question")
public class QuestionController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired private QuestionService service;

    @GetMapping("/list")
    public ResponseResult<?> list(@RequestParam(required = false) String type,
                                  @RequestParam(required = false) String tag,
                                  @RequestParam(required = false) String keyword) {
        return ResponseResult.success(service.list(type, tag, keyword));
    }

    @PostMapping
    public ResponseResult<?> create(@RequestBody Question q) {
        q.setId(null);
        return ResponseResult.success(service.save(q));
    }

    @PutMapping("/{id}")
    public ResponseResult<?> update(@PathVariable Long id, @RequestBody Question q) {
        q.setId(id);
        return ResponseResult.success(service.save(q));
    }

    /** 批量导入题库（Excel模板解析为JSON行），body {rows:[...]}。 */
    @PostMapping("/import-batch")
    public ResponseResult<?> importBatch(@RequestBody Map<String, Object> body) {
        List<Question> rows = null;
        if (body != null && body.get("rows") != null) {
            rows = MAPPER.convertValue(body.get("rows"),
                    MAPPER.getTypeFactory().constructCollectionType(List.class, Question.class));
        }
        return ResponseResult.success(service.importBatch(rows));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseResult.success("已删除");
    }
}
