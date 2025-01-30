package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.repository.ItemProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemProdutoService {

    @Autowired
    private ItemProdutoRepository itemProdutoRepository;

    public List<ItemProduto> listarTodos() {
        return itemProdutoRepository.findAll();
    }

    public ItemProduto buscarPorId(Integer id) throws SmartValidityException {
        return itemProdutoRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("ItemProduto não encontrado com o ID: " + id));
    }

    public List<ItemProduto> buscarPorProduto(Integer produtoId) {
        return itemProdutoRepository.findByProdutoId(produtoId);
    }

    public ItemProduto salvar(ItemProduto itemProduto) {
        return itemProdutoRepository.save(itemProduto);
    }

    public ItemProduto atualizar(Integer id, ItemProduto itemProdutoAtualizado) throws SmartValidityException {
        ItemProduto itemProduto = buscarPorId(id);

        itemProduto.setLote(itemProdutoAtualizado.getLote());
        itemProduto.setPrecoCompra(itemProdutoAtualizado.getPrecoCompra());
        itemProduto.setPrecoVenda(itemProdutoAtualizado.getPrecoVenda());
        itemProduto.setDataFabricacao(itemProdutoAtualizado.getDataFabricacao());
        itemProduto.setDataVencimento(itemProdutoAtualizado.getDataVencimento());
        itemProduto.setDataRecebimento(itemProdutoAtualizado.getDataRecebimento());
        itemProduto.setProduto(itemProdutoAtualizado.getProduto());

        return itemProdutoRepository.save(itemProduto);
    }

    public void excluir(Integer id) throws SmartValidityException {
        ItemProduto itemProduto = buscarPorId(id);
        itemProdutoRepository.delete(itemProduto);
    }
}
