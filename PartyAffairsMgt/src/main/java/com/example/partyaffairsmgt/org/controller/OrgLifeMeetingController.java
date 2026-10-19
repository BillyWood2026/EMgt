package com.example.partyaffairsmgt.org.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.org.entity.MeetingAbsence;
import com.example.partyaffairsmgt.org.entity.MeetingMaterial;
import com.example.partyaffairsmgt.org.entity.MeetingRectification;
import com.example.partyaffairsmgt.org.entity.OrgLifeMeeting;
import com.example.partyaffairsmgt.org.service.OrgLifeMeetingService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 4.3 组织生活会与民主生活会接口。 */
@RestController
@RequestMapping("/api/party/org/life-meeting")
@CrossOrigin(origins = "*")
public class OrgLifeMeetingController {

    @Autowired private OrgLifeMeetingService service;

    @GetMapping("/list")
    public ResponseResult<List<OrgLifeMeeting>> list(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String meetingType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status) {
        return ResponseResult.success(service.list(orgId, meetingType, year, status));
    }

    @PostMapping
    public ResponseResult<OrgLifeMeeting> create(@RequestBody OrgLifeMeeting m) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.create(m));
    }

    @PutMapping
    public ResponseResult<OrgLifeMeeting> update(@RequestBody OrgLifeMeeting m) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.update(m));
    }

    @PostMapping("/hold/{id}")
    public ResponseResult<OrgLifeMeeting> markHeld(@PathVariable Long id) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.markHeld(id));
    }

    // ---------- 会前材料 ----------

    @GetMapping("/materials/{meetingId}")
    public ResponseResult<List<MeetingMaterial>> materials(@PathVariable Long meetingId) {
        return ResponseResult.success(service.materials(meetingId));
    }

    @PostMapping("/materials/{meetingId}")
    public ResponseResult<List<MeetingMaterial>> saveMaterials(@PathVariable Long meetingId,
                                                               @RequestBody List<MeetingMaterial> materials) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.saveMaterials(meetingId, materials));
    }

    // ---------- 缺席 ----------

    @GetMapping("/absences/{meetingId}")
    public ResponseResult<List<MeetingAbsence>> absences(@PathVariable Long meetingId) {
        return ResponseResult.success(service.absences(meetingId));
    }

    @PostMapping("/absences/{meetingId}")
    public ResponseResult<List<MeetingAbsence>> saveAbsences(@PathVariable Long meetingId,
                                                             @RequestBody List<MeetingAbsence> absences) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.saveAbsences(meetingId, absences));
    }

    // ---------- 整改台账 ----------

    @GetMapping("/rectifications")
    public ResponseResult<List<MeetingRectification>> rectifications(
            @RequestParam(required = false) Long meetingId) {
        return ResponseResult.success(service.rectifications(meetingId));
    }

    @PostMapping("/rectifications")
    public ResponseResult<MeetingRectification> saveRectification(@RequestBody MeetingRectification r) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.saveRectification(r));
    }

    @PostMapping("/rectifications/close/{id}")
    public ResponseResult<MeetingRectification> closeRectification(@PathVariable Long id,
                                                                   @RequestBody Map<String, String> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R04, PartyRole.R07);
        return ResponseResult.success(service.closeRectification(id,
                body.get("result"), body.get("evidenceUrl")));
    }

    // ---------- 统计 ----------

    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> stats(@RequestParam(required = false) Integer year) {
        return ResponseResult.success(service.stats(year));
    }
}
