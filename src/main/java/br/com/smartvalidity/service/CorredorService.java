package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.repository.CorredorRepository;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CorredorService {

    @Autowired
    private CorredorRepository corredorRepository;

    @Autowired
    private UsuarioService usuarioService;

    public Corredor salvar(Corredor corredor) throws SmartValidityException {
        Set<Usuario> responsaveis = new HashSet<>();

        for (Usuario responsavel : corredor.getResponsaveis()) {
            Usuario u = this.usuarioService.buscarPorId(responsavel.getId());
            responsaveis.add(u);
        }

        corredor.setResponsaveis(responsaveis);
        return corredorRepository.save(corredor);
    }

    @Transactional(readOnly = true)
    public List<Corredor> listarTodos() {
        return corredorRepository.findAllWithCategorias();
    }

    public Corredor buscarPorId(Integer id) throws SmartValidityException {
        return corredorRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Corredor não encontrado com o ID: " + id));
    }

    public Corredor atualizar(Integer id, Corredor corredorAtualizado) throws SmartValidityException {
        Corredor corredor = buscarPorId(id);

        corredor.setNome(corredorAtualizado.getNome());
        corredor.setResponsaveis(corredorAtualizado.getResponsaveis());

        return corredorRepository.save(corredor);
    }

    public void excluir(Integer id


    ) throws SmartValidityException {
        Corredor corredor = buscarPorId(id);
        corredorRepository.delete(corredor);
    }
}
