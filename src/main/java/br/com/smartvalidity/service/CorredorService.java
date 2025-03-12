package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.repository.CorredorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorredorService {

    @Autowired
    private CorredorRepository corredorRepository;

    public Corredor salvar(Corredor corredor) {
        return corredorRepository.save(corredor);
    }

    public List<Corredor> listarTodos() {
        return corredorRepository.findAll();
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
