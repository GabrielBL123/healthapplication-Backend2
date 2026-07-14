package com.gabrielbl.healthaplication.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String tokenHash; // store a hash, not the raw token

    @ManyToOne
    private Usuario usuario;

    private LocalDateTime expiryDate;
    private boolean revoked = false;



}