package com.example.user;

import org.springframework.stereotype.Service;

@Service
public class BrokenService {

    private final NonExistentRepository repo;

    public BrokenService(NonExistentRepository repo) {
        this.repo = repo;
    }
}
