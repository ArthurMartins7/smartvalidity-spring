package br.com.smartvalidity.service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ItemProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemProdutoService {

    @Autowired
    private ItemProdutoRepository itemProdutoRepository;

    @Autowired
    private ProdutoService produtoService;

    public List<ItemProduto> buscarTodos() {
        return itemProdutoRepository.findAll();
    }

    public ItemProduto buscarPorId(String id) throws SmartValidityException {
        return itemProdutoRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("ItemProduto não encontrado com o ID: " + id));
    }

    public List<ItemProduto> buscarPorProduto(String produtoId) {
        return itemProdutoRepository.findByProdutoId(produtoId);
    }

    public List<ItemProduto> buscarPorLote(final String lote) throws SmartValidityException {
        List<ItemProduto> itens = this.itemProdutoRepository.findByLote(lote);

        Optional.ofNullable(itens).orElseThrow(() -> new SmartValidityException("Não existe nenhum produto no estoque com esse lote."));

        return itens;
    }

    public ItemProduto salvar2(ItemProduto itemProduto) {
        return itemProdutoRepository.save(itemProduto);
    }

    public ItemProduto salvar(final ItemProduto itemProduto) throws SmartValidityException {
        Produto produto = this.produtoService.buscarPorId(itemProduto.getProduto().getId());

        itemProduto.setProduto(produto);

        produto.setQuantidade(produto.getQuantidade() + 1);

        return itemProdutoRepository.save(itemProduto);
    }

    public ItemProduto atualizar(String id, ItemProduto itemProdutoAtualizado) throws SmartValidityException {
        ItemProduto itemProduto = buscarPorId(id);

        itemProduto.setLote(itemProdutoAtualizado.getLote());
        itemProduto.setPrecoVenda(itemProdutoAtualizado.getPrecoVenda());
        itemProduto.setDataFabricacao(itemProdutoAtualizado.getDataFabricacao());
        itemProduto.setDataVencimento(itemProdutoAtualizado.getDataVencimento());
        itemProduto.setDataRecebimento(itemProdutoAtualizado.getDataRecebimento());
        itemProduto.setProduto(itemProdutoAtualizado.getProduto());

        return itemProdutoRepository.save(itemProduto);
    }

    public void excluir2(String id) throws SmartValidityException {
        ItemProduto itemProduto = this.buscarPorId(id);
        itemProdutoRepository.delete(itemProduto);
    }

    public void excluir(final String idItemProduto) throws SmartValidityException {
        ItemProduto itemProduto = this.buscarPorId(idItemProduto);

        Produto produto = this.produtoService.buscarPorId(itemProduto.getProduto().getId());

        this.itemProdutoRepository.delete(itemProduto);

        produto.setQuantidade(produto.getQuantidade() - 1);

        this.produtoService.salvar(produto);

    }

    public void darBaixaItensVendidos(List<ItemProduto> itemProduto) throws SmartValidityException {

        itemProduto.forEach(item -> {
            try {
                this.buscarPorLote(item.getLote());


            } catch (SmartValidityException e) {
                throw new RuntimeException(e);
            }
        });

        for (ItemProduto item : itemProduto) {
            this.buscarPorLote(item.getLote());

            //this.excluir(item);

        }

    }

    public void darBaixaItensVendidos(String lote, int quantidadeParaRemover) throws SmartValidityException {
        List<ItemProduto> itensProduto = this.buscarPorLote(lote);

        List<ItemProduto> itensParaDarBaixa = itensProduto.stream()
                .limit(quantidadeParaRemover)
                .toList();

        for (ItemProduto item : itensParaDarBaixa) {
            this.excluir(item.getId());
        }

    }
}
