package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.AlreadySubmittedException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.AvaliacaoMensal;
import com.gabrielbl.healthaplication.model.DTOs.AtualizarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.AvaliacaoMensalResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.EmpresaResponseDTO;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarEmpresaDTO;
import com.gabrielbl.healthaplication.model.DTOs.SetorResponseDTO; // ✨ IMPORT NOVO
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.AvaliacaoTokenLink;
import com.gabrielbl.healthaplication.repository.AvaliacaoMensalRepository;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.AvaliacaoTokenLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors; // ✨ IMPORT NOVO

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AvaliacaoTokenLinkRepository avaliacaoLinkRepository;

    @Autowired
    private AvaliacaoMensalRepository avaliacaoMensalRepository;

    public void criarEmpresa(RegistrarEmpresaDTO data) {
        if(empresaRepository.findByCnpj(data.cnpj())!=null) throw new AlreadySubmittedException("Cnpj ja registrado.");

        Empresa empresa = new Empresa(data.cnpj(), data.nome(), data.email(), data.telefone());
        empresaRepository.save(empresa);
    }

    public void atualizarEmpresa(UUID id, AtualizarEmpresaDTO data) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa nao encontrada"));

        empresa.setNome(data.nome());
        empresa.setEmail(data.email());
        empresa.setTelefone(data.telefone());

        empresaRepository.save(empresa);
    }

    public void deletarEmpresa(UUID id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa nao encontrada"));

        empresaRepository.delete(empresa);
    }

    public Page<EmpresaResponseDTO> getAllEmpresas(Pageable pageable) {
        Page<Empresa> page = empresaRepository.findAll(pageable);

        // ✨ Redirecionado para o nosso novo método de conversão 'toDTO'
        return page.map(this::toDTO);
    }

    public Empresa getEmpresa(UUID id) {
        return empresaRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Empresa nao encontrada"));
    }

    // ========================================================================
    // ✨ NOVO MÉTODO: Converte a Entidade para DTO puxando os Setores juntos!
    // ========================================================================
    private EmpresaResponseDTO toDTO(Empresa empresa) {
        List<SetorResponseDTO> listaSetores = null;

        // Verifica se a empresa tem setores vinculados antes de tentar listar
        if (empresa.getSetores() != null && !empresa.getSetores().isEmpty()) {
            listaSetores = empresa.getSetores().stream()
                    .map(setor -> new SetorResponseDTO(
                            setor.getId(),        // 1. ID do Setor
                            setor.getNome(),      // 2. Nome do Setor
                            empresa.getId(),      // 3. ID da Empresa (O Java já tem isso aqui no método!)
                            empresa.getNome()     // 4. Nome da Empresa
                    ))
                    .collect(Collectors.toList());
        }

        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getCnpj(),
                empresa.getNome(),
                empresa.getEmail(),
                empresa.getTelefone(),
                listaSetores
        );
    }
}