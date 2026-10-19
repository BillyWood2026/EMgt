package com.example.partyaffairsmgt.assessment.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.assessment.entity.AssessScheme;
import com.example.partyaffairsmgt.assessment.service.AssessSchemeService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party/assessment/scheme")
@CrossOrigin(origins = "*")
public class AssessSchemeController {

    @Autowired
    private AssessSchemeService schemeService;

    @GetMapping("/list")
    public ResponseResult<List<AssessScheme>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keyword) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(schemeService.list(status, year, keyword));
    }

    @PostMapping
    public ResponseResult<AssessScheme> save(@RequestBody AssessScheme s) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(schemeService.save(s));
    }

    @PostMapping("/weight-check")
    public ResponseResult<Map<String, Object>> weightCheck(@RequestBody List<Long> ids) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(schemeService.weightCheck(ids));
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<AssessScheme> publish(@PathVariable Long id,
                                                @RequestBody(required = false) List<Long> indicatorIds,
                                                @RequestParam(required = false, defaultValue = "false") boolean force) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(schemeService.publish(id, indicatorIds, force));
    }

    @PostMapping("/{id}/finish")
    public ResponseResult<AssessScheme> finish(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(schemeService.finish(id));
    }
}
