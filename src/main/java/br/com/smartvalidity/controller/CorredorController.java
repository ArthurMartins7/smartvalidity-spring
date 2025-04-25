package br.com.smartvalidity.controller;

import br.com.smartvalidity.auth.AuthenticationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import br.com.smartvalidity.model.seletor.CorredorSeletor;
import br.com.smartvalidity.service.CorredorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("corredor")
@MultipartConfig(fileSizeThreshold = 10485760) // 10MB
public class CorredorController {

    @Autowired
    private CorredorService corredorService;

    @PostMapping
    public ResponseEntity<Corredor> salvarCorredor(@Valid @RequestBody Corredor corredor) throws SmartValidityException {
        Corredor novo = corredorService.salvar(corredor);
        return ResponseEntity.status(201).body(novo);
    }

    @GetMapping
    public ResponseEntity<List<Corredor>> listarTodos() {
        return ResponseEntity.ok(corredorService.listarTodos());
    }

    @PostMapping("/filtro")
    public ResponseEntity<List<Corredor>> pesquisarComFiltro(@RequestBody CorredorSeletor seletor) {
        List<Corredor> resultado = corredorService.pesquisarComSeletor(seletor);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/filtro/paginas")
    public ResponseEntity<Integer> contarPaginas(@RequestBody CorredorSeletor seletor) {
        int paginas = corredorService.contarPaginas(seletor);
        return ResponseEntity.ok(paginas);
    }

    @PostMapping("/contar")
    public ResponseEntity<Long> contarTotalRegistros(@RequestBody CorredorSeletor seletor) {
        long total = corredorService.contarTotalRegistros(seletor);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Corredor> buscarPorId(@PathVariable Integer id) throws SmartValidityException {
        Corredor corredor = corredorService.buscarPorId(id);
        return ResponseEntity.ok(corredor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Corredor> atualizar(@PathVariable Integer id, @Valid @RequestBody Corredor corredor) throws SmartValidityException {
        Corredor atualizado = corredorService.atualizar(id, corredor);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) throws SmartValidityException {
        corredorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
