package com.example.hrplanning.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.dto.ResponseResult;
import com.example.hrplanning.entity.TalentInventory;
import com.example.hrplanning.service.TalentInventoryService;
import com.example.hrplanning.util.PageResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 编制盘点 /api/hr-planning/inventory
 * 前端 hr-planning/Index.vue「编制盘点」tab：GET /page 直接读取 {records,total}
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/hr-planning/inventory")
public class TalentInventoryController {

    @Autowired
    private TalentInventoryService talentInventoryService;

    @GetMapping("/page")
    public Map<String, Object> page(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "orgLevel2", required = false) String orgLevel2,
            @RequestParam(value = "headcountStatus", required = false) String headcountStatus) {
        IPage<TalentInventory> page = talentInventoryService.page(pageNum, pageSize, orgLevel2, headcountStatus);
        return PageResponses.of(page);
    }

    @GetMapping("/list")
    public ResponseResult<java.util.List<TalentInventory>> list() {
        return ResponseResult.success(talentInventoryService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<TalentInventory> getById(@PathVariable Long id) {
        return ResponseResult.success(talentInventoryService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody TalentInventory inventory) {
        talentInventoryService.save(inventory);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody TalentInventory inventory) {
        inventory.setId(id);
        talentInventoryService.updateById(inventory);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        talentInventoryService.removeById(id);
        return ResponseResult.success();
    }
}
