package com.example.recruitment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.recruitment.entity.Offer;
import com.example.recruitment.mapper.OfferMapper;
import com.example.recruitment.service.OfferService;
import org.springframework.stereotype.Service;

@Service
public class OfferServiceImpl extends ServiceImpl<OfferMapper, Offer> implements OfferService {
}
