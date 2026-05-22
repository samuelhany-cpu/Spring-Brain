package com.example.user;

import javax.inject.Inject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inject")
public class ControllerWithInjectField {

    @Inject
    private UserService userService;

    @GetMapping
    public Object list() {
        return userService.findAll();
    }
}
