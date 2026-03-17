package com.i9brgroup.jbarreto.facial_auth_i9.domain.models.auth;

import com.i9brgroup.jbarreto.facial_auth_i9.domain.enums.UserRoles;
import com.i9brgroup.jbarreto.facial_auth_i9.web.dto.request.UserLoginRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Table(name = "biometric_login")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserLoginEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(name = "site_id", nullable = false)
    private String siteId;
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRoles role;
    @Column(name = "[keyPhotoS3]")
    private String photo;

    public UserLoginEntity(UserLoginRequest userLoginRequest) {
        this.username = userLoginRequest.username();
        this.email = userLoginRequest.email();
        this.password = userLoginRequest.password();
        this.createdAt = LocalDateTime.now();
        this.siteId = userLoginRequest.siteId();
        this.role = userLoginRequest.roles();
    }

    public UserLoginEntity(String s, String site01) {
        this.username = s;
        this.siteId = site01;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == null) return List.of();
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
