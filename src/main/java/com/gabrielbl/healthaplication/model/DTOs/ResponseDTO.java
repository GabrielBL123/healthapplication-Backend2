package com.gabrielbl.healthaplication.model.DTOs;

public class ResponseDTO<T> {
    private String message;
    private T data;

    public ResponseDTO(String message, T data) {
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
}