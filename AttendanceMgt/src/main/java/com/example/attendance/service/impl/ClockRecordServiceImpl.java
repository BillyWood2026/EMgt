package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.ClockRecord;
import com.example.attendance.mapper.ClockRecordMapper;
import com.example.attendance.service.ClockRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日常打卡 Service 实现：
 * 保存/更新时依据上/下班打卡时间自动结算工时并识别异常（LATE/EARLY/MISSING）。
 */
@Service
public class ClockRecordServiceImpl extends ServiceImpl<ClockRecordMapper, ClockRecord>
        implements ClockRecordService {

    /** 标准上班 09:00，下班 18:00。 */
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("H:mm");

    @Override
    public boolean save(ClockRecord record) {
        settle(record);
        return super.save(record);
    }

    @Override
    public boolean updateById(ClockRecord record) {
        settle(record);
        return super.updateById(record);
    }

    /**
     * 由 checkIn/checkOut 计算 workHours 与 riskFlag。
     * 任一打卡缺失 -> MISSING；晚于 09:00 -> LATE；早于 18:00 -> EARLY；否则 NORMAL。
     */
    private void settle(ClockRecord r) {
        if (r == null) {
            return;
        }
        String in = r.getCheckIn();
        String out = r.getCheckOut();
        boolean hasIn = StringUtils.hasText(in);
        boolean hasOut = StringUtils.hasText(out);

        if (!hasIn || !hasOut) {
            // 未带任何打卡时间的纯备注/部分字段更新不做覆盖。
            if (hasIn || hasOut) {
                r.setRiskFlag("MISSING");
                r.setRiskDescription("缺卡：上班或下班未打卡");
                r.setWorkHours(null);
            }
            return;
        }

        LocalTime tin = parse(in);
        LocalTime tout = parse(out);
        if (tin == null || tout == null) {
            return;
        }

        long minutes = ChronoUnit.MINUTES.between(tin, tout);
        if (minutes > 0) {
            r.setWorkHours(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
        }

        String risk;
        String desc = null;
        if (tin.isAfter(WORK_START)) {
            risk = "LATE";
            desc = "迟到：上班打卡晚于 09:00";
        } else if (tout.isBefore(WORK_END)) {
            risk = "EARLY";
            desc = "早退：下班打卡早于 18:00";
        } else {
            risk = "NORMAL";
        }
        r.setRiskFlag(risk);
        r.setRiskDescription(desc);
    }

    private LocalTime parse(String hm) {
        try {
            return LocalTime.parse(hm.trim(), HM);
        } catch (Exception e) {
            try {
                return LocalTime.parse(hm.trim());
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
