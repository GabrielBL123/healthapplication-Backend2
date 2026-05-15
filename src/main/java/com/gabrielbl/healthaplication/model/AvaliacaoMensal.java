package com.gabrielbl.healthaplication.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "avaliacao_mensal"/*, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "competencia"})
}
*/
)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AvaliacaoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String competencia; //2026-03

    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @OneToMany(mappedBy = "avaliacaoMensal",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<AvaliacaoSetor>  avaliacaoSetores;

    @OneToOne(mappedBy = "avaliacaoMensal",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private AvaliacaoTokenLink avaliacaoTokenLink;

}