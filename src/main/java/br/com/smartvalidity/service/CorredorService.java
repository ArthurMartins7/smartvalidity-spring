package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.repository.CorredorRepository;
import br.com.smartvalidity.model.seletor.CorredorSeletor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorredorService {

    @Autowired
    private CorredorRepository corredorRepository;

    public Corredor salvar(Corredor corredor) throws SmartValidityException {
        try {
            return corredorRepository.save(corredor);
        } catch (Exception e) {
            throw new SmartValidityException("Erro ao salvar corredor: " + e.getMessage());
        }
    }

    public List<Corredor> listarTodos() {
        return corredorRepository.findAll();
    }

    public List<Corredor> pesquisarComSeletor(CorredorSeletor seletor) {
        if (seletor != null && seletor.temPaginacao()) {
            PageRequest pageRequest = PageRequest.of(seletor.getPagina() - 1, seletor.getLimite());
            return corredorRepository.findAll(seletor, pageRequest).getContent();
        }
        return corredorRepository.findAll(seletor);
    }

    public int contarPaginas(CorredorSeletor seletor) {
        if (seletor != null && seletor.temPaginacao()) {
            PageRequest pageRequest = PageRequest.of(0, seletor.getLimite());
            Page<Corredor> resultado = corredorRepository.findAll(seletor, pageRequest);
            return resultado.getTotalPages();
        }
        long total = corredorRepository.count(seletor);
        return total > 0 ? 1 : 0;
    }

    public long contarTotalRegistros(CorredorSeletor seletor) {
        return corredorRepository.count(seletor);
    }

    public Corredor buscarPorId(Integer id) throws SmartValidityException {
        return corredorRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Corredor não encontrado com o ID: " + id));
    }

    public Corredor atualizar(Integer id, Corredor corredorAtualizado) throws SmartValidityException {
        Corredor existente = buscarPorId(id);

        existente.setNome(corredorAtualizado.getNome());
        existente.setResponsaveis(corredorAtualizado.getResponsaveis());

        return corredorRepository.save(existente);
    }

    public void excluir(Integer id) throws SmartValidityException {
        Corredor existente = buscarPorId(id);
        corredorRepository.delete(existente);
    }
}
