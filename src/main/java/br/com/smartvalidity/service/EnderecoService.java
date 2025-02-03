package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.model.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;


    public Endereco salvar(Endereco endereco) {

        return enderecoRepository.save(endereco);
    }

    public List<Endereco> listarTodos() {

        return enderecoRepository.findAll();
    }

    public Endereco buscarPorId(Integer id) throws SmartValidityException {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Endereço não encontrada com o ID: " + id));
    }

    public Endereco atualizar(Integer id, Endereco enderecoAtualizado) throws SmartValidityException {
        Endereco endereco = buscarPorId(id);

        endereco.setLogradouro(enderecoAtualizado.getLogradouro());
        endereco.setNumero(enderecoAtualizado.getNumero());
        endereco.setComplemento(enderecoAtualizado.getComplemento());
        endereco.setBairro(enderecoAtualizado.getBairro());
        endereco.setCidade(enderecoAtualizado.getCidade());
        endereco.setEstado(enderecoAtualizado.getEstado());
        endereco.setPais(enderecoAtualizado.getPais());
        endereco.setCep(enderecoAtualizado.getCep());

        return enderecoRepository.save(endereco);
    }

    public void excluir(Integer id) throws SmartValidityException {
        Endereco endereco = buscarPorId(id);
        enderecoRepository.delete(endereco);
    }
}
