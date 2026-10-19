package com.example.partyaffairsmgt.org.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.org.entity.PartyAffairPublicity;
import com.example.partyaffairsmgt.org.entity.PublicityConfig;
import com.example.partyaffairsmgt.org.service.PartyPublicityService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 4.6 党务公开接口（含公开事项默认期限配置）。 */
@RestController
@RequestMapping("/api/party/org/publicity")
@CrossOrigin(origins = "*")
public class PartyAffairPublicityController {

    @Autowired private PartyPublicityService service;

    @GetMapping("/list")
    public ResponseResult<List<PartyAffairPublicity>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(service.list(orgId, category, status, year));
    }

    @PostMapping
    public ResponseResult<PartyAffairPublicity> create(@RequestBody PartyAffairPublicity p) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.save(p));
    }

    @PutMapping
    public ResponseResult<PartyAffairPublicity> update(@RequestBody PartyAffairPublicity p) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.save(p));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        service.delete(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/publish/{id}")
    public ResponseResult<PartyAffairPublicity> publish(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.publish(id));
    }

    @PostMapping("/expire/process")
    public ResponseResult<Map<String, Object>> processExpired() {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.processExpired());
    }

    @GetMapping("/overdue")
    public ResponseResult<List<PartyAffairPublicity>> overdue() {
        return ResponseResult.success(service.overdueUnpublished());
    }

    // ---------- 默认期限配置 ----------

    @GetMapping("/config/list")
    public ResponseResult<List<PublicityConfig>> configs() {
        return ResponseResult.success(service.configs());
    }

    @PostMapping("/config")
    public ResponseResult<PublicityConfig> saveConfig(@RequestBody PublicityConfig c) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.saveConfig(c));
    }

    @PutMapping("/config")
    public ResponseResult<PublicityConfig> updateConfig(@RequestBody PublicityConfig c) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.saveConfig(c));
    }
}
