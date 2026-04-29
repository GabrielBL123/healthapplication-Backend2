package com.gabrielbl.healthaplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "avaliacao_mensal_setores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AvaliacaoSetor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @OneToMany(mappedBy = "avaliacaoSetor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Resposta> resposta = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_mensal_id", nullable = false)
    private AvaliacaoMensal avaliacaoMensal;

}
