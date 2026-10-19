package com.example.sysmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.ResponseResult;
import com.example.sysmgt.entity.SysDictData;
import com.example.sysmgt.entity.SysDictType;
import com.example.sysmgt.service.SysDictDataService;
import com.example.sysmgt.service.SysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/dict")
public class SysDictController {

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @Autowired
    private SysDictDataService sysDictDataService;

    @GetMapping("/type/page")
    public ResponseResult<Page<SysDictType>> typePage(@RequestParam(defaultValue = "1") int current,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String dictName,
                                                      @RequestParam(required = false) String dictType,
                                                      @RequestParam(required = false) String dictCode,
                                                      @RequestParam(required = false) String status) {
        String key = (dictType == null || dictType.trim().isEmpty()) ? dictCode : dictType;
        Page<SysDictType> page = sysDictTypeService.page(current, size, dictName, key, status);
        page.getRecords().forEach(this::fillFrontendAlias);
        return ResponseResult.success(page);
    }

    @GetMapping("/type/list")
    public ResponseResult<List<SysDictType>> typeList(@RequestParam(required = false) String dictName,
                                                      @RequestParam(required = false) String dictType,
                                                      @RequestParam(required = false) String dictCode,
                                                      @RequestParam(required = false) String status) {
        String key = (dictType == null || dictType.trim().isEmpty()) ? dictCode : dictType;
        List<SysDictType> rows = sysDictTypeService.list(dictName, key, status);
        rows.forEach(this::fillFrontendAlias);
        return ResponseResult.success(rows);
    }

    @GetMapping("/type/{id}")
    public ResponseResult<SysDictType> getTypeById(@PathVariable Long id) {
        SysDictType row = sysDictTypeService.getById(id);
        fillFrontendAlias(row);
        return ResponseResult.success(row);
    }

    /** 前端 Dict.vue 使用 dictCode/description 字段名，与 dict_type/remark 双向桥接 */
    private void fillFrontendAlias(SysDictType row) {
        if (row == null) return;
        if (row.getDictCode() == null) row.setDictCode(row.getDictType());
        if (row.getDescription() == null) row.setDescription(row.getRemark());
    }

    /** 创建/更新前把前端字段映射到实体列字段 */
    private void applyFrontendAlias(SysDictType row) {
        if (row == null) return;
        if ((row.getDictType() == null || row.getDictType().trim().isEmpty()) && row.getDictCode() != null) {
            row.setDictType(row.getDictCode().trim());
        }
        if ((row.getRemark() == null || row.getRemark().trim().isEmpty()) && row.getDescription() != null) {
            row.setRemark(row.getDescription());
        }
    }

    @GetMapping("/type/type/{dictType}")
    public ResponseResult<SysDictType> getTypeByDictType(@PathVariable String dictType) {
        return ResponseResult.success(sysDictTypeService.getByDictType(dictType));
    }

    @PostMapping("/type")
    public ResponseResult<Boolean> createType(@RequestBody SysDictType sysDictType) {
        applyFrontendAlias(sysDictType);
        if (sysDictType.getDictType() == null || sysDictType.getDictType().trim().isEmpty()) {
            return ResponseResult.error("字典编码不能为空");
        }
        if (!sysDictTypeService.checkDictTypeUnique(null, sysDictType.getDictType())) {
            return ResponseResult.error("字典类型已存在");
        }
        return ResponseResult.success(sysDictTypeService.save(sysDictType));
    }

    @PutMapping("/type/{id}")
    public ResponseResult<Boolean> updateType(@PathVariable Long id, @RequestBody SysDictType sysDictType) {
        applyFrontendAlias(sysDictType);
        sysDictType.setId(id);
        if (!sysDictTypeService.checkDictTypeUnique(id, sysDictType.getDictType())) {
            return ResponseResult.error("字典类型已存在");
        }
        return ResponseResult.success(sysDictTypeService.updateById(sysDictType));
    }

    @DeleteMapping("/type/{id}")
    @Transactional
    public ResponseResult<Boolean> deleteType(@PathVariable Long id) {
        SysDictType dictType = sysDictTypeService.getById(id);
        if (dictType != null) {
            sysDictDataService.deleteByDictType(dictType.getDictType());
        }
        return ResponseResult.success(sysDictTypeService.removeById(id));
    }

