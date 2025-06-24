package br.com.smartvalidity.controller;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.dto.AlertaRequestDTO;
import br.com.smartvalidity.model.dto.AlertaResponseDTO;
import br.com.smartvalidity.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alertas")
@Slf4j
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    // ===== ENDPOINTS ANTIGOS (manter compatibilidade) =====

    @PostMapping("/legacy")
    public ResponseEntity<AlertaResponseDTO> create(@RequestBody AlertaRequestDTO dto) {
        return ResponseEntity.ok(alertaService.create(dto));
    }

    @GetMapping("/legacy")
    public ResponseEntity<List<AlertaResponseDTO>> findAll() {
        return ResponseEntity.ok(alertaService.findAll());
    }

    @PutMapping("/legacy/{id}")
    public ResponseEntity<AlertaResponseDTO> update(@PathVariable Integer id, @RequestBody AlertaRequestDTO dto) {
        return alertaService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===== ENDPOINTS MODERNOS (usados pelo frontend) =====

    @PostMapping
    @Operation(summary = "Criar alerta personalizado", 
               description = "Cria um novo alerta personalizado com vinculação automática de itens-produto")
    public ResponseEntity<AlertaDTO.Listagem> criarAlerta(
            @Valid @RequestBody AlertaDTO.Cadastro alertaDTO,
            @RequestParam(required = false) String usuarioCriadorId) throws SmartValidityException {
        
        log.info("Recebendo requisição para criar alerta: {}", alertaDTO.getTitulo());
        AlertaDTO.Listagem response = alertaService.criarAlerta(alertaDTO, usuarioCriadorId);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar alerta por ID")
    public ResponseEntity<AlertaDTO.Listagem> buscarPorId(@PathVariable Integer id) throws SmartValidityException {
        AlertaDTO.Listagem response = alertaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar alerta personalizado")
    public ResponseEntity<AlertaDTO.Listagem> atualizarAlerta(
            @PathVariable Integer id, 
            @Valid @RequestBody AlertaDTO.Edicao alertaDTO) throws SmartValidityException {
        
        log.info("Recebendo requisição para atualizar alerta ID: {}", id);
        AlertaDTO.Listagem response = alertaService.atualizarAlerta(id, alertaDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir alerta")
    public ResponseEntity<Void> excluirAlerta(@PathVariable Integer id) throws SmartValidityException {
        alertaService.delete(id);
        log.info("Alerta {} excluído com sucesso", id);
        return ResponseEntity.noContent().build();
    }
} 