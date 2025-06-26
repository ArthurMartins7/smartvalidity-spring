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
import br.com.smartvalidity.model.dto.ProdutoDTO;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.seletor.ProdutoSeletor;
import br.com.smartvalidity.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/produto")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        return ResponseEntity.ok(produtoService.listarTodosDTO());
    }
    
    @GetMapping("/com-itens-nao-inspecionados")
    @Operation(summary = "Listar produtos com itens não inspecionados",
               description = "Retorna apenas produtos que possuem itens-produto não inspecionados, para uso em alertas personalizados")
    public ResponseEntity<List<Produto>> listarProdutosComItensNaoInspecionados() {
        List<Produto> produtos = produtoService.buscarProdutosComItensNaoInspecionados();
        return ResponseEntity.ok(produtos);
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
