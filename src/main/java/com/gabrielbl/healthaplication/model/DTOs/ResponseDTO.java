package com.gabrielbl.healthaplication.model.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResponseDTO<T> {
    private String message;
    private T data;

    public ResponseDTO(String message, T data) {
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
}