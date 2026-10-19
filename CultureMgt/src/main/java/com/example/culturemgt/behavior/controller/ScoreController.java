package com.example.culturemgt.behavior.controller;

import com.example.culturemgt.behavior.service.ScoreService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 个人文化积分台账（账户 + 流水 + 部门榜 + 达标率）。 */
@RestController
@RequestMapping("/api/culture/behavior/score")
public class ScoreController {

    @Autowired private ScoreService service;

    /** 我的积分账户。 */
    @GetMapping("/my")
    public ResponseResult<?> my() {
        return ResponseResult.success(service.myAccount());
    }

    /** 指定用户账户（C99/C03/C02/C07 全员，C04/C05 仅本部门）。 */
    @GetMapping("/{userName}")
    public ResponseResult<?> accountOf(@PathVariable String userName) {
        return ResponseResult.success(service.accountOf(userName));
    }

    /** 部门全员积分列表（文化岗）。 */
    @GetMapping("/dept-list")
    public ResponseResult<?> deptList(@RequestParam(required = false) String dept) {
        return ResponseResult.success(service.deptList(dept));
    }

    /** 积分流水（隐私隔离：本人或文化岗/干部）。 */
    @GetMapping("/txn")
    public ResponseResult<?> txnList(@RequestParam(required = false) String userName,
                                     @RequestParam(required = false) String dimension,
                                     @RequestParam(required = false) String periodTag) {
        return ResponseResult.success(service.txnList(userName, dimension, periodTag));
    }

    /** 排行榜（period=2026Q3 或空=全部，scope=dept|all）。 */
    @GetMapping("/rank")
    public ResponseResult<?> rank(@RequestParam(required = false) String period,
                                  @RequestParam(required = false) String scope) {
        return ResponseResult.success(service.rankList(period, scope));
    }

    /** 部门达标率（算法72）。 */
    @GetMapping("/compliance")
    public ResponseResult<?> compliance(@RequestParam(required = false) String dept,
                                        @RequestParam(required = false) String periodTag) {
        return ResponseResult.success(service.deptCompliance(dept, periodTag));
    }
}
