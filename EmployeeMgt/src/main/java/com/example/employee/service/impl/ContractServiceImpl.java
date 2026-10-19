package com.example.employee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.employee.dto.ContractActionRequest;
import com.example.employee.entity.Contract;
import com.example.employee.mapper.ContractMapper;
import com.example.employee.service.ContractService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    @Override
    public List<Contract> searchContracts(String contractNumber, String employeeName, String status) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(contractNumber)) {
            wrapper.like(Contract::getContractNumber, contractNumber.trim());
        }
        if (StringUtils.hasText(employeeName)) {
            wrapper.like(Contract::getEmployeeName, employeeName.trim());
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Contract::getContractStatus, status.trim().toUpperCase());
        }
        wrapper.orderByAsc(Contract::getId);
        return list(wrapper);
    }

    @Override
    public Contract createContract(Contract contract) {
        if (!StringUtils.hasText(contract.getContractNumber())) {
            contract.setContractNumber(generateContractNumber());
        }
        if (!StringUtils.hasText(contract.getContractStatus())) {
            contract.setContractStatus("VALID");
        }
        if (contract.getDeleted() == null) {
            contract.setDeleted(0);
        }
        if (contract.getVersion() == null) {
            contract.setVersion(1);
        }
        contract.setCreateTime(LocalDateTime.now());
        contract.setUpdateTime(LocalDateTime.now());
        save(contract);
        return getById(contract.getId());
    }

    @Override
    public Contract updateContract(Contract contract) {
        if (contract.getId() == null) {
            throw new IllegalArgumentException("合同ID(id)不能为空");
        }
        contract.setUpdateTime(LocalDateTime.now());
        updateById(contract);
        return getById(contract.getId());
    }

    @Override
    public Contract renew(ContractActionRequest request) {
        Contract contract = locate(request);
        if (contract == null) {
            return null;
        }
        contract.setContractStatus("VALID");
        // 固定期限合同续签：结束日期顺延一年；无固定期限保持空/原值。
        if (contract.getContractEndDate() != null
                && !"UNFIXED".equalsIgnoreCase(contract.getContractDurationType())) {
            contract.setContractEndDate(contract.getContractEndDate().plusYears(1));
        }
        contract.setUpdateTime(LocalDateTime.now());
        updateById(contract);
        return getById(contract.getId());
    }

    @Override
    public Contract terminate(ContractActionRequest request) {
        Contract contract = locate(request);
        if (contract == null) {
            return null;
        }
        contract.setContractStatus("TERMINATED");
        contract.setUpdateTime(LocalDateTime.now());
        updateById(contract);
        return getById(contract.getId());
    }

    /** 先按合同编号定位，编号缺失/未命中时回退到该员工最新一份合同。 */
    private Contract locate(ContractActionRequest request) {
        if (request == null) {
            return null;
        }
        if (StringUtils.hasText(request.getContractNumber())) {
            Contract byNumber = getOne(new LambdaQueryWrapper<Contract>()
                    .eq(Contract::getContractNumber, request.getContractNumber().trim())
                    .last("LIMIT 1"));
            if (byNumber != null) {
                return byNumber;
            }
        }
        if (StringUtils.hasText(request.getEmployeeId())) {
            return getOne(new LambdaQueryWrapper<Contract>()
                    .eq(Contract::getEmployeeId, request.getEmployeeId().trim())
                    .orderByDesc(Contract::getId)
                    .last("LIMIT 1"));
        }
        return null;
    }

    private String generateContractNumber() {
        long seq = count() + 1;
        String candidate = String.format("HT-%d-%03d", Year.now().getValue(), seq);
        // 极端情况下序号撞号则向后顺延。
        while (exists(new LambdaQueryWrapper<Contract>()
                .eq(Contract::getContractNumber, candidate))) {
            seq++;
            candidate = String.format("HT-%d-%03d", Year.now().getValue(), seq);
        }
        return candidate;
    }
}
