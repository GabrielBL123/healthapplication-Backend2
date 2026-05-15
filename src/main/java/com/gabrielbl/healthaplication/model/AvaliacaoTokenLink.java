package com.gabrielbl.healthaplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao_link")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvaliacaoTokenLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID )
    private String id;

    @Column(nullable = false,unique = true)
    private String token;

    private LocalDateTime dataExpiracao;
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private AvaliacaoMensal avaliacaoMensal;

    @Enumerated(EnumType.STRING)
    private AvaliacaoStatusToken status = AvaliacaoStatusToken.ATIVO;

    public boolean isValid() {
        return status == AvaliacaoStatusToken.ATIVO && LocalDateTime.now().isBefore(dataExpiracao);
    }

}