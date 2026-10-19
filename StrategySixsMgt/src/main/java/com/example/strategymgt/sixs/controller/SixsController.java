package com.example.strategymgt.sixs.controller;

import com.example.strategymgt.common.ResponseResult;
import com.example.strategymgt.sixs.entity.*;
import com.example.strategymgt.sixs.service.SixsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/strategy/sixs")
public class SixsController {

    private final SixsService sixsService;

    public SixsController(SixsService sixsService) {
        this.sixsService = sixsService;
    }

    @GetMapping("/s1")
    public ResponseResult<List<SixsS1Plan>> s1() { return ResponseResult.ok(sixsService.listS1()); }
    @PostMapping("/s1")
    public ResponseResult<SixsS1Plan> saveS1(@RequestBody SixsS1Plan s) { return ResponseResult.ok(sixsService.saveS1(s)); }
    @PutMapping("/s1/{id}")
    public ResponseResult<Boolean> updateS1(@PathVariable Long id, @RequestBody SixsS1Plan s) {
        s.setId(id);
        return ResponseResult.ok(sixsService.updateS1(s));
    }
    @DeleteMapping("/s1/{id}")
    public ResponseResult<Boolean> deleteS1(@PathVariable Long id) { return ResponseResult.ok(sixsService.deleteS1(id)); }

    @GetMapping("/s2")
    public ResponseResult<List<SixsS2Business>> s2() { return ResponseResult.ok(sixsService.listS2()); }
    @PostMapping("/s2")
    public ResponseResult<SixsS2Business> saveS2(@RequestBody SixsS2Business s) { return ResponseResult.ok(sixsService.saveS2(s)); }
    @PutMapping("/s2/{id}")
    public ResponseResult<Boolean> updateS2(@PathVariable Long id, @RequestBody SixsS2Business s) {
        s.setId(id);
        return ResponseResult.ok(sixsService.updateS2(s));
    }
    @DeleteMapping("/s2/{id}")
    public ResponseResult<Boolean> deleteS2(@PathVariable Long id) { return ResponseResult.ok(sixsService.deleteS2(id)); }

    @GetMapping("/s3")
    public ResponseResult<List<SixsS3Report>> s3() { return ResponseResult.ok(sixsService.listS3()); }
    @PostMapping("/s3")
    public ResponseResult<SixsS3Report> saveS3(@RequestBody SixsS3Report s) { return ResponseResult.ok(sixsService.saveS3(s)); }
    @PutMapping("/s3/{id}")
    public ResponseResult<Boolean> updateS3(@PathVariable Long id, @RequestBody SixsS3Report s) {
        s.setId(id);
        return ResponseResult.ok(sixsService.updateS3(s));
    }
    @DeleteMapping("/s3/{id}")
    public ResponseResult<Boolean> deleteS3(@PathVariable Long id) { return ResponseResult.ok(sixsService.deleteS3(id)); }

    @GetMapping("/s4")
    public ResponseResult<List<SixsS4Audit>> s4() { return ResponseResult.ok(sixsService.listS4()); }
    @PostMapping("/s4")
    public ResponseResult<SixsS4Audit> saveS4(@RequestBody SixsS4Audit s) { return ResponseResult.ok(sixsService.saveS4(s)); }
    @PutMapping("/s4/{id}")
    public ResponseResult<Boolean> updateS4(@PathVariable Long id, @RequestBody SixsS4Audit s) {
        s.setId(id);
        return ResponseResult.ok(sixsService.updateS4(s));
    }
    @DeleteMapping("/s4/{id}")
    public ResponseResult<Boolean> deleteS4(@PathVariable Long id) { return ResponseResult.ok(sixsService.deleteS4(id)); }

    @GetMapping("/s5")
    public ResponseResult<List<SixsS5Performance>> s5() { return ResponseResult.ok(sixsService.listS5()); }
    @PostMapping("/s5")
    public ResponseResult<SixsS5Performance> saveS5(@RequestBody SixsS5Performance s) { return ResponseResult.ok(sixsService.saveS5(s)); }
    @PutMapping("/s5/{id}")
    public ResponseResult<Boolean> updateS5(@PathVariable Long id, @RequestBody SixsS5Performance s) {
        s.setId(id);
        return ResponseResult.ok(sixsService.updateS5(s));
    }
    @DeleteMapping("/s5/{id}")
    public ResponseResult<Boolean> deleteS5(@PathVariable Long id) { return ResponseResult.ok(sixsService.deleteS5(id)); }
    @GetMapping("/s5/total-score")
    public ResponseResult<BigDecimal> s5TotalScore(@RequestParam String evalCode) {
        return ResponseResult.ok(sixsService.calcTotalPerformanceScore(evalCode));
    }

    @GetMapping("/s6")
    public ResponseResult<List<SixsS6Manager>> s6() { return ResponseResult.ok(sixsService.listS6()); }
    @PostMapping("/s6")
    public ResponseResult<SixsS6Manager> saveS6(@RequestBody SixsS6Manager s) { return ResponseResult.ok(sixsService.saveS6(s)); }
    @PutMapping("/s6/{id}")
    public ResponseResult<Boolean> updateS6(@PathVariable Long id, @RequestBody SixsS6Manager s) {
        s.setId(id);
        return ResponseResult.ok(sixsService.updateS6(s));
    }
    @DeleteMapping("/s6/{id}")
    public ResponseResult<Boolean> deleteS6(@PathVariable Long id) { return ResponseResult.ok(sixsService.deleteS6(id)); }
}
