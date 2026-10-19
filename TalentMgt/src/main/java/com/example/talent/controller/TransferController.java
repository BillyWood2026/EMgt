package com.example.talent.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位调动 Controller（base: /api/transfer，/api/movement 的别名入口）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/transfer")
public class TransferController extends AbstractMovementController {
}
