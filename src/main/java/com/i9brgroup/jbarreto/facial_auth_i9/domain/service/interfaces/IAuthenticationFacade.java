package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.UserLoginEntity;
import org.springframework.security.core.Authentication;

public interface IAuthenticationFacade {
    UserLoginEntity getAuthentication();
}
