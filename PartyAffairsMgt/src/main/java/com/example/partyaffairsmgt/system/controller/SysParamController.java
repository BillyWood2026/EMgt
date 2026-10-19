package com.example.partyaffairsmgt.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.CurrentHeader;
import com.example.partyaffairsmgt.common.party.PartyBizException;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.system.entity.SysParam;
import com.example.partyaffairsmgt.system.entity.SysParamHistory;
import com.example.partyaffairsmgt.system.mapper.SysParamHistoryMapper;
import com.example.partyaffairsmgt.system.mapper.SysParamMapper;
import com.example.partyaffairsmgt.system.service.SysAuditWriter;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 参数配置（需求 12.6：管理员修改+版本历史+审计+一键回滚）。
 * E-SY-12 参数不存在；E-SY-13 回滚版本不存在；E-SY-16 必填缺失。
 */
@RestController
@RequestMapping("/api/party/system/param")
@CrossOrigin(origins = "*")
public class SysParamController {

    @Autowired
    private SysParamMapper paramMapper;
    @Autowired
    private SysParamHistoryMapper historyMapper;
    @Autowired
    private SysAuditWriter auditWriter;

    @GetMapping("/list")
    public ResponseResult<List<SysParam>> list(@RequestParam(required = false) String category,
                                               @RequestParam(required = false) String keywordLike) {
        PartyRole.requireAny(PartyRole.READ);
        QueryWrapper<SysParam> qw = new QueryWrapper<>();
        if (category != null && !category.isBlank()) qw.eq("category", category);
        if (keywordLike != null && !keywordLike.isBlank()) {
            qw.and(w -> w.like("param_key", keywordLike).or().like("param_name", keywordLike));
        }
        qw.orderByAsc("category", "param_key");
        return ResponseResult.success(paramMapper.selectList(qw));
    }

    /** 修改参数：写历史(version+1)+版本号递增+审计。 */
    @PutMapping("/{key}")
    public ResponseResult<SysParam> update(@PathVariable String key, @RequestBody SysParam body,
                                           @RequestParam(required = false) String remark) {
        PartyRole.requireAny(PartyRole.R02);
        SysParam old = findByKey(key);
        if (body.getParamValue() == null) {
            throw PartyBizException.block("E-SY-16", "必填项缺失：参数值");
        }
        LocalDateTime now = LocalDateTime.now();
        int newVersion = (old.getVersion() == null ? 1 : old.getVersion()) + 1;
        SysParam u = new SysParam();
        u.setId(old.getId());
        u.setParamValue(body.getParamValue());
        u.setVersion(newVersion);
        u.setUpdatedBy(CurrentHeader.operator());
        u.setUpdateTime(now);
        paramMapper.updateById(u);

        SysParamHistory h = new SysParamHistory();
        h.setParamKey(key);
        h.setVersion(newVersion);
        h.setOldValue(old.getParamValue());
        h.setNewValue(body.getParamValue());
        h.setChangedBy(CurrentHeader.operator());
        h.setChangeTime(now);
        h.setRemark(remark);
        historyMapper.insert(h);

        auditWriter.write("敏感", "参数修改", "sys_param", key, old.getParamValue(), body.getParamValue(),
                remark);
        return ResponseResult.success(paramMapper.selectById(old.getId()));
    }

    @GetMapping("/{key}/history")
    public ResponseResult<List<SysParamHistory>> history(@PathVariable String key) {
        PartyRole.requireAny(PartyRole.READ);
        findByKey(key);
        QueryWrapper<SysParamHistory> qw = new QueryWrapper<>();
        qw.eq("param_key", key).orderByDesc("version");
        return ResponseResult.success(historyMapper.selectList(qw));
    }

    /** 一键回滚到指定历史版本。 */
    @PostMapping("/{key}/rollback")
    public ResponseResult<SysParam> rollback(@PathVariable String key, @RequestParam Integer version) {
        PartyRole.requireAny(PartyRole.R02);
        SysParam old = findByKey(key);
        QueryWrapper<SysParamHistory> qw = new QueryWrapper<>();
        qw.eq("param_key", key).eq("version", version);
        SysParamHistory target = historyMapper.selectOne(qw);
        if (target == null) {
            throw PartyBizException.block("E-SY-13", "回滚版本不存在：" + key + " v" + version);
        }
        LocalDateTime now = LocalDateTime.now();
        int newVersion = (old.getVersion() == null ? 1 : old.getVersion()) + 1;
        SysParam u = new SysParam();
        u.setId(old.getId());
        u.setParamValue(target.getNewValue());
        u.setVersion(newVersion);
        u.setUpdatedBy(CurrentHeader.operator());
        u.setUpdateTime(now);
        paramMapper.updateById(u);

        SysParamHistory h = new SysParamHistory();
        h.setParamKey(key);
        h.setVersion(newVersion);
        h.setOldValue(old.getParamValue());
        h.setNewValue(target.getNewValue());
        h.setChangedBy(CurrentHeader.operator());
        h.setChangeTime(now);
        h.setRemark("回滚自v" + version);
        historyMapper.insert(h);

        auditWriter.write("敏感", "参数回滚", "sys_param", key, old.getParamValue(), target.getNewValue(),
                "回滚自v" + version);
        return ResponseResult.success(paramMapper.selectById(old.getId()));
    }

    private SysParam findByKey(String key) {
        QueryWrapper<SysParam> qw = new QueryWrapper<>();
        qw.eq("param_key", key);
        SysParam p = paramMapper.selectOne(qw);
        if (p == null) {
            throw PartyBizException.block("E-SY-12", "参数不存在：" + key);
        }
        return p;
    }
}
