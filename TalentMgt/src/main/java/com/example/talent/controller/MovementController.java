package com.example.talent.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位异动/晋升调薪 Controller（base: /api/movement）
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/movement")
public class MovementController extends AbstractMovementController {
}
