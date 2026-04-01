package com.gabrielbl.healthaplication.services;

import com.gabrielbl.healthaplication.exception.BusinessException;
import com.gabrielbl.healthaplication.exception.NotFoundException;
import com.gabrielbl.healthaplication.model.DTOs.RegisterSetorDTO;
import com.gabrielbl.healthaplication.model.Empresa;
import com.gabrielbl.healthaplication.model.Setor;
import com.gabrielbl.healthaplication.model.Usuario;
import com.gabrielbl.healthaplication.repository.EmpresaRepository;
import com.gabrielbl.healthaplication.repository.SetorRepository;
import com.gabrielbl.healthaplication.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SetorService {

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void criarSetor(RegisterSetorDTO data, String nomeRh) {


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
}
