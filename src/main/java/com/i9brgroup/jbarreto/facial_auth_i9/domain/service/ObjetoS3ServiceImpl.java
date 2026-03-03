package com.i9brgroup.jbarreto.facial_auth_i9.domain.service;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth.ObjetoS3;
import com.i9brgroup.jbarreto.facial_auth_i9.domain.service.interfaces.ObjetoS3Service;
import com.i9brgroup.jbarreto.facial_auth_i9.resources.repository.auth.ObjetoS3Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ObjetoS3ServiceImpl implements ObjetoS3Service {

    private final ObjetoS3Repository objetoS3Repository;
    private final Logger logger = LoggerFactory.getLogger(ObjetoS3ServiceImpl.class);

    public ObjetoS3ServiceImpl(ObjetoS3Repository objetoS3Repository) {
        this.objetoS3Repository = objetoS3Repository;
    }

    @Override
    public ObjetoS3 save(ObjetoS3 objetoS3) {
        return objetoS3Repository.save(objetoS3);
    }

    @Override
    public String findById(String id) {
         var objeto = objetoS3Repository.findById(id);
         if (objeto.isEmpty()){
             logger.error("Objeto S3 não encontrado");
             return null;
         }
            return objeto.get().getNomeArquivoS3();
    }
}
