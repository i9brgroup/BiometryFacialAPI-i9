package com.i9brgroup.jbarreto.facial_auth_i9.resources.repository;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.UserLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<UserLoginEntity, Long> {
    UserDetails findByEmail(String email);
    boolean existsByEmail(String email);
}
