package com.example.talent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.talent.entity.MovementOrder;

import java.util.List;

public interface MovementOrderService extends IService<MovementOrder> {

    /** 条件查询（关键字/状态/类型） */
    List<MovementOrder> search(String keyword, String status, String movementType);

    /** 提交：DRAFT -> SUBMITTED */
    MovementOrder submit(Long id);

    /** 批准：SUBMITTED -> APPROVED */
    MovementOrder approve(Long id);

    /** 驳回：-> REJECTED */
    MovementOrder reject(Long id);
}
