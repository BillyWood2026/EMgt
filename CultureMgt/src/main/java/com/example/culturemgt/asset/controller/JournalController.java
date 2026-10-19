package com.example.culturemgt.asset.controller;

import com.example.culturemgt.asset.entity.CultureJournal;
import com.example.culturemgt.asset.service.JournalService;
import com.example.culturemgt.common.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/culture/journal")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping("/list")
    public ResponseResult<List<CultureJournal>> list(@RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String keyword) {
        return ResponseResult.success(journalService.list(status, keyword));
    }

    @GetMapping("/public")
    public ResponseResult<List<CultureJournal>> publicList() {
        return ResponseResult.success(journalService.publicList());
    }

    @GetMapping("/{id}")
    public ResponseResult<CultureJournal> get(@PathVariable Long id) {
        return ResponseResult.success(journalService.get(id));
    }

    @PostMapping
    public ResponseResult<CultureJournal> save(@RequestBody CultureJournal journal) {
        return ResponseResult.success(journalService.save(journal));
    }

    @PostMapping("/{id}/publish")
    public ResponseResult<Void> publish(@PathVariable Long id) {
        journalService.publish(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/offline")
    public ResponseResult<Void> offline(@PathVariable Long id) {
        journalService.offline(id);
        return ResponseResult.success(null);
    }

    @PostMapping("/{id}/view")
    public ResponseResult<CultureJournal> view(@PathVariable Long id) {
        return ResponseResult.success(journalService.view(id));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        journalService.delete(id);
        return ResponseResult.success(null);
    }
}
