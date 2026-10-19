package com.example.skillcert.service;

import java.util.List;
import java.util.Map;

public interface LedgerService {

    Map<String, Object> statistics();

    List<Map<String, Object>> myProfile();

    Map<String, Object> employeeProfile(Long skillTypeId);
}
