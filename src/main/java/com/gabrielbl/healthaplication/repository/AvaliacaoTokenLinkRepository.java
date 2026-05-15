package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.AvaliacaoTokenLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AvaliacaoTokenLinkRepository extends JpaRepository<AvaliacaoTokenLink, UUID> {

    AvaliacaoTokenLink findByToken(String token);
}
