package com.example.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DtoRepository extends JpaRepository<UserDto, Long> {
}
