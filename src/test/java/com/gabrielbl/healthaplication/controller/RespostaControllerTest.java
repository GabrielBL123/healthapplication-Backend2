package com.gabrielbl.healthaplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielbl.healthaplication.infra.security.SecurityFilter;
import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.gabrielbl.healthaplication.services.RespostaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.FilterType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RespostaController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class RespostaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RespostaService respostaService;

    @Test
    void submeterResposta_WhenRequestIsValid_ReturnsSuccess() throws Exception {
        String token = "token-123";
        RespostaDTO request = new RespostaDTO(
                "Funcionario Teste",
                "funcionario.teste",
                "Analista",
                "RH",
                LocalDateTime.of(2024, 1, 10, 8, 0),
                Duration.ofHours(8),
                IntStream.rangeClosed(1, 52).toArray()
        );

        mockMvc.perform(post("/resposta/responder/{token-id}", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resposta submetido com sucesso"))
                .andExpect(jsonPath("$.data").value(nullValue()));

        verify(respostaService).submeterResposta(any(RespostaDTO.class), eq(token));
    }
}
