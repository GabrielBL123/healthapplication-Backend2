package com.gabrielbl.healthaplication.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "avaliacao_mensal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String competencia;

    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @OneToMany(mappedBy = "avaliacaoMensal",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<AvaliacaoSetor>  avaliacaoSetores;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private List<Usuario> usuarios;

    @OneToOne(mappedBy = "avaliacaoMensal",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private AvaliacaoTokenLink avaliacaoTokenLink;

}