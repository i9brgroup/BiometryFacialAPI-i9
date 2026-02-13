package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserLoginService {
    UserLoginEntity createNew(UserLoginRequest userLoginRequest);
    List<UserLoginEntity> getAllUsers();
}
