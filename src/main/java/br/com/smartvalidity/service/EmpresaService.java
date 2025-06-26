package br.com.smartvalidity.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.EmpresaUsuarioDTO;
import br.com.smartvalidity.model.entity.Empresa;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.repository.EmpresaRepository;
import jakarta.transaction.Transactional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Empresa cadastrarEmpresaEAssinante(EmpresaUsuarioDTO dto) throws SmartValidityException {

        // Verifica se CNPJ já existe
        if (empresaRepository.findByCnpj(dto.getCnpj()).isPresent()) {
            throw new SmartValidityException("CNPJ já cadastrado!");
        }

        // Verifica se e-mail já existe utilizando regra do UsuarioService
        //usuarioService.verificarEmailJaUtilizado(dto.getEmail(), null);

        // Monta entidade Empresa
        Empresa empresa = new Empresa();
        empresa.setCnpj(dto.getCnpj());
        empresa.setRazaoSocial(dto.getRazaoSocial());

        // Monta usuário assinante
        Usuario assinante = new Usuario();
        assinante.setNome(dto.getNomeUsuario());
        assinante.setEmail(dto.getEmail());
        assinante.setCargo(dto.getCargo());
        assinante.setSenha(passwordEncoder.encode(dto.getSenha()));
        assinante.setPerfilAcesso(PerfilAcesso.ASSINANTE);
        assinante.setEmpresa(empresa);

        // Associação empresa ⇄ usuários
        empresa.setUsuarios(new ArrayList<>());
        empresa.getUsuarios().add(assinante);

        // Persistir (cascade ALL salva o usuário)
        return empresaRepository.save(empresa);
    }

    public Empresa buscarPorId(String id) throws SmartValidityException {
        return this.empresaRepository.findById(id).orElseThrow(() -> new SmartValidityException("Empresa não encontrada"));
    }
} 