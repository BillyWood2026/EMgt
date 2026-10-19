package com.example.partyaffairsmgt.opinion.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.opinion.entity.OpinionSource;
import com.example.partyaffairsmgt.opinion.service.OpinionCollectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 信源配置控制器（需求 8.1）。 */
@RestController
@RequestMapping("/api/party/opinion/source")
@CrossOrigin(origins = "*")
public class OpinionSourceController {

    @Autowired
    private OpinionCollectService service;

    @GetMapping("/list")
    public ResponseResult<List<OpinionSource>> list(@RequestParam(required = false) String type) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R03, PartyRole.R04, PartyRole.R14);
        return ResponseResult.success(service.listSources(type));
    }

    @PostMapping
    public ResponseResult<OpinionSource> save(@RequestBody OpinionSource src) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        return ResponseResult.success(service.saveSource(src));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R14);
        service.deleteSource(id);
        return ResponseResult.success(null);
    }
}
