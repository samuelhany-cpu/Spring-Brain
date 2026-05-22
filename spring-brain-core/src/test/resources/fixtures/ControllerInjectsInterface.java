package com.example.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that injects a service by its interface name (no @Service on the interface itself).
 * The concrete implementation UserServiceImpl implements UserService.
 */
@RestController
@RequestMapping("/api/items")
public class ControllerInjectsInterface {

    private final UserService userService;

    public ControllerInjectsInterface(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Object list() {
        return userService.findAll();
    }
}
