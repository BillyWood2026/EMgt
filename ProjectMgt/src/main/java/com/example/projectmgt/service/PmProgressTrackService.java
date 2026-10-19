package com.example.projectmgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.projectmgt.entity.PmProgressTrack;

import java.util.List;

public interface PmProgressTrackService {

    List<PmProgressTrack> list();

    IPage<PmProgressTrack> page(Page<PmProgressTrack> page, String lagStatus, Long projectId);

    PmProgressTrack getById(Long id);

    PmProgressTrack create(PmProgressTrack track);

    PmProgressTrack update(PmProgressTrack track);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);
}
