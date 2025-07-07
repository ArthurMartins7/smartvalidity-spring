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
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.seletor.FornecedorSeletor;
import br.com.smartvalidity.service.FornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;


@RestController
@RequestMapping("fornecedor")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @PostMapping
    public ResponseEntity<Fornecedor> salvarFornecedor(@Valid @RequestBody Fornecedor fornecedor) throws SmartValidityException {
        Fornecedor novoFornecedor = fornecedorService.salvar(fornecedor);
        return ResponseEntity.status(201).body(novoFornecedor);
    }


    @GetMapping
    public ResponseEntity<List<Fornecedor>> listarTodas() {

        return ResponseEntity.ok(fornecedorService.listarTodos());
    }

    @Operation(summary = "Pesquisar fornecedores com filtros",
            description = "Retorna uma lista de fornecedores que atendem aos critérios especificados no seletor.")
    @PostMapping("/filtro")
    public List<Fornecedor> pesquisarComSeletor(@RequestBody FornecedorSeletor seletor) {
        return fornecedorService.pesquisarComSeletor(seletor);
    }

    @PostMapping("/contar")
    public ResponseEntity<Long> contarTotalRegistros(@RequestBody FornecedorSeletor seletor) {
        long total = fornecedorService.contarTotalRegistros(seletor);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fornecedor> buscarPorId(@PathVariable Integer id) throws SmartValidityException {
        Fornecedor fornecedor = fornecedorService.buscarPorId(id);
        return ResponseEntity.ok(fornecedor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fornecedor> atualizarFornecedor(@PathVariable Integer id, @Valid @RequestBody Fornecedor fornecedor) throws SmartValidityException {
        Fornecedor fornecedorAtualizado = fornecedorService.atualizar(id, fornecedor);
        return ResponseEntity.ok(fornecedor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarFornecedor(@PathVariable Integer id) throws SmartValidityException {
        fornecedorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
