package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.UsuarioFuncao;

import java.time.Duration;
import java.time.LocalDateTime;

public record RegistrarDTO(String nome, String login, String password, UsuarioFuncao role,
                           String empresaCnpj,
                           String cargo, //String setores,
                           LocalDateTime tempoDeTrabalho,
                           Duration jornada) {
}
