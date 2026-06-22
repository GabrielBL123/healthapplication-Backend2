package com.gabrielbl.healthaplication.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "resposta")
@Table(name = "resposta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "token_id")
    private AvaliacaoTokenLink token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_setor_id", nullable = false)
    private AvaliacaoSetor avaliacaoSetor;

    @Column(length = 52, nullable = false)
    @Min(1)
    @Max(5)
    private List<Integer> valores;

    private LocalDateTime createdAt;
}
