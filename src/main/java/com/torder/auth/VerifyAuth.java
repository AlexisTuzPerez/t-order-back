package com.torder.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/verifyAuth")
public class VerifyAuth{

    @GetMapping
    public ResponseEntity<String> hi(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(name);
    }
}
