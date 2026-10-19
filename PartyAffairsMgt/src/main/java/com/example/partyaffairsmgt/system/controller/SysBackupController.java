package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysBackupDrill;
import com.example.partyaffairsmgt.system.entity.SysBackupRecord;
import com.example.partyaffairsmgt.system.mapper.SysBackupDrillMapper;
import com.example.partyaffairsmgt.system.mapper.SysBackupRecordMapper;
import com.example.partyaffairsmgt.system.service.SysAuditWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据备份与恢复演练（需求 12.7：全量/增量备份加密存储，RPO<=15分钟 RTO<=120分钟）。
 * E-SY-15 演练 RPO/RTO 不达标(预警，仍保存)；E-SY-16 必填缺失。
 */
@RestController
@RequestMapping("/api/party/system/backup")
@CrossOrigin(origins = "*")
public class SysBackupController {

    private static final int RPO_LIMIT = 15;
    private static final int RTO_LIMIT = 120;

    @Autowired
    private SysBackupRecordMapper recordMapper;
    @Autowired
    private SysBackupDrillMapper drillMapper;
    @Autowired
    private SysAuditWriter auditWriter;

    @GetMapping("/list")
    public ResponseResult<Page<SysBackupRecord>> list(@RequestParam(required = false) String backupType,
                                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysBackupRecord> qw = new QueryWrapper<>();
        if (backupType != null && !backupType.isBlank()) qw.eq("backup_type", backupType);
        qw.orderByDesc("id");
        return ResponseResult.success(recordMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    @GetMapping("/drill/list")
    public ResponseResult<Page<SysBackupDrill>> drillList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysBackupDrill> qw = new QueryWrapper<>();
        qw.orderByDesc("id");
        return ResponseResult.success(drillMapper.selectPage(new Page<>(pageNum, pageSize), qw));
    }

    /** 手动触发备份（模拟生成备份记录，文件加密存储）。 */
    @PostMapping("/trigger")
    public ResponseResult<SysBackupRecord> trigger(@RequestParam(defaultValue = "增量") String type) {
        PartyRole.requireAny(PartyRole.R02);
        if (!"增量".equals(type) && !"全量".equals(type)) {
            throw PartyBizException.block("E-SY-16", "备份类型仅支持：增量/全量");
        }
        LocalDateTime start = LocalDateTime.now();
        boolean full = "全量".equals(type);
        SysBackupRecord r = new SysBackupRecord();
        r.setBackupType(type);
        r.setFileName("party_affairs_mgt_" + (full ? "full" : "incr") + "_"
                + start.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + ".sql.enc");
        r.setFileSize(full ? ThreadLocalRandom.current().nextLong(45L, 56L) * 1024 * 1024
                : ThreadLocalRandom.current().nextLong(2L, 5L) * 1024 * 1024);
        r.setLocation(full ? "异地容灾库(加密)" : "本地备份盘(加密)");
        r.setStatus("成功");
        r.setOperator(CurrentHeader.operator());
        r.setStartTime(start);
        r.setEndTime(start.plusSeconds(ThreadLocalRandom.current().nextInt(30, 300)));
        r.setCreateTime(LocalDateTime.now());
        recordMapper.insert(r);
        auditWriter.write("操作", "触发备份", "sys_backup_record", r.getId(), null,
                type + "/" + r.getFileName(), null);
        return ResponseResult.success(r);
    }

    /** 恢复演练登记：RPO>15分钟或RTO>120分钟判定不达标并预警(E-SY-15)，仍保存。 */
    @PostMapping("/drill")
    public ResponseResult<Map<String, Object>> drill(@RequestBody SysBackupDrill drill) {
        PartyRole.requireAny(PartyRole.R02);
        if (drill.getRpoMinutes() == null || drill.getRtoMinutes() == null) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：实际RPO/RTO分钟数");
        }
        boolean pass = drill.getRpoMinutes() <= RPO_LIMIT && drill.getRtoMinutes() <= RTO_LIMIT;
        drill.setId(null);
        drill.setResult(pass ? "达标" : "不达标");
        drill.setOperator(CurrentHeader.operator());
        drill.setCreateTime(LocalDateTime.now());
        drillMapper.insert(drill);
        auditWriter.write("操作", "恢复演练", "sys_backup_drill", drill.getId(), null,
                "RPO=" + drill.getRpoMinutes() + ",RTO=" + drill.getRtoMinutes() + "," + drill.getResult(), null);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("drill", drill);
        if (!pass) {
            result.put("warn", "[E-SY-15][预警] 恢复演练不达标：要求RPO<=" + RPO_LIMIT + "分钟、RTO<="
                    + RTO_LIMIT + "分钟，实际RPO=" + drill.getRpoMinutes() + "、RTO=" + drill.getRtoMinutes());
        }
        return ResponseResult.success(result);
    }
}
