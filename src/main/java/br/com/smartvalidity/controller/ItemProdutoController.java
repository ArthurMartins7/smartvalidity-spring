package br.com.smartvalidity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.ItemProdutoDTO;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.service.ItemProdutoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/item-produto")
public class ItemProdutoController {

    @Autowired
    private ItemProdutoService itemProdutoService;

    @GetMapping
    public ResponseEntity<List<ItemProduto>> buscarTodos() {
        return ResponseEntity.ok(itemProdutoService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemProduto> buscarPorId(@PathVariable String id) throws SmartValidityException {
        ItemProduto itemProduto = itemProdutoService.buscarPorId(id);
        return ResponseEntity.ok(itemProduto);
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<ItemProduto>> buscarPorProduto(@PathVariable String produtoId) {
        return ResponseEntity.ok(itemProdutoService.buscarPorProduto(produtoId));
    }
    
    @GetMapping("/produto/{produtoId}/nao-inspecionados")
    public ResponseEntity<List<ItemProduto>> buscarItensProdutoNaoInspecionadosPorProduto(@PathVariable String produtoId) {
        List<ItemProduto> itens = itemProdutoService.buscarItensProdutoNaoInspecionadosPorProduto(produtoId);
        return ResponseEntity.ok(itens);
    }

    @PostMapping
    public ResponseEntity<?> salvar(@Valid @RequestBody ItemProdutoDTO itemProdutoDTO) throws SmartValidityException {
        ItemProduto itemProduto = new ItemProduto();
        itemProduto.setLote(itemProdutoDTO.getLote());
        itemProduto.setPrecoVenda(itemProdutoDTO.getPrecoVenda());
        itemProduto.setDataFabricacao(itemProdutoDTO.getDataFabricacao());
        itemProduto.setDataVencimento(itemProdutoDTO.getDataVencimento());
        itemProduto.setDataRecebimento(itemProdutoDTO.getDataRecebimento());
        itemProduto.setInspecionado(itemProdutoDTO.getInspecionado() != null ? itemProdutoDTO.getInspecionado() : false);
        itemProduto.setMotivoInspecao(itemProdutoDTO.getMotivoInspecao());
        itemProduto.setUsuarioInspecao(itemProdutoDTO.getUsuarioInspecao());
        itemProduto.setDataHoraInspecao(itemProdutoDTO.getDataHoraInspecao());
        itemProduto.setProduto(itemProdutoDTO.getProduto());
        
        Integer quantidade = itemProdutoDTO.getQuantidade() != null ? itemProdutoDTO.getQuantidade() : 1;
        
        if (quantidade == 1) {
            // Salvar um único item
            ItemProduto novoItemProduto = itemProdutoService.salvar(itemProduto);
            return ResponseEntity.status(201).body(novoItemProduto);
        } else {
            // Salvar múltiplos itens
            List<ItemProduto> novosItensProduto = itemProdutoService.salvarMultiplos(itemProduto, quantidade);
            return ResponseEntity.status(201).body(novosItensProduto);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemProduto> atualizar(@PathVariable String id, @Valid @RequestBody ItemProdutoDTO itemProdutoDTO) throws SmartValidityException {
        ItemProduto itemProduto = new ItemProduto();
        itemProduto.setId(id);
        itemProduto.setLote(itemProdutoDTO.getLote());
        itemProduto.setPrecoVenda(itemProdutoDTO.getPrecoVenda());
        itemProduto.setDataFabricacao(itemProdutoDTO.getDataFabricacao());
        itemProduto.setDataVencimento(itemProdutoDTO.getDataVencimento());
        itemProduto.setDataRecebimento(itemProdutoDTO.getDataRecebimento());
        itemProduto.setInspecionado(itemProdutoDTO.getInspecionado() != null ? itemProdutoDTO.getInspecionado() : false);
        itemProduto.setMotivoInspecao(itemProdutoDTO.getMotivoInspecao());
        itemProduto.setUsuarioInspecao(itemProdutoDTO.getUsuarioInspecao());
        itemProduto.setDataHoraInspecao(itemProdutoDTO.getDataHoraInspecao());
        itemProduto.setProduto(itemProdutoDTO.getProduto());
        
        ItemProduto itemAtualizado = itemProdutoService.atualizar(id, itemProduto);
        return ResponseEntity.ok(itemAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) throws SmartValidityException {
        itemProdutoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
