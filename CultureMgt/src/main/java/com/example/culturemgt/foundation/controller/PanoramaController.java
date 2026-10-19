package com.example.culturemgt.foundation.controller;

import com.example.culturemgt.common.ResponseResult;
import com.example.culturemgt.foundation.service.PanoramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/culture/foundation/panorama")
public class PanoramaController {

    @Autowired private PanoramaService service;

    @GetMapping("/overview")
    public ResponseResult<?> overview() {
        return ResponseResult.success(service.overview());
    }
}
