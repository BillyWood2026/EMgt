package com.example.partyaffairsmgt.news.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.news.entity.NewsColumn;
import com.example.partyaffairsmgt.news.service.NewsColumnService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 栏目管理控制器（需求 7.1）。 */
@RestController
@RequestMapping("/api/party/news/column")
@CrossOrigin(origins = "*")
public class NewsColumnController {

    @Autowired
    private NewsColumnService columnService;

    @GetMapping("/tree")
    public ResponseResult<List<Map<String, Object>>> tree() {
        return ResponseResult.success(columnService.tree());
    }

    @GetMapping("/list")
    public ResponseResult<List<NewsColumn>> list() {
        return ResponseResult.success(columnService.list());
    }

    @PostMapping
    public ResponseResult<NewsColumn> save(@RequestBody NewsColumn column) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R13);
        return ResponseResult.success(columnService.save(column));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        columnService.delete(id);
        return ResponseResult.success(null);
    }
}
