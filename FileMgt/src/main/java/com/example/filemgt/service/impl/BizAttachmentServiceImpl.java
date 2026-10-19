package com.example.filemgt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.filemgt.entity.BizAttachment;
import com.example.filemgt.mapper.BizAttachmentMapper;
import com.example.filemgt.service.BizAttachmentService;
import org.springframework.stereotype.Service;

@Service
public class BizAttachmentServiceImpl extends ServiceImpl<BizAttachmentMapper, BizAttachment> implements BizAttachmentService {
}
