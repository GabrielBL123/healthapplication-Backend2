package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


     Optional<RefreshToken> findByTokenHash(String tokenHash);
}
