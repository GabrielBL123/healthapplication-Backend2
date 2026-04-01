package com.gabrielbl.healthaplication.model.DTOs;

import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.UsuarioFuncao;

import java.time.Duration;
import java.time.LocalDateTime;

public record RegisterDTO(String nome,String login, String password, UsuarioFuncao role,
                          String empresaCnpj,
                          String cargo, //String setor,
                          LocalDateTime tempoDeTrabalho,
                          Duration jornada) {
}
