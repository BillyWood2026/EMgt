package com.example.hrplanning.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 培养工程学员（base: /api/training-monitoring/students）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training-monitoring/students")
public class TrainingStudentController extends AbstractTrainingStudentController {
}
