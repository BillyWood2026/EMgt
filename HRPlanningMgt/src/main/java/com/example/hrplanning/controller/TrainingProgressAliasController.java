package com.example.hrplanning.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监控进度别名入口（base: /api/training-progress）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training-progress")
public class TrainingProgressAliasController extends AbstractTrainingProgressController {
}
