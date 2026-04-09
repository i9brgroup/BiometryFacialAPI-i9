package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.UserLoginService;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.InvalidCredentialsException;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.auth.UserRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.AuthenticationDataRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserServiceImpl implements UserLoginService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Override
    public UserLoginEntity createNew(UserLoginRequest userLoginRequest) {
        if (userRepository.existsByEmail(userLoginRequest.email())) {
            throw new UsernameNotFoundException("Email already registered");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null){
            logger.info("Usuario {} tentativa de cadastro de novo usuário com email: {}", auth.getName(), userLoginRequest.email());
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

    @Override
    public boolean deleteById(Long id) {
        return false;
    }

    @Override
    public TokenResponse signIn(AuthenticationDataRequest userDataDTO){
        var authenticationToken = new UsernamePasswordAuthenticationToken(userDataDTO.email(), userDataDTO.password());
        var auth = authenticationManager.authenticate(authenticationToken);
        var user = (UserLoginEntity) auth.getPrincipal();

        if (user == null) {
            logger.warn("Credenciais inválidas para email: {}", userDataDTO.email());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        logger.info("Usuario {} logado no sistema na data {}", user.getEmail(), LocalDateTime.now().format(formatter));

        var accessToken = tokenService.createAccessToken(user);
        var refreshToken = tokenService.createRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public void signOut(String refreshToken) {
        tokenService.putTokenOnBlacklist(refreshToken);
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        return tokenService.refresh(refreshToken);
    }



}
