package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;

@Component
public class RespostaGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    private static final String[] CARGOS = {"Analista", "Desenvolvedor", "Gerente", "Coordenador", "Especialista"};
    private static final String[] SETORES = {"RH", "TI", "Financeiro", "Operações", "Marketing", "Vendas"};

    public RespostaDTO generateRandomResposta() {
        return new RespostaDTO(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                CARGOS[random.nextInt(CARGOS.length)],
                SETORES[random.nextInt(SETORES.length)],
                LocalDateTime.now().minusDays(random.nextInt(30)),
                Duration.ofHours(random.nextInt(2, 12)), // 2 to 12 hours
                generateRandomAnswers() // 52 random answers (0-5 scale or boolean)
        );
    }

    /**
     * Generates 52 random answers.
     * Adjust the range (0-5) based on your questionnaire scale.
     */
    private int[] generateRandomAnswers() {
        int[] answers = new int[52];
        for (int i = 0; i < 52; i++) {
            answers[i] = random.nextInt(6); // 0-5 scale (change based on your needs)
        }
        return answers;
    }

    public int[] generateAnswersWithPattern(int pattern) {
        int[] answers = new int[52];
        switch (pattern) {
            case 1 -> Arrays.fill(answers, 5); // All positive
            case 2 -> Arrays.fill(answers, 1); // All negative
            case 3 -> {
                // Mixed pattern
                for (int i = 0; i < 52; i++) {
                    answers[i] = i % 2 == 0 ? 5 : 1;
                }
            }
            default -> {
                // Random (default)
                for (int i = 0; i < 52; i++) {
                    answers[i] = random.nextInt(6);
                }
            }
        }
        return answers;
    }
}