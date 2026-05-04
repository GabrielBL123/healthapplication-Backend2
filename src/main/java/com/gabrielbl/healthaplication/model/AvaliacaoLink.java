package com.gabrielbl.healthaplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "avaliacao_link")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvaliacaoLink {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String linkToken;

    @Column(nullable = false)
    private UUID empresaId;

    private LocalDateTime craidoEm = LocalDateTime.now();

    private LocalDateTime expiraEm;

}