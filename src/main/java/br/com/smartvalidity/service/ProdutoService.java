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

    public List<Produto> buscarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(String id) throws SmartValidityException {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Produto não encontrado com o ID: " + id));
    }

    public List<Produto> buscarPorCategoria(String categoriaId) {
        return produtoRepository.findByCategoriaId(categoriaId);
    }



    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public Produto atualizar(String id, Produto produtoAtualizado) throws SmartValidityException {
        Produto produto = buscarPorId(id);

        produto.setCodigoBarras(produtoAtualizado.getCodigoBarras());
        produto.setDescricao(produtoAtualizado.getDescricao());
        produto.setMarca(produtoAtualizado.getMarca());
        produto.setUnidadeMedida(produtoAtualizado.getUnidadeMedida());
        produto.setCategoria(produtoAtualizado.getCategoria());
        produto.setFornecedores(produtoAtualizado.getFornecedores());

        return produtoRepository.save(produto);
    }

    public void excluir(String id) throws SmartValidityException {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }
}
