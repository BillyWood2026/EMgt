package com.example.culturemgt.activity.controller;

import com.example.culturemgt.activity.entity.ActivitySign;
import com.example.culturemgt.activity.service.SignService;
import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.common.cul.CurrentHeader;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/culture/activity-sign")
public class SignController {

    @Autowired private SignService service;

    /** 报名花名册（含签到状态）。 */
    @GetMapping("/roster")
    public ResponseResult<?> roster(@RequestParam Long activityId) {
        return ResponseResult.success(service.roster(activityId));
    }

    /** 我的报名记录。 */
    @GetMapping("/mine")
    public ResponseResult<?> mine() {
        return ResponseResult.success(service.mine(CurrentHeader.operator()));
    }

    /** 员工报名，body 可带 userDept。 */
    @PostMapping("/{activityId}/sign")
    public ResponseResult<?> sign(@PathVariable Long activityId,
                                  @RequestBody(required = false) Map<String, String> body) {
        String dept = body == null ? null : body.get("userDept");
        service.sign(activityId, dept);
        return ResponseResult.success(true);
    }

    @PostMapping("/{activityId}/cancel")
    public ResponseResult<?> cancel(@PathVariable Long activityId) {
        service.cancelSign(activityId);
        return ResponseResult.success(true);
    }

    /** 签到登记：checkStatus=已签到/迟到/请假。 */
    @PostMapping("/{activityId}/check")
    public ResponseResult<?> check(@PathVariable Long activityId,
                                   @RequestParam String userName,
                                   @RequestParam String checkStatus) {
        service.check(activityId, userName, checkStatus);
        return ResponseResult.success(true);
    }
}
