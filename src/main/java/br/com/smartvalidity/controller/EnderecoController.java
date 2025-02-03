package br.com.smartvalidity.controller;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.service.EnderecoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("endereco")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    @PostMapping
    public ResponseEntity<Endereco> salvarEndereco(@Valid @RequestBody Endereco endereco) {
        Endereco novoEndereco = enderecoService.salvar(endereco);
        return ResponseEntity.status(201).body(novoEndereco);
    }

    @GetMapping
    public ResponseEntity<List<Endereco>> listarTodos() {

        return ResponseEntity.ok(enderecoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endereco> buscarPorId(@PathVariable Integer id) throws SmartValidityException {
        Endereco endereco = enderecoService.buscarPorId(id);
        return ResponseEntity.ok(endereco);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Endereco> atualizarEndereco(@PathVariable Integer id, @Valid @RequestBody Endereco endereco) throws SmartValidityException {
        Endereco enderecoAtualizado = enderecoService.atualizar(id, endereco);
        return ResponseEntity.ok(enderecoAtualizado);
    }
}
