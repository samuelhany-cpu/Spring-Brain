package com.example.security;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secure")
@PreAuthorize("hasRole('ADMIN')")
public class SecuredController {

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public String staffOnly() {
        return "staff";
    }

    @GetMapping("/legacy")
    @Secured("ROLE_ADMIN")
    public String legacyAdmin() {
        return "legacy";
    }

    @GetMapping("/jakarta")
    @RolesAllowed("ADMIN")
    public String jakartaAdmin() {
        return "jakarta";
    }
}
