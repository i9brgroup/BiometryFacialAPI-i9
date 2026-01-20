package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.UserLoginService;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.UserRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAuthentication implements UserDetailsService {

    private final UserRepository userRepository;

    public UserAuthentication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail((username));
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return user;
    }
}
