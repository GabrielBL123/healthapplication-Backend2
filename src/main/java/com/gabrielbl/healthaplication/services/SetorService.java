package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.BusinessException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.RegistrarSetorDTO;
import com.gabrielbl.healthaplication.model.DTOs.SetorResponseDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.SetorRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetorService {

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    }


    public void deletarSetor(RegistrarSetorDTO data) {

        Setor setor = setorRepository.findByNomeAndEmpresaCnpj(data.setor(), data.cnpj());

        if(setor == null) throw new NotFoundException("Setor nao encontrado");
        setorRepository.delete(setor);


    }


    public void atualizarSetor(RegistrarSetorDTO data, String nomeRh) {

        Usuario usuario = usuarioRepository.findByLogin(nomeRh);
        if(usuario == null) throw new NotFoundException("Usuario nao encontrado");

        Empresa empresa = usuario.getEmpresa();

        Setor setor = setorRepository.findByNomeAndEmpresaCnpj(empresa.getCnpj(), data.setor());

        setorRepository.save(setor);
    }

    public List<SetorResponseDTO> getAllSetores() {
          return setorRepository.findAll().stream()
                  .map(this::toDTO)
                  .toList();
    }

    public List<SetorResponseDTO> getAllEmpresaSetores(String cnpj) {

        Empresa empresa = empresaRepository.findByCnpj(cnpj);

        return empresa.getSetor().stream()
                .map(this::toDTO)
                .toList();
    }

    private SetorResponseDTO toDTO(Setor setor) {
        Empresa empresa = setor.getEmpresa();
        return new SetorResponseDTO(
                setor.getId(),
                setor.getNome(),
                empresa != null ? empresa.getId() : null,
                empresa != null ? empresa.getNome() : null
        );
    }
}
