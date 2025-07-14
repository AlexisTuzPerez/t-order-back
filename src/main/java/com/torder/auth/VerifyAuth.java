package com.torder.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.torder.user.User;
import com.torder.user.UserRepository;


@RestController
@RequestMapping("api/verifyAuth")
public class VerifyAuth{

    private final UserRepository userRepository;

    public VerifyAuth(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<User> hi(){

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(name).get();

        return ResponseEntity.ok(user);
    }
}
