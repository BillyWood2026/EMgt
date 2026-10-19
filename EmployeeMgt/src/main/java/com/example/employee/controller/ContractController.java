package com.example.employee.controller;

import com.example.common.dto.ResponseResult;
import com.example.employee.dto.ContractActionRequest;
import com.example.employee.entity.Contract;
import com.example.employee.service.ContractService;
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
 * 合同资源，路径与前端 staff-mgt/Contract.vue 完全一致(legacy /api/hr-archive 前缀)。
 * 更新(PUT)的 id 在 JSON body 内。
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/hr-archive/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    /** 合同列表，可选参数 contractNumber / employeeName / status。前端默认不带参，全量返回。 */
    @GetMapping
    public ResponseResult<List<Contract>> list(@RequestParam(required = false) String contractNumber,
                                               @RequestParam(required = false) String employeeName,
                                               @RequestParam(required = false) String status) {
        return ResponseResult.success(contractService.searchContracts(contractNumber, employeeName, status));
    }

    /** 列表别名（标准 CRUD /list）。 */
    @GetMapping("/list")
    public ResponseResult<List<Contract>> listAlias(@RequestParam(required = false) String contractNumber,
                                                    @RequestParam(required = false) String employeeName,
                                                    @RequestParam(required = false) String status) {
        return list(contractNumber, employeeName, status);
    }

    /** 合同详情。 */
    @GetMapping("/{id}")
    public ResponseResult<Contract> getById(@PathVariable Long id) {
        return ResponseResult.success(contractService.getById(id));
    }

    /** 新增合同。 */
    @PostMapping
    public ResponseResult<Contract> create(@RequestBody Contract contract) {
        return ResponseResult.success("合同已保存", contractService.createContract(contract));
    }

    /** 更新合同；id 在 body 内。 */
    @PutMapping
    public ResponseResult<Contract> update(@RequestBody Contract contract) {
        return ResponseResult.success("合同已更新", contractService.updateContract(contract));
    }

    /** 更新别名（标准 CRUD：id 在 URL 上）。 */
    @PutMapping("/{id}")
    public ResponseResult<Contract> updateByPath(@PathVariable Long id, @RequestBody Contract contract) {
        contract.setId(id);
        return update(contract);
    }

    /** 删除合同(逻辑删除)。 */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        contractService.removeById(id);
        return ResponseResult.success();
    }

    /** 合同续签：状态置为 VALID(固定期限结束日期顺延一年)。 */
    @PostMapping("/renewal")
    public ResponseResult<Contract> renewal(@RequestBody ContractActionRequest request) {
        Contract contract = contractService.renew(request);
        if (contract == null) {
            return ResponseResult.error(404, "合同不存在，续签失败");
        }
        return ResponseResult.success("合同续签成功", contract);
    }

    /** 合同终止：状态置为 TERMINATED。 */
    @PostMapping("/termination")
    public ResponseResult<Contract> termination(@RequestBody ContractActionRequest request) {
        Contract contract = contractService.terminate(request);
        if (contract == null) {
            return ResponseResult.error(404, "合同不存在，终止失败");
        }
        return ResponseResult.success("合同已终止", contract);
    }
}
