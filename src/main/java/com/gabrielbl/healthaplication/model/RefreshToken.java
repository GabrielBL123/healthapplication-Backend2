package com.gabrielbl.healthaplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cglib.core.Local;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String tokenHash; // store a hash, not the raw token

    @ManyToOne
    private Usuario usuario;

    private Instant expiresAt;
    private boolean revoked = false;


    public RefreshToken(String s, Usuario usuario, Instant expiresAt, boolean b) {
        this.tokenHash = s;
        this.usuario = usuario;
        this.expiresAt = expiresAt;
        this.revoked = b;
    }
}