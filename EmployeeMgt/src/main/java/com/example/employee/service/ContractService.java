package com.example.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.employee.dto.ContractActionRequest;
import com.example.employee.entity.Contract;

import java.util.List;

public interface ContractService extends IService<Contract> {

    /** 合同列表查询，支持按合同编号、员工姓名、状态可选过滤。 */
    List<Contract> searchContracts(String contractNumber, String employeeName, String status);

    /** 新增合同：contractNumber/contractStatus 缺省时自动补全。 */
    Contract createContract(Contract contract);

    /** 更新合同：按 body 中的 id 更新，null 字段不参与更新。 */
    Contract updateContract(Contract contract);

    /** 续签：合同置为 VALID；固定期限合同结束日期顺延一年。找不到返回 null。 */
    Contract renew(ContractActionRequest request);

    /** 终止：合同置为 TERMINATED。找不到返回 null。 */
    Contract terminate(ContractActionRequest request);
}
