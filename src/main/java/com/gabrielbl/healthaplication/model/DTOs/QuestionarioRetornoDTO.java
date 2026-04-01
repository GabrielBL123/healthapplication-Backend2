package com.gabrielbl.healthaplication.model.DTOs;

public record QuestionarioRetornoDTO(
        Long submissionId,
        Integer competence,
        Long templateId
) {
}
