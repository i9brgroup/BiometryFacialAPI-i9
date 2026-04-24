package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.AuthenticationDataRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.ListUserResponse;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.response.TokenResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface UserLoginService {
    UserLoginEntity createNew(UserLoginRequest userLoginRequest);
    Page<ListUserResponse> getAllUsersPageable(Integer page, Integer size, String orderBy, String direction);
    TokenResponse signIn(AuthenticationDataRequest authenticationDataRequest);
    void signOut(String refreshToken);
    TokenResponse refresh(String refreshToken);
    boolean toggleActive(Long id);
}
