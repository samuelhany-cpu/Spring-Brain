package com.example.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/direct")
public class DirectRepoController {

    private final UserRepository userRepository;

    public DirectRepoController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> all() {
        return userRepository.findAll();
    }
}
