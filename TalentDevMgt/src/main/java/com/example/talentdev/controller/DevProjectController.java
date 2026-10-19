package com.example.talentdev.controller;

import com.example.common.dto.ResponseResult;
import com.example.talentdev.entity.DevProject;
import com.example.talentdev.service.DevProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dev Project Controller (人才培养项目)
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/talent/dev-project")
public class DevProjectController {

    @Autowired
    private DevProjectService devProjectService;

    @GetMapping({"", "/list"})
    public ResponseResult<List<DevProject>> list() {
        return ResponseResult.success(devProjectService.list());
    }

    @GetMapping("/{id}")
    public ResponseResult<DevProject> getById(@PathVariable Long id) {
        return ResponseResult.success(devProjectService.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody DevProject devProject) {
        devProjectService.save(devProject);
        return ResponseResult.success();
    }

    @PutMapping("/{id}")
    public ResponseResult<Void> update(@PathVariable Long id, @RequestBody DevProject devProject) {
        devProject.setId(id);
        devProjectService.updateById(devProject);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        devProjectService.removeById(id);
        return ResponseResult.success();
    }
}
