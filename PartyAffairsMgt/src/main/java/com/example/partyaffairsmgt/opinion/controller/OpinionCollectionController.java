package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionCollection;
import com.example.partyaffairsmgt.opinion.service.OpinionCollectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 舆情采集信息控制器（需求 8.1）。舆情数据仅专员及相关审批角色可见（403 矩阵）。 */
@RestController
@RequestMapping("/api/party/opinion/collection")
@CrossOrigin(origins = "*")
public class OpinionCollectionController {

    @Autowired
    private OpinionCollectService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionCollection>> list(@RequestParam(required = false) String sourcePlatform,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String keyword) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listCollections(sourcePlatform, status, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<OpinionCollection> detail(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.detailCollection(id));
    }

    @PostMapping
    public ResponseResult<OpinionCollection> save(@RequestBody OpinionCollection c) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.saveCollection(c));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        service.deleteCollection(id);
        return ResponseResult.success(null);
    }
}
