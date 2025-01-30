package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public Produto buscarPorId(Integer id) throws SmartValidityException {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Produto não encontrado com o ID: " + id));
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto atualizar(Integer id, Produto produtoAtualizado) throws SmartValidityException {
        Produto produto = buscarPorId(id);

        produto.setCodigoBarras(produtoAtualizado.getCodigoBarras());
        produto.setDescricao(produtoAtualizado.getDescricao());
        produto.setMarca(produtoAtualizado.getMarca());
        produto.setUnidadeMedida(produtoAtualizado.getUnidadeMedida());
        produto.setQuantidade(produtoAtualizado.getQuantidade());
        produto.setCategoria(produtoAtualizado.getCategoria());
        produto.setFornecedores(produtoAtualizado.getFornecedores());

        return produtoRepository.save(produto);
    }

    public void excluir(Integer id) throws SmartValidityException {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }
}
