package com.example.employee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 合同续签/终止请求体，对应 Contract.vue 中 renewal / termination 的 POST body。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractActionRequest {

    /** 合同编号 */
    private String contractNumber;

    /** 员工ID(可能为业务编号或员工主键) */
    private String employeeId;

    /** 员工姓名 */
    private String employeeName;
}
