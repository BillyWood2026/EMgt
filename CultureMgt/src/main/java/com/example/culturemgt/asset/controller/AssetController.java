package com.example.culturemgt.asset.controller;

import com.example.culturemgt.asset.entity.CultureAsset;
import com.example.culturemgt.asset.service.AssetService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/culture/asset")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/list")
    public ResponseResult<List<CultureAsset>> list(@RequestParam(required = false) String assetType,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String keyword) {
        return ResponseResult.success(assetService.list(assetType, status, keyword));
    }

    @GetMapping("/{id}")
    public ResponseResult<CultureAsset> get(@PathVariable Long id) {
        return ResponseResult.success(assetService.get(id));
    }

    @PostMapping
    public ResponseResult<CultureAsset> save(@RequestBody CultureAsset asset) {
        return ResponseResult.success(assetService.save(asset));
    }

    @PostMapping("/{id}/toggle")
    public ResponseResult<Void> toggle(@PathVariable Long id) {
        assetService.toggle(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/view")
    public ResponseResult<CultureAsset> view(@PathVariable Long id) {
        return ResponseResult.success(assetService.view(id));
    }

    @PostMapping("/{id}/download")
    public ResponseResult<CultureAsset> download(@PathVariable Long id) {
        return ResponseResult.success(assetService.download(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ResponseResult.success(null);
    }
}
