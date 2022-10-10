package com.refactoring.refactoringproject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "축하한다 휴먼!! 배포 성공!!";
    }
}
