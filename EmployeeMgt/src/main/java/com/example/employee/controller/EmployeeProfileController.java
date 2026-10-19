package com.example.employee.controller;

import com.example.common.dto.ResponseResult;
import com.example.employee.entity.EmployeeProfile;
import com.example.employee.service.EmployeeProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/employee/profile")
public class EmployeeProfileController {

    @Autowired
    private EmployeeProfileService employeeProfileService;

    @GetMapping
    public ResponseResult<List<EmployeeProfile>> list() {
        return ResponseResult.success(employeeProfileService.list());
    }

    /** 列表别名（标准 CRUD /list）。 */
    @GetMapping("/list")
    public ResponseResult<List<EmployeeProfile>> listAlias() {
        return list();
    }

    @GetMapping("/{id}")
    public ResponseResult<EmployeeProfile> getById(@PathVariable Long id) {
        return ResponseResult.success(employeeProfileService.getById(id));
    }

    @PostMapping
    public ResponseResult<EmployeeProfile> create(@RequestBody EmployeeProfile profile) {
        employeeProfileService.save(profile);
        return ResponseResult.success(profile);
    }

    @PutMapping("/{id}")
    public ResponseResult<EmployeeProfile> update(@PathVariable Long id, @RequestBody EmployeeProfile profile) {
        profile.setId(id);
        employeeProfileService.updateById(profile);
        return ResponseResult.success(profile);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        employeeProfileService.removeById(id);
        return ResponseResult.success();
    }
}
