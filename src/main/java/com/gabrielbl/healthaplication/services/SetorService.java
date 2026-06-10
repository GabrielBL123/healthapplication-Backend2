package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.BusinessException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.AvaliacaoSetor;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarSetorDTO;
import com.gabrielbl.healthaplication.model.DTOs.SetorResponseDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.AvaliacaoSetorRepository;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.SetorRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SetorService {

    private final SetorRepository setorRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AvaliacaoSetorRepository avaliacaoSetorRepository;

    public SetorService(SetorRepository setorRepository,
                        EmpresaRepository empresaRepository,
                        UsuarioRepository usuarioRepository,
                        AvaliacaoSetorRepository avaliacaoSetorRepository
                        ) {
        this.setorRepository = setorRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.avaliacaoSetorRepository = avaliacaoSetorRepository;
    }

    public void criarSetor(RegistrarSetorDTO data, String nomeRh) {


        Usuario usuario = usuarioRepository.findByLogin(nomeRh);
        if(usuario == null){
            throw new NotFoundException("Usuario não cadastrado");
        }
        Empresa empresa = usuario.getEmpresa();
        if (empresa == null) {
            throw new NotFoundException("Empresa não encontrada");
        }

        if (setorRepository.findByNomeAndEmpresaCnpj(data.setor(), data.cnpj()) != null) {
            throw new BusinessException("Setor já existe nessa empresa");
        }

        Setor setor = new Setor();
        setor.setNome(data.setor());
        setor.setEmpresa(empresa);
        setorRepository.save(setor);

        AvaliacaoSetor avaliacaoSetor = new AvaliacaoSetor();
        avaliacaoSetor.setSetor(setor);
        avaliacaoSetorRepository.save(avaliacaoSetor);
    }


    public void deletarSetor(UUID id) {
        // Busca o setor diretamente pelo ID que veio da URL do React
        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Setor nao encontrado"));

        try {
            setorRepository.delete(setor);
        } catch (Exception e) {
            // Proteção extra caso o banco bloqueie a exclusão por ter funcionários nesse setor
            throw new RuntimeException("Não é possível deletar este setor pois ele já está em uso.");
        }
    }


    public void atualizarSetor(RegistrarSetorDTO data, String nomeRh) {

        Usuario usuario = usuarioRepository.findByLogin(nomeRh);
        if (usuario == null) throw new NotFoundException("Usuario nao encontrado");

        Empresa empresa = usuario.getEmpresa();

        // Correct argument order: nome first, then cnpj (matching repository method signature)
        Setor setor = setorRepository.findByNomeAndEmpresaCnpj(data.setor(), empresa.getCnpj());
        if (setor == null) throw new NotFoundException("Setor nao encontrado");

        setorRepository.save(setor);
    }

    public Page<SetorResponseDTO> getAllSetores(Pageable pageable) {

        Page<Setor> setores = setorRepository.findAll(pageable);


        return setores.map(a -> new SetorResponseDTO(a.getId(),a.getNome(),
                a.getEmpresa().getId(),a.getEmpresa().getNome()) );
    }

    public Page<SetorResponseDTO> getAllEmpresaSetores(String cnpj,Pageable pageable) {

        Empresa empresa = empresaRepository.findByCnpj(cnpj);

        if(empresa == null) throw new NotFoundException("Cnpj nao encontrado");

        Page<Setor> setores = setorRepository.findByEmpresaCnpj(empresa.getCnpj(), pageable);

        return setores.map(a -> new SetorResponseDTO(a.getId(),a.getNome(),
                a.getEmpresa().getId(),a.getEmpresa().getNome()));
    }
}
