package com.gabrielbl.healthaplication.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Entity(name = "usuario")
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String nome;
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    private UsuarioFuncao role;

    private String cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor setor;

    private LocalDateTime tempoDeTrabalho;
    private Duration jornada;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "avaliacao_mensal_id")
    private AvaliacaoMensal avaliacaoMensal;






    public Usuario(String login, String nome, String password, UsuarioFuncao role,
                   Empresa empresa, String cargo, //String setores,
                   LocalDateTime tempoDeTrabalho, Duration jornada) {

        this.login = login;
        this.nome = nome;
        this.password = password;
        this.role = role;
        this.empresa = empresa;
        this.cargo = cargo;
        //this.setores = setores;
        this.tempoDeTrabalho = tempoDeTrabalho;
        this.jornada = jornada;

    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (this.role) {
            case ADMIN -> List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            case RH -> List.of(
                    new SimpleGrantedAuthority("ROLE_RH"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            case USER -> List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

        };
    }


    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
