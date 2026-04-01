package com.gabrielbl.healthaplication.model.DTOs;

import java.util.List;

public record LoginResponseDTO(String token, List<String> roles) {
}
