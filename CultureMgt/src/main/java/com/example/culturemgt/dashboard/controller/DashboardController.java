package com.example.culturemgt.dashboard.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.common.cul.CulRole;
import com.example.culturemgt.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 子模块10 跨模块文化数据看板（管理层驾驶舱）。
 * K1 集团总看板 /overview：C03/C07/C99；K2 分组织 /org：C02/C03/C07/C99（C02 限本组织）；
 * K3 明细钻取 /drill：type+org 筛选，export=true 需 confirm=true 二次确认并留痕审计。
 */
@RestController
@RequestMapping("/api/culture/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /** K1 集团文化总看板（高管大屏）：覆盖度/活跃度/文化健康/干部履职 + 五类预警卡。 */
    @GetMapping("/overview")
    public ResponseResult<Map<String, Object>> overview() {
        CulRole.requireAny(CulRole.C03, CulRole.C07, CulRole.C99);
        return ResponseResult.success(service.overview(null));
    }

    /** K2 分组织文化看板：按组织筛选钻取本组织全部指标。 */
    @GetMapping("/org")
    public ResponseResult<Map<String, Object>> org(@RequestParam(required = false) String org) {
        CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C07, CulRole.C99);
        return ResponseResult.success(service.overview(service.guardOrg(org)));
    }

    /** K3 明细钻取报表：指标明细 + CSV 导出（E-DB-03 需 confirm=true）。 */
    @GetMapping("/drill")
    public ResponseResult<?> drill(@RequestParam String type,
                                   @RequestParam(required = false) String org,
                                   @RequestParam(defaultValue = "false") boolean export,
                                   @RequestParam(defaultValue = "false") boolean confirm) {
        CulRole.requireAny(CulRole.C02, CulRole.C03, CulRole.C07, CulRole.C99);
        String o = service.guardOrg(org);
        if (export) {
            return ResponseResult.success(service.export(type, o, confirm));
        }
        List<Map<String, Object>> rows = service.drill(type, o);
        return ResponseResult.success(Map.of("type", type, "org", o == null ? "全集团" : o, "rows", rows));
    }
}
