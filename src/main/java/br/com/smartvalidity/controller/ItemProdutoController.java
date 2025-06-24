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
import br.com.smartvalidity.model.entity.ItemProduto;
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
    public ResponseEntity<ItemProduto> salvar(@Valid @RequestBody ItemProduto itemProduto) throws SmartValidityException {
        ItemProduto novoItemProduto = itemProdutoService.salvar(itemProduto);
        return ResponseEntity.status(201).body(novoItemProduto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemProduto> atualizar(@PathVariable String id, @Valid @RequestBody ItemProduto itemProduto) throws SmartValidityException {
        ItemProduto itemAtualizado = itemProdutoService.atualizar(id, itemProduto);
        return ResponseEntity.ok(itemAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) throws SmartValidityException {
        itemProdutoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
