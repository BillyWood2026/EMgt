package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionKeyword;
import com.example.partyaffairsmgt.opinion.service.OpinionCollectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 关键词库管理控制器（需求 8.1：分组、权重）。 */
@RestController
@RequestMapping("/api/party/opinion/keyword")
@CrossOrigin(origins = "*")
public class OpinionKeywordController {

    @Autowired
    private OpinionCollectService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionKeyword>> list(@RequestParam(required = false) String group) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listKeywords(group));
    }

    @PostMapping
    public ResponseResult<OpinionKeyword> save(@RequestBody OpinionKeyword kw) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.saveKeyword(kw));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        service.deleteKeyword(id);
        return ResponseResult.success(null);
    }
}
