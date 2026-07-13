package com.gabrielbl.healthaplication.services;


import com.gabrielbl.healthaplication.model.*;
import com.gabrielbl.healthaplication.model.DTOs.RespostaDTO;
import com.gabrielbl.healthaplication.repository.*;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Null;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

@Component
@Transactional
public class RespostaGenerator {

    private final Faker faker = new Faker();
    private final Random random = new Random();

    private static final String[] CARGOS = {"Analista", "Desenvolvedor", "Gerente", "Coordenador", "Especialista"};
    private static final String[] SETORES = {"RH", "TI", "Financeiro", "Operações", "Marketing", "Vendas"};

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    RespostaRepository respostaRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    AvaliacaoMensalRepository avaliacaoMensalRepository;

    @Autowired
    AvaliacaoSetorRepository avaliacaoSetorRepository;

    @Autowired
    SetorRepository setorRepository;

    @Autowired
    AvaliacaoTokenLinkRepository avaliacaoTokenLinkRepository;


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
    Deve ser criado TODAS as entidades necessarias para submeter uma RESPOSTA
     */

    public Empresa generateRandomEmpresa() {
        Empresa empresa = new Empresa(
                "01234567891234",
                faker.company().name(),
                faker.internet().emailAddress(),
                faker.phoneNumber().cellPhone()

        );
        empresaRepository.save(empresa);
        return empresa;
    }



    public void generateRandomSetor(Empresa empresa,int i) {
        Setor setor = new Setor(
                SETORES[i],
                empresa
        );
        setorRepository.save(setor);
        empresa.getSetores().add(setor);
        empresaRepository.save(empresa);
    }

    public Usuario generateRandomUsuario(Empresa empresa) {

        Usuario usuario = new Usuario(
                faker.name().fullName(),
                faker.internet().emailAddress(),
                "",
                UsuarioFuncao.USER,
                empresa,
                CARGOS[random.nextInt(CARGOS.length)],
                LocalDateTime.now().minusDays(random.nextInt(365)),
                Duration.ofHours(random.nextInt())
        );
        usuarioRepository.save(usuario);

        return usuario;
    }

    public Usuario generateRandomRh(Empresa empresa) {

        Usuario usuario = new Usuario(
                "Rh aleatorio da empresa aleatoria",
                faker.internet().emailAddress(),
                "",
                UsuarioFuncao.RH,
                empresa,
                "Rh",
                null,
                null
        );

        usuarioRepository.save(usuario);

        return usuario;
    }

    public String generateRandomAvaliacaoMensal(Empresa empresa) {

        /// Cria e salva as entidades AvaliacaoMensal, AvaliacaoSetor e AvaliacaoTokenLink

        AvaliacaoMensal avaliacao = new AvaliacaoMensal(empresa);
        avaliacaoMensalRepository.save(avaliacao);
        for(Setor setor : empresa.getSetores()) {
            AvaliacaoSetor avaliacaoSetor = new AvaliacaoSetor(setor,avaliacao);
            avaliacao.getAvaliacaoSetores().add(avaliacaoSetor);
            avaliacaoSetorRepository.save(avaliacaoSetor);

        }
        AvaliacaoTokenLink avaliacaoLink = new AvaliacaoTokenLink();
        avaliacaoLink.setToken(UUID.randomUUID().toString());
        avaliacaoLink.setAvaliacaoMensal(avaliacao);
        avaliacaoTokenLinkRepository.save(avaliacaoLink);
        avaliacao.getAvaliacaoTokenLink().add(avaliacaoLink);
        avaliacaoMensalRepository.save(avaliacao);

        return avaliacaoLink.getToken();
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