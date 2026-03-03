package com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.ObjetoS3;

public interface ObjetoS3Service {
    ObjetoS3 save(ObjetoS3 objetoS3);
    String findById(String id);
}
