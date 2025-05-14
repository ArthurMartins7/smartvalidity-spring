package br.com.smartvalidity.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.repository.ItemProdutoRepository;

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

    /**
     * Salva um item que foi marcado como inspecionado, com tratamento transacional específico
     * 
     * @param itemProduto O item de produto a ser salvo como inspecionado
     * @return O item de produto salvo
     * @throws SmartValidityException Se ocorrer algum erro durante o salvamento
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public ItemProduto salvarItemInspecionado(ItemProduto itemProduto) throws SmartValidityException {
        try {
            // Verificação adicional para garantir que o item existe
            if (itemProduto.getId() != null) {
                ItemProduto itemExistente = itemProdutoRepository.findById(itemProduto.getId())
                    .orElseThrow(() -> new SmartValidityException("ItemProduto não encontrado com o ID: " + itemProduto.getId()));
                
                // Atualizar os campos necessários para inspeção
                itemExistente.setInspecionado(itemProduto.getInspecionado());
                itemExistente.setMotivoInspecao(itemProduto.getMotivoInspecao());
                itemExistente.setUsuarioInspecao(itemProduto.getUsuarioInspecao());
                itemExistente.setDataHoraInspecao(itemProduto.getDataHoraInspecao());
                
                // Salvar o item
                return itemProdutoRepository.save(itemExistente);
            } else {
                throw new SmartValidityException("ID do item não pode ser nulo para inspeção");
            }
        } catch (Exception e) {
            if (e instanceof SmartValidityException) {
                throw (SmartValidityException) e;
            }
            // Logar o erro para diagnóstico
            System.err.println("Erro ao salvar item inspecionado: " + e.getMessage());
            e.printStackTrace();
            throw new SmartValidityException("Erro ao salvar item inspecionado: " + e.getMessage());
        }
    }
}
