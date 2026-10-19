package com.example.hrplanning.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 培养过程监控进度（base: /api/training-monitoring/progress）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training-monitoring/progress")
public class TrainingProgressController extends AbstractTrainingProgressController {
}
