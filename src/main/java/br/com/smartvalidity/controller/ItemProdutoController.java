package br.com.smartvalidity.controller;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.service.ItemProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
