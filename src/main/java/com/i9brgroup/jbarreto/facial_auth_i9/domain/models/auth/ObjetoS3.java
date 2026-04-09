package com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "[objetosS3]")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ObjetoS3 {
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "[NomeObjeto]")
    private String nomeArquivoS3;
}
