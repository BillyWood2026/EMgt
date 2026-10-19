package com.example.partyaffairsmgt.membership.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.membership.entity.DuesPublication;
import com.example.partyaffairsmgt.membership.service.DuesPublicationService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 党费收缴情况年度公示（需求 5.3 第9条）。 */
@RestController
@RequestMapping("/api/party/membership/dues-publication")
@CrossOrigin(origins = "*")
public class DuesPublicationController {

    @Autowired
    private DuesPublicationService publicationService;

    @GetMapping("/list")
    public ResponseResult<List<DuesPublication>> list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long orgId) {
        return ResponseResult.success(publicationService.list(year, orgId));
    }

    @PostMapping
    public ResponseResult<DuesPublication> save(@RequestBody DuesPublication body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R12);
        return ResponseResult.success(publicationService.save(body));
    }

    @PostMapping("/publish/{id}")
    public ResponseResult<DuesPublication> publish(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R12);
        return ResponseResult.success(publicationService.publish(id));
    }

    @GetMapping("/todo")
    public ResponseResult<List<Map<String, Object>>> todo(@RequestParam(required = false) Integer year) {
        return ResponseResult.success(publicationService.todo(year));
    }
}
