package com.example.employee.controller;

import com.example.common.dto.ResponseResult;
import com.example.employee.entity.Employee;
import com.example.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工资源，路径与前端 staff-mgt/List.vue 完全一致(legacy /api/hr-archive 前缀)。
 * 注意：更新(PUT)的 id 在 JSON body 内，URL 不带 /{id}；离职即 PUT 根路径并置 employeeStatus=RESIGNED。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/hr-archive/employee")
public class HrArchiveEmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /** 员工列表，可选参数 name(姓名模糊) / department(部门) / status(状态)。前端默认不带参，全量返回。 */
    @GetMapping
    public ResponseResult<List<Employee>> list(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) String department,
                                               @RequestParam(required = false) String status) {
        return ResponseResult.success(employeeService.searchEmployees(name, department, status));
    }

    /** 列表别名（标准 CRUD /list）。 */
    @GetMapping("/list")
    public ResponseResult<List<Employee>> listAlias(@RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String department,
                                                    @RequestParam(required = false) String status) {
        return list(name, department, status);
    }

    /** 员工详情。 */
    @GetMapping("/{id}")
    public ResponseResult<Employee> getById(@PathVariable Long id) {
        return ResponseResult.success(employeeService.getById(id));
    }

    /** 新增员工。 */
    @PostMapping
    public ResponseResult<Employee> create(@RequestBody Employee employee) {
        return ResponseResult.success("员工新增成功", employeeService.createEmployee(employee));
    }

    /** 更新员工；id 在 body 内。离职流程同样调用此接口，仅 employeeStatus 变为 RESIGNED。 */
    @PutMapping
    public ResponseResult<Employee> update(@RequestBody Employee employee) {
        return ResponseResult.success("员工信息已更新", employeeService.updateEmployee(employee));
    }

    /** 更新别名（标准 CRUD：id 在 URL 上）。 */
    @PutMapping("/{id}")
    public ResponseResult<Employee> updateByPath(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        return update(employee);
    }

    /** 删除员工(逻辑删除)。 */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        employeeService.removeById(id);
        return ResponseResult.success();
    }
}
