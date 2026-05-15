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

@Entity
@Table(name = "resposta"
        /*
        , uniqueConstraints = {
        @UniqueConstraint(columnNames = {"avaliacao_id", "pergunta_id"})
        }
*/
)
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
    @JoinColumn(name = "avaliacao_setor_id",nullable = false)
    private AvaliacaoSetor avaliacaoSetor;

    @Column(length =27,nullable = false)
    @Min(1)
    @Max(5)
    private List<Integer> valores;

    private LocalDateTime createdAt;










    /*
    NUNCA(1),//nunca
    RARAMENTE(2),//raramente
    FREQUENTEMENTE(3),//frequentemente
    ASVEZES(4),//as vezes
    SEMPRE(5);//semrpe


     */


}
