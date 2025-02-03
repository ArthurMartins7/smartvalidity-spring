package br.com.smartvalidity.controller;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.service.CorredorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("corredor")
public class CorredorController {

    @Autowired
    private CorredorService corredorService;

    @PostMapping
    public ResponseEntity<Corredor> salvarCorredor(@Valid @RequestBody Corredor corredor) {
        Corredor novoCorredor = corredorService.salvar(corredor);
        return ResponseEntity.status(201).body(novoCorredor);
    }

    @GetMapping
    public ResponseEntity<List<Corredor>> listarTodOs() {
        return ResponseEntity.ok(corredorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Corredor> buscarPorId(@PathVariable Integer id) throws SmartValidityException {
        Corredor corredor = corredorService.buscarPorId(id);
        return ResponseEntity.ok(corredor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Corredor> atualizarCorredor(@PathVariable Integer id, @Valid @RequestBody Corredor corredor) throws SmartValidityException {
        Corredor corredorAtualizado = corredorService.atualizar(id, corredor);
        return ResponseEntity.ok(corredorAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCorredor(@PathVariable Integer id) throws SmartValidityException {
        corredorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
