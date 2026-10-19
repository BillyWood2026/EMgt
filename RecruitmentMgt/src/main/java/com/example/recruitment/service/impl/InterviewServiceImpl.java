package com.example.recruitment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.recruitment.entity.Interview;
import com.example.recruitment.mapper.InterviewMapper;
import com.example.recruitment.service.InterviewService;
import org.springframework.stereotype.Service;

@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {

    /** 权重：价值观30% + 专业40% + 素质20% + 潜力10%。 */
    private static final double W_VALUES = 0.30;
    private static final double W_PROFESSION = 0.40;
    private static final double W_QUALITY = 0.20;
    private static final double W_POTENTIAL = 0.10;

    @Override
    public boolean save(Interview entity) {
        applyComposite(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(Interview entity) {
        applyComposite(entity);
        return super.updateById(entity);
    }

    /**
     * 四维分齐全时计算综合评分并持久化；任一维度缺失则不覆盖（部分字段更新场景）。
     */
    private void applyComposite(Interview e) {
        if (e == null) {
            return;
        }
        if (e.getValuesScore() != null && e.getProfessionScore() != null
                && e.getQualityScore() != null && e.getPotentialScore() != null) {
            long score = Math.round(e.getValuesScore() * W_VALUES
                    + e.getProfessionScore() * W_PROFESSION
                    + e.getQualityScore() * W_QUALITY
                    + e.getPotentialScore() * W_POTENTIAL);
            e.setCompositeScore((int) score);
        }
    }
}
