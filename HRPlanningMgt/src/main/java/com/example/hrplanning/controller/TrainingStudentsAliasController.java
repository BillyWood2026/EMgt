package com.example.hrplanning.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学员别名入口（base: /api/training-students）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/training-students")
public class TrainingStudentsAliasController extends AbstractTrainingStudentController {
}
