package com.gabrielbl.healthaplication.repository;

import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import com.gabrielbl.healthaplication.model.AvaliacaoTokenLink;
import jakarta.transaction.Transactional;
import org.apache.el.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoTokenLinkRepository extends JpaRepository<AvaliacaoTokenLink, UUID> {

    AvaliacaoTokenLink findByToken(String token);



    // ✅ Buscar token ativo por avaliacaoMensal
    List<AvaliacaoTokenLink> findByAvaliacaoMensalAndIsActive(
            AvaliacaoMensal avaliacaoMensal,
            Boolean isActive
    );

    // ✅ Buscar um token válido (se existir)
    Optional<AvaliacaoTokenLink> findFirstByAvaliacaoMensalAndIsActiveOrderByExpiracaoEmDesc(
            AvaliacaoMensal avaliacaoMensal,
            Boolean isActive
    );

    // ✅ Desativar todos os tokens de uma avaliação
    @Modifying
    @Transactional
    @Query("""
        UPDATE AvaliacaoTokenLink 
        SET isActive = false 
        WHERE avaliacaoMensal.id = :avaliacaoMensalId AND isActive = true
    """)
    int desativarTodosOsTokens(@Param("avaliacaoMensalId") UUID avaliacaoMensalId);

    // ✅ Validar token por string (para usar o link)
    Optional<AvaliacaoTokenLink> findByTokenAndIsActive(String token, Boolean isActive);
}
