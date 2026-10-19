package com.example.partyaffairsmgt.org.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.org.entity.TermCandidate;
import com.example.partyaffairsmgt.org.entity.TermElection;
import com.example.partyaffairsmgt.org.entity.TermWitness;
import com.example.partyaffairsmgt.org.service.TermElectionService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 4.2 换届选举接口（方案/候选人/会议计票/结果备案 四步向导）。 */
@RestController
@RequestMapping("/api/party/org/election")
public class TermElectionController {

    @Autowired private TermElectionService service;

    @GetMapping("/list")
    public ResponseResult<List<Map<String, Object>>> list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long orgId) {
        return ResponseResult.success(service.list(year, orgId));
    }

    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> detail(@PathVariable Long id) {
        return ResponseResult.success(service.detail(id));
    }

    @GetMapping("/history")
    public ResponseResult<List<TermElection>> history(@RequestParam(required = false) Long orgId) {
        return ResponseResult.success(service.history(orgId));
    }

    @PostMapping("/plan")
    public ResponseResult<TermElection> createPlan(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.createPlan(body));
    }

    @PutMapping("/plan/{id}")
    public ResponseResult<TermElection> updatePlan(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.updatePlan(id, body));
    }

    @PostMapping("/should-vote/{id}")
    public ResponseResult<Integer> refreshShouldVote(@PathVariable Long id) {
        return ResponseResult.success(service.refreshShouldVote(id));
    }

    @PostMapping("/candidates/{id}")
    public ResponseResult<List<TermCandidate>> saveCandidates(@PathVariable Long id,
                                                              @RequestBody Map<String, List<Map<String, Object>>> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.saveCandidates(id, body.getOrDefault("candidates", List.of())));
    }

    @PostMapping("/witnesses/{id}")
    public ResponseResult<List<TermWitness>> saveWitnesses(@PathVariable Long id,
                                                           @RequestBody Map<String, List<Map<String, Object>>> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.saveWitnesses(id, body.getOrDefault("witnesses", List.of())));
    }

    @PostMapping("/tally/{id}")
    public ResponseResult<Map<String, Object>> tally(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04);
        return ResponseResult.success(service.saveTally(id, body));
    }

    @PostMapping("/file/{id}")
    public ResponseResult<TermElection> file(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02);
        return ResponseResult.success(service.fileResult(id, body));
    }

    @PostMapping("/audit/{id}")
    public ResponseResult<TermElection> audit(@PathVariable Long id,
                                              @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R03);
        boolean pass = Boolean.TRUE.equals(body.get("pass"))
                || Integer.valueOf(1).equals(body.get("pass"));
        return ResponseResult.success(service.audit(id, pass, (String) body.get("opinion")));
    }
}
