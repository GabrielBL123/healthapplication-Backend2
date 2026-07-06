package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.gabrielbl.healthaplication.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RespostaServiceMultiplasRespostasTest {

    @Mock private AvaliacaoMensalRepository avaliacaoMensalRepository;
    @Mock private SetorRepository setorRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AvaliacaoTokenLinkRepository tokenLinkRepository;
    @Mock private AvaliacaoSetorRepository avaliacaoSetorRepository;
    @Mock private RespostaRepository respostaRepository;

    @InjectMocks
    private RespostaService respostaService;

    private AvaliacaoMensal avaliacaoMensal;

    @BeforeEach
    void setUp() {
        // Plain in-memory objects - no persistence needed, service is mocked at the repository level
        Empresa empresa = new Empresa("12345678000199", "Empresa Teste", "empresa@teste.com", "11999999999");

        Setor setor = new Setor();
        setor.setNome("RH");
        setor.setEmpresa(empresa);

        avaliacaoMensal = new AvaliacaoMensal();
        avaliacaoMensal.setIsActive(true);
        avaliacaoMensal.setEmpresa(empresa);

        AvaliacaoSetor avaliacaoSetor = new AvaliacaoSetor();
        avaliacaoSetor.setSetor(setor);
        avaliacaoSetor.setAvaliacaoMensal(avaliacaoMensal);

        AvaliacaoTokenLink tokenLink = new AvaliacaoTokenLink();
        tokenLink.setToken("token-123");
        tokenLink.setAvaliacaoMensal(avaliacaoMensal);
        tokenLink.setIsActive(true);

        when(tokenLinkRepository.findByToken("token-123")).thenReturn(tokenLink);
        when(setorRepository.findByNomeAndEmpresaCnpj(eq("RH"), any())).thenReturn(setor);
        when(avaliacaoSetorRepository.findBySetorNomeAndAvaliacaoMensal(eq("RH"), eq(avaliacaoMensal)))
                .thenReturn(avaliacaoSetor);
    }

    @Test
    void submeterResposta_MultiplasRespostas_SalvaTodasCorretamente() {
        for (int i = 0; i < 10; i++) {
            String login = "funcionario" + i;
            when(usuarioRepository.findByLogin(login)).thenReturn(null); // each login is "new"

            RespostaDTO dto = new RespostaDTO(
                    "Funcionario " + i,
                    login,
                    "Analista",
                    "RH",
                    LocalDateTime.of(2024, 1, 10, 8, 0),
                    Duration.ofHours(8),
                    IntStream.rangeClosed(1, 52).toArray()
            );

            respostaService.submeterResposta(dto, "token-123");
        }

        ArgumentCaptor<Resposta> captor = ArgumentCaptor.forClass(Resposta.class);
        verify(respostaRepository, times(10)).save(captor.capture());
        assertEquals(10, captor.getAllValues().size());
    }
}