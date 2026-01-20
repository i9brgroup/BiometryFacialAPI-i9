package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.UserLoginService;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.UserRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserLoginEntity createNew(UserLoginRequest userLoginRequest) {
        if (userRepository.existsByEmail(userLoginRequest.email())) {
            throw new UsernameNotFoundException("Email already registered");
        }

        var userPassword = userLoginRequest.password();
        var passwordEncoded = passwordEncoder.encode(userPassword);
        var user = new UserLoginEntity(userLoginRequest);
        user.setPassword(passwordEncoded);

        return userRepository.save(user);
    }

    @Override
    public List<UserLoginEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
