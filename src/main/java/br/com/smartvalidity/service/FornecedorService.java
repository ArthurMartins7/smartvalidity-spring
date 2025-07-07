package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.repository.FornecedorRepository;
import br.com.smartvalidity.model.seletor.FornecedorSeletor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    public Fornecedor salvar(Fornecedor fornecedor) throws SmartValidityException {
        try {
            return fornecedorRepository.save(fornecedor);
        } catch (Exception e) {
            throw new SmartValidityException("Erro ao salvar fornecedor: " + e.getMessage());
        }
    }

    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    public List<Fornecedor> pesquisarComSeletor(FornecedorSeletor seletor) {
        if (seletor != null && seletor.temPaginacao()) {
            int pageNumber = seletor.getPagina();
            int pageSize = seletor.getLimite();

            PageRequest pagina = PageRequest.of(pageNumber - 1, pageSize);
            return fornecedorRepository.findAll(seletor, pagina).getContent();
        }
        return fornecedorRepository.findAll(seletor);
    }

    public int contarPaginas(FornecedorSeletor seletor) {
        if (seletor != null && seletor.temPaginacao()) {
            int pageSize = seletor.getLimite();
            PageRequest pagina = PageRequest.of(0, pageSize);

            Page<Fornecedor> paginaResultado = fornecedorRepository.findAll(seletor, pagina);
            return paginaResultado.getTotalPages();
        }

        long totalRegistros = fornecedorRepository.count(seletor);
        return totalRegistros > 0 ? 1 : 0;
    }

    public long contarTotalRegistros(FornecedorSeletor seletor) {
        return fornecedorRepository.count(seletor);
    }

    public Fornecedor buscarPorId(Integer id) throws SmartValidityException {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Fornecedor não encontrado com o ID: " + id));
    }

    public Fornecedor atualizar(Integer id, Fornecedor fornecedorAtualizado) throws SmartValidityException {
        Fornecedor fornecedor = buscarPorId(id);

        fornecedor.setNome(fornecedorAtualizado.getNome());
        fornecedor.setTelefone(fornecedorAtualizado.getTelefone());
        fornecedor.setCnpj(fornecedorAtualizado.getCnpj());

        return fornecedorRepository.save(fornecedor);
    }

    public void excluir(Integer id) throws SmartValidityException {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedorRepository.delete(fornecedor);
    }
}
