package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.UserLoginService;
import com.i9brgroup.jbarreto.facial_auth_i9.infrastructure.exceptions.model.InvalidCredentialsException;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.auth.UserRepository;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.AuthenticationDataRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ListUserResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TokenResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final RefreshTokenStore refreshTokenStore;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, TokenService tokenService, RefreshTokenStore refreshTokenStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenStore = refreshTokenStore;
    }

    @Override
    public UserLoginEntity createNew(UserLoginRequest userLoginRequest) {
        if (userRepository.existsByEmail(userLoginRequest.email())) {
            throw new UsernameNotFoundException("Email already registered");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            logger.info("Usuario {} tentativa de cadastro de novo usuário com email: {}", auth.getName(), userLoginRequest.email());
        }

        var userPassword = userLoginRequest.password();
        var passwordEncoded = passwordEncoder.encode(userPassword);
        var user = new UserLoginEntity(userLoginRequest);
        user.setPassword(passwordEncoded);

        return userRepository.save(user);
    }

    @Override
    public Page<ListUserResponse> getAllUsersPageable(Integer page, Integer size, String orderBy, String direction) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), orderBy);
        Page<UserLoginEntity> foundUsers = userRepository.findAll(pageRequest);
        return foundUsers.map(ListUserResponse::new);
    }

    @Override
    public TokenResponse signIn(AuthenticationDataRequest userDataDTO) {
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

    @Override
    @Transactional
    public boolean toggleActive(Long id) {
        var user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        var newStatus = user.toggleAtivo();
        userRepository.save(user);

        if (!newStatus) {
            refreshTokenStore.deleteByUserId(id);
        }
        return newStatus;
    }
}
