package br.com.smartvalidity.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/alertas")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @PostMapping("/legacy")
    public ResponseEntity<AlertaDTO.Response> create(@RequestBody AlertaDTO.Request dto) {
        return ResponseEntity.ok(alertaService.create(dto));
    }

    @GetMapping("/legacy")
    public ResponseEntity<List<AlertaDTO.Response>> findAll() {
        return ResponseEntity.ok(alertaService.findAll());
    }

    @PutMapping("/legacy/{id}")
    public ResponseEntity<AlertaDTO.Response> update(@PathVariable Integer id, @RequestBody AlertaDTO.Request dto) {
        return alertaService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar alerta personalizado", 
               description = "Cria um novo alerta personalizado com vinculação automática de itens-produto")
    public ResponseEntity<?> criarAlerta(
            @RequestBody AlertaDTO.Cadastro alertaDTO,
            @RequestParam(required = false) String usuarioCriadorId) {
        
        try {
            log.info("Recebendo requisição para criar alerta: {}", alertaDTO.getTitulo());
            log.info("DTO completo recebido: {}", alertaDTO);
            AlertaDTO.Listagem response = alertaService.criarAlerta(alertaDTO, usuarioCriadorId);
            return ResponseEntity.status(201).body(response);
        } catch (SmartValidityException e) {
            log.error("Erro de validação ao criar alerta: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro de validação");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Erro inesperado ao criar alerta: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro interno do servidor");
            errorResponse.put("message", "Erro ao processar a solicitação: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
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

    @PutMapping("/{id}/toggle-ativo")
    @Operation(summary = "Alternar status ativo do alerta")
    public ResponseEntity<AlertaDTO.Listagem> toggleAtivo(@PathVariable Integer id) throws SmartValidityException {
        AlertaDTO.Listagem response = alertaService.toggleAtivo(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filtro")
    @Operation(summary = "Listar alertas com filtros")
    public ResponseEntity<List<AlertaDTO.Listagem>> listarComFiltro(@RequestBody AlertaDTO.Filtro filtro) {
        List<AlertaDTO.Listagem> lista = alertaService.filtrarAlertas(filtro);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/contar-registros")
    @Operation(summary = "Contar alertas com filtros")
    public ResponseEntity<Long> contarRegistros(@RequestBody AlertaDTO.Filtro filtro) {
        long total = alertaService.contarAlertasFiltrados(filtro);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/count")
    @Operation(summary = "Contar alertas com filtros (formato {{total}})")
    public ResponseEntity<java.util.Map<String, Long>> contarRegistrosMap(@RequestBody AlertaDTO.Filtro filtro) {
        long total = alertaService.contarAlertasFiltrados(filtro);
        return ResponseEntity.ok(java.util.Map.of("total", total));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir alerta")
    public ResponseEntity<?> excluirAlerta(@PathVariable Integer id) {
        try {
            alertaService.delete(id);
            log.info("Alerta {} excluído com sucesso", id);
            return ResponseEntity.noContent().build();
        } catch (SmartValidityException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Exclusão negada");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 