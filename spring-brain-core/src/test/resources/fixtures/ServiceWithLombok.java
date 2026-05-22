package com.example.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceWithLombok {

    private final UserRepository userRepository;
    private final UserService userService;
    private static final String CONSTANT = "ignored";
}
