package br.com.smartvalidity.controller;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.seletor.FornecedorSeletor;
import br.com.smartvalidity.model.seletor.ProdutoSeletor;
import br.com.smartvalidity.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/produto")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        return ResponseEntity.ok(produtoService.listarTodosDTO());
    }

    @Operation(summary = "Pesquisar produtos com filtros",
            description = "Retorna uma lista de produtos que atendem aos critérios especificados no seletor.")
    @PostMapping("/filtro")
    public List<Produto> pesquisarComSeletor(@RequestBody ProdutoSeletor seletor) {
        return produtoService.pesquisarComSeletor(seletor);
    }

    @PostMapping("/contar")
    public ResponseEntity<Long> contarTotalRegistros(@RequestBody ProdutoSeletor seletor) {
        long total = produtoService.contarTotalRegistros(seletor);
        return ResponseEntity.ok(total);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable String id) throws SmartValidityException {
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Produto>> buscarPorCategoria(@PathVariable String categoriaId) {
        List<Produto> produtos = produtoService.buscarPorCategoria(categoriaId);
        return ResponseEntity.ok(produtos);
    }



    @PostMapping
    public ResponseEntity<Produto> salvar(@Valid @RequestBody Produto produto) throws SmartValidityException {
        Produto novoProduto = produtoService.salvar(produto);
        return ResponseEntity.status(201).body(novoProduto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable String id, @Valid @RequestBody Produto produto) throws SmartValidityException {
        Produto produtoAtualizado = produtoService.atualizar(id, produto);
        return ResponseEntity.ok(produtoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) throws SmartValidityException {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