    @DeleteMapping("/type/batch")
    @Transactional
    public ResponseResult<Boolean> batchDeleteType(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            SysDictType dictType = sysDictTypeService.getById(id);
            if (dictType != null) {
                sysDictDataService.deleteByDictType(dictType.getDictType());
            }
        }
        return ResponseResult.success(sysDictTypeService.removeByIds(ids));
    }

    @GetMapping("/data/page")
    public ResponseResult<Page<SysDictData>> dataPage(@RequestParam(defaultValue = "1") int current,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String dictType,
                                                      @RequestParam(required = false) String dictLabel,
                                                      @RequestParam(required = false) String status) {
        return ResponseResult.success(sysDictDataService.page(current, size, dictType, dictLabel, status));
    }

    @GetMapping("/data/list")
    public ResponseResult<List<SysDictData>> dataList(@RequestParam(required = false) String dictType,
                                                      @RequestParam(required = false) String dictLabel,
                                                      @RequestParam(required = false) String status) {
        return ResponseResult.success(sysDictDataService.list(dictType, dictLabel, status));
    }

    @GetMapping("/data/type/{dictType}")
    public ResponseResult<List<SysDictData>> dataListByType(@PathVariable String dictType) {
        return ResponseResult.success(sysDictDataService.listByDictType(dictType));
    }

    @GetMapping("/data/{id}")
    public ResponseResult<SysDictData> getDataById(@PathVariable Long id) {
        return ResponseResult.success(sysDictDataService.getById(id));
    }

    @PostMapping("/data")
    public ResponseResult<Boolean> createData(@RequestBody SysDictData sysDictData) {
        return ResponseResult.success(sysDictDataService.save(sysDictData));
    }

    @PutMapping("/data/{id}")
    public ResponseResult<Boolean> updateData(@PathVariable Long id, @RequestBody SysDictData sysDictData) {
        sysDictData.setId(id);
        return ResponseResult.success(sysDictDataService.updateById(sysDictData));
    }

    @DeleteMapping("/data/{id}")
    public ResponseResult<Boolean> deleteData(@PathVariable Long id) {
        return ResponseResult.success(sysDictDataService.removeById(id));
    }

    @DeleteMapping("/data/batch")
    public ResponseResult<Boolean> batchDeleteData(@RequestBody List<Long> ids) {
        return ResponseResult.success(sysDictDataService.removeByIds(ids));
    }

    // ---- 前端扁平路径别名（Dict.vue 在用），委托 type/data 逻辑，不改变现有 /type、/data 端点 ----

    @GetMapping("/list")
    public ResponseResult<List<SysDictType>> flatList(@RequestParam(required = false) String dictName,
                                                      @RequestParam(required = false) String dictType,
                                                      @RequestParam(required = false) String dictCode,
                                                      @RequestParam(required = false) String status) {
        String key = (dictType == null || dictType.trim().isEmpty()) ? dictCode : dictType;
        List<SysDictType> rows = sysDictTypeService.list(dictName, key, status);
        rows.forEach(this::fillFrontendAlias);
        return ResponseResult.success(rows);
    }

    @GetMapping("/{id}")
    public ResponseResult<SysDictType> flatGetById(@PathVariable Long id) {
        SysDictType row = sysDictTypeService.getById(id);
        fillFrontendAlias(row);
        return ResponseResult.success(row);
    }

    @PostMapping
    public ResponseResult<Boolean> flatCreate(@RequestBody SysDictType sysDictType) {
        return createType(sysDictType);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> flatUpdate(@PathVariable Long id, @RequestBody SysDictType sysDictType) {
        return updateType(id, sysDictType);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseResult<Boolean> flatDelete(@PathVariable Long id) {
        return deleteType(id);
    }

    @GetMapping("/{dictId}/items")
    public ResponseResult<List<SysDictData>> flatItems(@PathVariable Long dictId) {
        SysDictType dictType = sysDictTypeService.getById(dictId);
        if (dictType == null) {
            return ResponseResult.error("字典类型不存在");
        }
        return ResponseResult.success(sysDictDataService.listByDictType(dictType.getDictType()));
    }

    @PostMapping("/{dictId}/item")
    public ResponseResult<Boolean> flatCreateItem(@PathVariable Long dictId, @RequestBody SysDictData sysDictData) {
        SysDictType dictType = sysDictTypeService.getById(dictId);
        if (dictType == null) {
            return ResponseResult.error("字典类型不存在");
        }
        sysDictData.setId(null);
        sysDictData.setDictType(dictType.getDictType());
        return ResponseResult.success(sysDictDataService.save(sysDictData));
    }

    @PutMapping("/item/{id}")
    public ResponseResult<Boolean> flatUpdateItem(@PathVariable Long id, @RequestBody SysDictData sysDictData) {
        sysDictData.setId(id);
        return ResponseResult.success(sysDictDataService.updateById(sysDictData));
    }

    @DeleteMapping("/item/{id}")
    public ResponseResult<Boolean> flatDeleteItem(@PathVariable Long id) {
        return ResponseResult.success(sysDictDataService.removeById(id));
    }
}
