package com.example.partyaffairsmgt.assessment.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.assessment.entity.AssessIndicator;
import com.example.partyaffairsmgt.assessment.service.AssessIndicatorService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party/assessment/indicator")
@CrossOrigin(origins = "*")
public class AssessIndicatorController {

    @Autowired
    private AssessIndicatorService indicatorService;

    @GetMapping("/list")
    public ResponseResult<List<AssessIndicator>> list(
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        PartyRole.requireAny(PartyRole.READ);
        return ResponseResult.success(indicatorService.list(level, targetType, status, keyword));
    }

    @PostMapping
    public ResponseResult<AssessIndicator> save(@RequestBody AssessIndicator ind) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(indicatorService.save(ind));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> remove(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        indicatorService.remove(id);
        return ResponseResult.success(null);
    }
}
