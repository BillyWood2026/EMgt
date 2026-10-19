package com.example.improvemgt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.improvemgt.entity.ImprovementProposal;

import java.util.List;

public interface ImprovementProposalService {

    List<ImprovementProposal> list();

    IPage<ImprovementProposal> page(Page<ImprovementProposal> page, String status, String type, String department);

    ImprovementProposal getById(Long id);

    ImprovementProposal create(ImprovementProposal proposal);

    ImprovementProposal update(ImprovementProposal proposal);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    /** 评审：pending -> approved/rejected（批准立项） */
    ImprovementProposal review(Long id, boolean approved, String comment);

    /** 已批准提案进入实施中 */
    ImprovementProposal markImplementing(Long id);

    /** 实施完成 */
    ImprovementProposal markCompleted(Long id);
}
