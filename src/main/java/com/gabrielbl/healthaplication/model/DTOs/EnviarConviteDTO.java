package com.gabrielbl.healthaplication.model.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EnviarConviteDTO(@NotBlank @Email String email) {}
