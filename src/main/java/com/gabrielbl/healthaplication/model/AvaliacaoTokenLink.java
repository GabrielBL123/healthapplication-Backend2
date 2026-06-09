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

    private LocalDateTime expiracaoEm;

    @Column(nullable = false)
    private LocalDateTime criadoEm =  LocalDateTime.now();

    @Column(nullable = false)
    private Boolean isActive = true;


    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "avaliacao_mensal_id", nullable = false)
    private AvaliacaoMensal avaliacaoMensal;


    public boolean isValid(){
        if(!isActive) return false;
        if(expiracaoEm==null) return true;
        return expiracaoEm.isAfter(LocalDateTime.now());
    }
}