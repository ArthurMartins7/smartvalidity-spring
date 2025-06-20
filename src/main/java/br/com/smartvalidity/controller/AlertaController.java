package br.com.smartvalidity.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.seletor.AlertaSeletor;
import br.com.smartvalidity.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/alertas")
@CrossOrigin(origins = "*")
public class AlertaController {

    @Autowired
    private AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaDTO.Listagem>> listarTodos() throws SmartValidityException {
        List<AlertaDTO.Listagem> alertas = alertaService.listarTodos();
        return ResponseEntity.ok(alertas);
    }

    @Operation(summary = "Buscar alertas com filtros")
    @PostMapping("/filtro")
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarComFiltros(@RequestBody AlertaSeletor seletor) throws SmartValidityException {
        List<AlertaDTO.Listagem> alertas = alertaService.buscarComSeletor(seletor);
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaDTO.Listagem> buscarPorId(@PathVariable Integer id) throws SmartValidityException {
        AlertaDTO.Listagem alerta = alertaService.buscarPorId(id);
        return ResponseEntity.ok(alerta);
    }

    @PostMapping
    public ResponseEntity<AlertaDTO.Listagem> criar(@Valid @RequestBody AlertaDTO.Cadastro cadastroDTO, 
                                                    @RequestParam(required = false) String usuarioCriadorId) throws SmartValidityException {
        AlertaDTO.Listagem alertaCriado = alertaService.salvar(cadastroDTO, usuarioCriadorId);
        return ResponseEntity.status(201).body(alertaCriado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertaDTO.Listagem> atualizar(@PathVariable Integer id,
                                                        @Valid @RequestBody AlertaDTO.Edicao edicaoDTO) throws SmartValidityException {
        AlertaDTO.Listagem alertaAtualizado = alertaService.atualizar(id, edicaoDTO);
        return ResponseEntity.ok(alertaAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) throws SmartValidityException {
        alertaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ativar ou desativar um alerta")
    @PatchMapping("/{id}/toggle-ativo")
    public ResponseEntity<AlertaDTO.Listagem> toggleAtivo(@PathVariable Integer id) throws SmartValidityException {
        AlertaDTO.Listagem alertaAtualizado = alertaService.toggleAtivo(id);
        return ResponseEntity.ok(alertaAtualizado);
    }

    @Operation(summary = "Forçar geração de alertas automáticos")
    @PostMapping("/gerar-automaticos")
    public ResponseEntity<Map<String, String>> gerarAlertasAutomaticos() {
        alertaService.gerarAlertasAutomaticos();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Alertas automáticos gerados com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/contar-registros")
    public ResponseEntity<Long> contarTotalRegistros(@RequestBody AlertaSeletor seletor) throws SmartValidityException {
        long total = alertaService.contarTotalRegistros(seletor);
        return ResponseEntity.ok(total);
    }
}