package com.example.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.dto.ResponseResult;
import com.example.recruitment.entity.Offer;
import com.example.recruitment.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 录用通知 REST 接口，契约对齐前端 Offer.vue：
 * base /api/recruitment/offer
 */
@RestController
@RequestMapping("/api/recruitment/offer")
@CrossOrigin(origins = "*")
public class OfferController {

    @Autowired
    private OfferService offerService;

    /**
     * 列表查询。可选参数：candidateName（候选人，模糊）、position（岗位，模糊）、
     * status（状态，精确）；page/size 忽略，直接返回数组。
     */
    @GetMapping("/list")
    public ResponseResult<List<Offer>> list(
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Offer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(candidateName), Offer::getCandidateName, candidateName)
                .like(StringUtils.hasText(position), Offer::getPosition, position)
                .eq(StringUtils.hasText(status), Offer::getStatus, status)
                .orderByDesc(Offer::getCreateTime)
                .orderByDesc(Offer::getId);
        return ResponseResult.success(offerService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ResponseResult<Offer> getById(@PathVariable Long id) {
        return ResponseResult.success(offerService.getById(id));
    }

    @PostMapping
    public ResponseResult<Offer> create(@RequestBody Offer entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setId(null);
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("待发送");
        }
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        offerService.save(entity);
        return ResponseResult.success(entity);
    }

    @PutMapping("/{id}")
    public ResponseResult<Boolean> update(@PathVariable Long id, @RequestBody Offer entity) {
        entity.setId(id);
        Offer existing = offerService.getById(id);
        if (existing != null) {
            entity.setVersion(existing.getVersion());
        }
        entity.setUpdateTime(LocalDateTime.now());
        return ResponseResult.success(offerService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        return ResponseResult.success(offerService.removeById(id));
    }
}
