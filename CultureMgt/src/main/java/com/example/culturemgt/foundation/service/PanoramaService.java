package com.example.culturemgt.foundation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.culturemgt.foundation.entity.Creed;
import com.example.culturemgt.foundation.entity.Glossary;
import com.example.culturemgt.foundation.entity.Institution;
import com.example.culturemgt.foundation.entity.Milestone;
import com.example.culturemgt.foundation.entity.Redline;
import com.example.culturemgt.foundation.mapper.CreedMapper;
import com.example.culturemgt.foundation.mapper.GlossaryMapper;
import com.example.culturemgt.foundation.mapper.InstitutionMapper;
import com.example.culturemgt.foundation.mapper.MilestoneMapper;
import com.example.culturemgt.foundation.mapper.RedlineMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** 企业文化全景：聚合生效纲领、里程碑、红线禁令、生效制度、释义标签（前台只读）。 */
@Service
public class PanoramaService {

    @Autowired private CreedMapper creedMapper;
    @Autowired private MilestoneMapper milestoneMapper;
    @Autowired private RedlineMapper redlineMapper;
    @Autowired private InstitutionMapper institutionMapper;
    @Autowired private GlossaryMapper glossaryMapper;

    public Map<String, Object> overview() {
        Map<String, Object> data = new LinkedHashMap<>();

        Creed activeCreed = creedMapper.selectOne(new LambdaQueryWrapper<Creed>()
                .eq(Creed::getStatus, "生效")
                .orderByDesc(Creed::getEffectDate).orderByDesc(Creed::getId).last("limit 1"));
        data.put("activeCreed", activeCreed);

        List<Milestone> milestones = milestoneMapper.selectList(new LambdaQueryWrapper<Milestone>()
                .eq(Milestone::getStatus, "启用").eq(Milestone::getShowFront, 1)
                .orderByAsc(Milestone::getOccurDate));
        data.put("milestones", milestones);

        List<Redline> redlines = redlineMapper.selectList(new LambdaQueryWrapper<Redline>()
                .eq(Redline::getStatus, "生效").orderByAsc(Redline::getCode));
        data.put("redlines", redlines);
        Map<String, Long> levelCount = new LinkedHashMap<>();
        for (String lv : List.of("一般", "较重", "严重", "重大")) {
            levelCount.put(lv, redlines.stream().filter(r -> lv.equals(r.getViolationLevel())).count());
        }
        data.put("redlineLevelCount", levelCount);
        data.put("vetoCount", redlines.stream().filter(r -> r.getVetoFlag() != null && r.getVetoFlag() == 1).count());

        List<Institution> institutions = institutionMapper.selectList(new LambdaQueryWrapper<Institution>()
                .eq(Institution::getStatus, "生效").orderByDesc(Institution::getEffectDate));
        data.put("institutions", institutions);

        List<Glossary> glossaries = glossaryMapper.selectList(new LambdaQueryWrapper<Glossary>()
                .eq(Glossary::getStatus, "生效").orderByDesc(Glossary::getId));
        data.put("glossaries", glossaries);
        return data;
    }
}
