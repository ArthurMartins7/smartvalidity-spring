package br.com.smartvalidity.controller;


import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("fornecedor")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @PostMapping
    public ResponseEntity<Fornecedor> salvarFornecedor(@Valid @RequestBody Fornecedor fornecedor) {
        try {
            Fornecedor novoFornecedor = fornecedorService.salvar(fornecedor);
            return ResponseEntity.status(201).body(novoFornecedor);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<Fornecedor>> listarTodas() {

        return ResponseEntity.ok(fornecedorService.listarTodos());
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
