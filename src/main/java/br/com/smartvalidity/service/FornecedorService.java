package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
