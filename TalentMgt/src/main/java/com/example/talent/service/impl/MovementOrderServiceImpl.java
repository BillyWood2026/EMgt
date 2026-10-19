package com.example.talent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.talent.entity.MovementOrder;
import com.example.talent.mapper.MovementOrderMapper;
import com.example.talent.service.MovementOrderService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位异动/调动 Service 实现
 */
@Service
public class MovementOrderServiceImpl
        extends ServiceImpl<MovementOrderMapper, MovementOrder>
        implements MovementOrderService {

    @Override
    public List<MovementOrder> search(String keyword, String status, String movementType) {
        LambdaQueryWrapper<MovementOrder> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(MovementOrder::getMovementNo, kw)
                    .or().like(MovementOrder::getEmployeeName, kw)
                    .or().like(MovementOrder::getEmployeeNo, kw));
        }
        if (StringUtils.hasText(status)) {
            qw.eq(MovementOrder::getStatus, status.trim());
        }
        if (StringUtils.hasText(movementType)) {
            qw.eq(MovementOrder::getMovementType, movementType.trim());
        }
        qw.orderByDesc(MovementOrder::getId);
        return this.list(qw);
    }

    @Override
    public MovementOrder submit(Long id) {
        MovementOrder order = mustGet(id);
        order.setStatus("SUBMITTED");
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return this.getById(id);
    }

    @Override
    public MovementOrder approve(Long id) {
        MovementOrder order = mustGet(id);
        order.setStatus("APPROVED");
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return this.getById(id);
    }

    @Override
    public MovementOrder reject(Long id) {
        MovementOrder order = mustGet(id);
        order.setStatus("REJECTED");
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return this.getById(id);
    }

    private MovementOrder mustGet(Long id) {
        MovementOrder order = this.getById(id);
        if (order == null) {
            throw new IllegalArgumentException("异动单不存在: " + id);
        }
        return order;
    }

    /**
     * 调薪额/幅度兜底计算：调薪额 = 新薪资 - 原薪资；幅度 = 调薪额/原薪资×100（两位小数）。
     */
    public static void fillAdjustment(MovementOrder o) {
        if (o.getFromSalary() != null && o.getToSalary() != null) {
            if (o.getAdjustAmount() == null) {
                o.setAdjustAmount(o.getToSalary().subtract(o.getFromSalary()));
            }
            if (o.getAdjustRate() == null && o.getFromSalary().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal rate = o.getAdjustAmount() == null
                        ? o.getToSalary().subtract(o.getFromSalary())
                        : o.getAdjustAmount();
                o.setAdjustRate(rate.multiply(BigDecimal.valueOf(100))
                        .divide(o.getFromSalary(), 2, RoundingMode.HALF_UP));
            }
        }
    }
}
